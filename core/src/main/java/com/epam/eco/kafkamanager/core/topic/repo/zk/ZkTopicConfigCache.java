/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.core.topic.repo.zk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.utils.ZKPaths;
import org.apache.kafka.clients.admin.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.utils.MapperUtils;

import kafka.server.ConfigType;
import kafka.utils.ZkUtils;

/**
 * @author Andrei_Tytsik
 */
class ZkTopicConfigCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkTopicConfigCache.class);

    private static final String CONFIGS_PATH = ZkUtils.getEntityConfigRootPath(ConfigType.Topic());

    private static final String VERSION = "version";
    private static final String CONFIG = "config";

    private final KafkaAdminOperations adminOperations;

    private final PathChildrenCache configPathCache;

    private final Map<String, TopicConfig> configCache = new HashMap<>();
    private final AtomicReference<TopicConfig> defaultConfig = new AtomicReference<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public ZkTopicConfigCache(
            CuratorFramework curatorFramework,
            KafkaAdminOperations adminOperations,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(adminOperations, "KafkaAdminOperations can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        configPathCache = new PathChildrenCache(curatorFramework, CONFIGS_PATH, false);

        this.adminOperations = adminOperations;
        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        configPathCache.getListenable().addListener(
                (client, event) -> handlePathEvent(event));
        configPathCache.start(StartMode.BUILD_INITIAL_CACHE);

        initDefaultConfig();
        bootstrapCache();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        configPathCache.close();

        LOGGER.info("Closed");
    }

    public TopicConfig getConfig(String topicName) {
        lock.readLock().lock();
        try {
            return configCache.getOrDefault(topicName, defaultConfig.get());
        } finally {
            lock.readLock().unlock();
        }
    }

    private void initDefaultConfig() {
        String topicName = UUID.randomUUID().toString();
        defaultConfig.set(describeTopicConfigs(Collections.singletonList(topicName)).get(topicName));
    }

    private void bootstrapCache() {
        lock.writeLock().lock();
        try {
            List<String> topicNames = configPathCache.getCurrentData().stream()
                    .map(childData -> getTopicNameFromPath(childData.getPath()))
                    .collect(Collectors.toList());
            Map<String, TopicConfig> configMap = describeTopicConfigs(topicNames);
            if (!configMap.isEmpty()) {
                configCache.putAll(configMap);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void handlePathEvent(PathChildrenCacheEvent event) {
        if (CuratorUtils.isConnectionStateChangeEvent(event.getType())) {
            LOGGER.warn("ZK connection state changed: {}", event.getType());
            return;
        }

        TopicConfig updatedConfig = null;
        String topicNameOfRemovedConfig = null;

        if (event.getType() == Type.CHILD_ADDED || event.getType() == Type.CHILD_UPDATED) {
            updatedConfig = handleConfigUpdated(event.getData());
        } else if (event.getType() == Type.CHILD_REMOVED) {
            topicNameOfRemovedConfig = handleConfigRemoved(event.getData());
        }

        fireCacheListener(updatedConfig, topicNameOfRemovedConfig);
    }

    private TopicConfig handleConfigUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            String topicName = getTopicNameFromPath(childData.getPath());
            TopicConfig topicConfig =
                    describeTopicConfigs(Collections.singleton(topicName)).get(topicName);
            configCache.put(topicName, topicConfig);
            return topicConfig;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private String handleConfigRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            String topicName = getTopicNameFromPath(childData.getPath());
            configCache.remove(topicName);
            return topicName;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Map<String, TopicConfig> describeTopicConfigs(Collection<String> topicNames) {
        Map<String, Config> configs = adminOperations.describeTopicConfigs(topicNames);
        return configs.entrySet().stream().
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new TopicConfig(e.getKey(), e.getValue())));
    }

    @SuppressWarnings("unused")
    private Map<String, Object> toConfig(String topicName, ChildData childData) {
        String configInfoString = new String(childData.getData(), StandardCharsets.UTF_8);
        Map<String, Object> configInfoMap = MapperUtils.jsonToMap(configInfoString);

        Integer version = (Integer)configInfoMap.get(VERSION);
        if (version == 1) {
            return toConfigV1(configInfoMap);
        } else {
            throw new RuntimeException(
                    String.format("Unsupported config version: %s", configInfoString));
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> toConfigV1(Map<String, Object> configInfoMap) {
        return (Map<String, Object>)configInfoMap.get(CONFIG);
    }

    private String getTopicNameFromPath(String path) {
        return ZKPaths.getNodeFromPath(path);
    }

    private void fireCacheListener(TopicConfig updatedConfig, String topicNameOfRemovedConfig) {
        if (updatedConfig != null) {
            try {
                cacheListener.onTopicConfigUpdated(updatedConfig);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'topic config updated' event. Config = %s",
                                updatedConfig),
                        ex);
            }
        }
        if (topicNameOfRemovedConfig != null) {
            try {
                cacheListener.onTopicConfigRemoved(topicNameOfRemovedConfig);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'topic config removed' event. Topic name = %s",
                                topicNameOfRemovedConfig),
                        ex);
            }
        }
    }

    public static class TopicConfig {

        public final String name;
        public final Config config;

        public TopicConfig(String name, Config config) {
            Validate.notBlank(name, "Topic name is blank");
            Validate.notNull(config, "Config is null");

            this.name = name;
            this.config = config;
        }

    }

    public static interface CacheListener {
        void onTopicConfigUpdated(TopicConfig topicConfig);
        void onTopicConfigRemoved(String topicName);
    }

}
