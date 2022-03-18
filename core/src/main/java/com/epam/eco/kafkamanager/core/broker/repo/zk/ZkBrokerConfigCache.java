/*******************************************************************************
 *  Copyright 2022 EPAM Systems
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not
 *  use this file except in compliance with the License.  You may obtain a copy
 *  of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *  WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 *  License for the specific language governing permissions and limitations under
 *  the License.
 *******************************************************************************/
package com.epam.eco.kafkamanager.core.broker.repo.zk;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.kafkamanager.core.utils.CuratorUtils;
import com.epam.eco.kafkamanager.utils.MapperUtils;

import kafka.server.ConfigType;
import kafka.zk.ConfigEntityTypeZNode;

/**
 * @author Raman_Babich
 */
public class ZkBrokerConfigCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkBrokerConfigCache.class);

    private static final String CONFIGS_PATH = ConfigEntityTypeZNode.path(ConfigType.Broker());
    private static final String VERSION = "version";
    private static final String CONFIG = "config";

    private final PathChildrenCache configPathCache;
    private final ZkBrokerConfigCache.CacheListener cacheListener;
    private final Map<Integer, ZkBrokerConfigCache.BrokerConfig> configCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final CountDownLatch initializedLatch = new CountDownLatch(1);

    public ZkBrokerConfigCache(
            CuratorFramework curatorFramework,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        configPathCache = new PathChildrenCache(curatorFramework, CONFIGS_PATH, true);

        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        configPathCache.getListenable().addListener(
                (client, event) -> handlePathEvent(event));
        configPathCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        awaitInitialization();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        configPathCache.close();

        LOGGER.info("Closed");
    }

    public BrokerConfig getConfig(int brokerId) {
        Validate.isTrue(brokerId >= 0, "Broker id is invalid: %d", brokerId);
        lock.readLock().lock();
        try {
            return configCache.get(brokerId);
        } finally {
            lock.readLock().unlock();
        }
    }

    private void awaitInitialization() throws InterruptedException {
        initializedLatch.await();
    }

    private void signalInitializationDone() {
        initializedLatch.countDown();
    }

    private void handlePathEvent(PathChildrenCacheEvent event) {
        if (CuratorUtils.isConnectionStateChangeEvent(event.getType())) {
            LOGGER.warn("ZK connection state changed: {}", event.getType());
            return;
        }

        BrokerConfig updatedConfig = null;
        Integer brokerIdOfRemovedConfig = null;

        if (event.getType() == PathChildrenCacheEvent.Type.INITIALIZED) {
            signalInitializationDone();
        } else if (event.getType() == PathChildrenCacheEvent.Type.CHILD_ADDED || event.getType() == PathChildrenCacheEvent.Type.CHILD_UPDATED) {
            updatedConfig = handleConfigUpdated(event.getData());
        } else if (event.getType() == PathChildrenCacheEvent.Type.CHILD_REMOVED) {
            brokerIdOfRemovedConfig = handleConfigRemoved(event.getData());
        }

        fireCacheListener(updatedConfig, brokerIdOfRemovedConfig);
    }

    private BrokerConfig handleConfigUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            Integer brokerId = getBrokerIdFromPath(childData.getPath());
            Map<String, String> config = toConfig(childData);
            BrokerConfig brokerConfig = new BrokerConfig(brokerId, config);
            configCache.put(brokerId, brokerConfig);
            return brokerConfig;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Map<String, String> toConfig(ChildData childData) {
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
    private Map<String, String> toConfigV1(Map<String, Object> configInfoMap) {
        return (Map<String, String>)configInfoMap.get(CONFIG);
    }

    private Integer handleConfigRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            Integer brokerId = getBrokerIdFromPath(childData.getPath());
            configCache.remove(brokerId);
            return brokerId;
        } finally {
            lock.writeLock().unlock();
        }
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

    public static class BrokerConfig {

        public final Integer id;
        public final Map<String, String> config;

        public BrokerConfig(Integer id, Map<String, String> config) {
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
