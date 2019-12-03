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
package com.epam.eco.kafkamanager.core.broker.repo.zk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.utils.ZKPaths;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.common.config.ConfigDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.config.BrokerConfigDef;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.utils.MapperUtils;

import kafka.server.ConfigType;
import kafka.zk.ConfigEntityTypeZNode;

/**
 * @author Raman_Babich
 */
public class ZkBrokerConfigCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkBrokerConfigCache.class);

    private static final Config DEFAULT_CONFIG = createDefaultConfig();
    private static final String CONFIGS_PATH = ConfigEntityTypeZNode.path(ConfigType.Broker());
    private static final String VERSION = "version";
    private static final String CONFIG = "config";

    private final PathChildrenCache configPathCache;
    private final KafkaAdminOperations adminOperations;
    private final ZkBrokerConfigCache.CacheListener cacheListener;
    private final Map<Integer, ZkBrokerConfigCache.BrokerConfig> configCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public ZkBrokerConfigCache(
            CuratorFramework curatorFramework,
            KafkaAdminOperations adminOperations,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(adminOperations, "KafkaAdminOperations can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        configPathCache = new PathChildrenCache(curatorFramework, CONFIGS_PATH, true);

        this.adminOperations = adminOperations;
        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        configPathCache.getListenable().addListener(
                (client, event) -> handlePathEvent(event));
        configPathCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);

        bootstrapCache();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        configPathCache.close();

        LOGGER.info("Closed");
    }

    public BrokerConfig getConfig(int brokerId) {
        BrokerConfig brokerConfig = getFromCache(brokerId);
        if (brokerConfig != null) {
            return brokerConfig;
        }
        return getFromBrokerOrDefault(brokerId);
    }

    private BrokerConfig getFromCache(int brokerId) {
        lock.readLock().lock();
        try {
            return configCache.get(brokerId);
        } finally {
            lock.readLock().unlock();
        }
    }

    private BrokerConfig getFromBrokerOrDefault(int brokerId) {
        lock.writeLock().lock();
        try {
            BrokerConfig brokerConfig = configCache.get(brokerId);
            if (brokerConfig != null) {
                return brokerConfig;
            }
            try {
                brokerConfig = describeBrokerConfig(brokerId);
            } catch (Exception ex) {
                LOGGER.warn(String.format("Failed to describe config for '%s' broker", brokerId), ex);
            }
            if (brokerConfig != null) {
                configCache.put(brokerId, brokerConfig);
                return brokerConfig;
            }
            return new BrokerConfig(brokerId, DEFAULT_CONFIG);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void bootstrapCache() {
        lock.writeLock().lock();
        try {
            Map<Integer, Map<String, Object>> effectiveConfigs = configPathCache.getCurrentData().stream()
                    .collect(Collectors.toMap(
                            childData -> getBrokerIdFromPath(childData.getPath()),
                            this::toConfig));
            if (!effectiveConfigs.isEmpty()) {
                Map<Integer, BrokerConfig> configMap = describeBrokerConfigs(effectiveConfigs.keySet());
                for (Map.Entry<Integer, Map<String, Object>> entry : effectiveConfigs.entrySet()) {
                    Map<String, Object> effectiveConfig = entry.getValue();
                    if (effectiveConfig.isEmpty()) {
                        continue;
                    }
                    Integer brokerId = entry.getKey();
                    configMap.put(brokerId, applyEffectiveConfig(configMap.get(brokerId), effectiveConfig));
                }
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

        BrokerConfig updatedConfig = null;
        Integer brokerIdOfRemovedConfig = null;

        if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED || event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            updatedConfig = handleConfigUpdated(event.getData());
        } else if (event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            brokerIdOfRemovedConfig = handleConfigRemoved(event.getData());
        }

        fireCacheListener(updatedConfig, brokerIdOfRemovedConfig);
    }

    private BrokerConfig handleConfigUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            int brokerId = getBrokerIdFromPath(childData.getPath());
            BrokerConfig brokerConfig = configCache.get(brokerId);
            if (brokerConfig == null) {
                brokerConfig = describeBrokerConfig(brokerId);
            }
            brokerConfig = applyEffectiveConfig(brokerConfig, toConfig(childData));
            configCache.put(brokerId, brokerConfig);
            return brokerConfig;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private BrokerConfig applyEffectiveConfig(BrokerConfig config, Map<String, Object> effectiveConfig) {
        if (effectiveConfig.isEmpty()) {
            return config;
        }
        List<ConfigEntry> configEntries = new ArrayList<>();
        for (ConfigEntry entry : config.config.entries()) {
            Object obj = effectiveConfig.get(entry.name());
            if (obj != null) {
                configEntries.add(new ConfigEntry(entry.name(), obj.toString()));
            } else {
                configEntries.add(entry);
            }
        }
        return new BrokerConfig(config.id, new Config(configEntries));
    }

    private Map<String, Object> toConfig(ChildData childData) {
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

    private int handleConfigRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            int brokerId = getBrokerIdFromPath(childData.getPath());
            configCache.remove(brokerId);
            return brokerId;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private BrokerConfig describeBrokerConfig(Integer brokerId) {
        return describeBrokerConfigs(Collections.singletonList(brokerId)).get(brokerId);
    }

    private Map<Integer, BrokerConfig> describeBrokerConfigs(Collection<Integer> brokerIds) {
        Map<Integer, Config> configs = adminOperations.describeBrokerConfigs(brokerIds);
        return configs.entrySet().stream().
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> new BrokerConfig(e.getKey(), e.getValue())));
    }

    private Integer getBrokerIdFromPath(String path) {
        return Integer.valueOf(ZKPaths.getNodeFromPath(path));
    }

    private void fireCacheListener(BrokerConfig updatedConfig, Integer brokerIdOfRemovedConfig) {
        if (updatedConfig != null) {
            try {
                cacheListener.onBrokerConfigUpdated(updatedConfig);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'broker config updated' event. Config = %s",
                                updatedConfig),
                        ex);
            }
        }
        if (brokerIdOfRemovedConfig != null) {
            try {
                cacheListener.onBrokerConfigRemoved(brokerIdOfRemovedConfig);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'broker config removed' event. broker id = %s",
                                brokerIdOfRemovedConfig),
                        ex);
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static Config createDefaultConfig() {
        List<ConfigEntry> entries = new ArrayList<>(BrokerConfigDef.INSTANCE.keys().size());
        for (ConfigDef.ConfigKey key : BrokerConfigDef.INSTANCE.keys()) {
            entries.add(new ConfigEntry(
                    key.name,
                    key.hasDefault() ? Objects.toString(key.defaultValue, null) : null,
                    true,
                    false,
                    false));
        }
        return new Config(Collections.unmodifiableList(entries));
    }

    public static class BrokerConfig {

        public final Integer id;
        public final Config config;

        public BrokerConfig(Integer id, Config config) {
            Validate.notNull(id, "Id is null");
            Validate.isTrue(id >= 0, "Id is invalid: %d", id);
            Validate.notNull(config, "Config is null");

            this.id = id;
            this.config = config;
        }

    }

    public interface CacheListener {
        void onBrokerConfigUpdated(BrokerConfig brokerConfig);
        void onBrokerConfigRemoved(int brokerId);
    }

}
