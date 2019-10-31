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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache.StartMode;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent.Type;
import org.apache.curator.utils.ZKPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.kafkamanager.core.utils.CuratorUtils;

import kafka.zk.BrokerIdZNode;
import kafka.zk.BrokerIdsZNode;
import kafka.zk.BrokerInfo;

/**
 * @author Andrei_Tytsik
 */
class ZkBrokerCache {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZkBrokerCache.class);

    private static final String BROKERS_PATH = BrokerIdsZNode.path();

    private final PathChildrenCache brokerPathCache;

    private final CacheListener cacheListener;

    private final Map<Integer, BrokerInfo> brokerCache = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CountDownLatch initializedLatch = new CountDownLatch(1);

    public ZkBrokerCache(
            CuratorFramework curatorFramework,
            CacheListener cacheListener) {
        Validate.notNull(curatorFramework, "Curator framework can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        brokerPathCache = new PathChildrenCache(curatorFramework, BROKERS_PATH, true);

        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        brokerPathCache.getListenable().addListener((client, event) -> handlePathEvent(event));
        brokerPathCache.start(StartMode.POST_INITIALIZED_EVENT);
        awaitInitialization();

        LOGGER.info("Started");
    }

    public void close() throws IOException {
        brokerPathCache.close();

        LOGGER.info("Closed");
    }

    public int size() {
        lock.readLock().lock();
        try {
            return brokerCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Integer> listBrokerIds() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(brokerCache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(Integer brokerId) {
        lock.readLock().lock();
        try {
            return brokerCache.containsKey(brokerId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public BrokerInfo getBroker(Integer brokerId) {
        lock.readLock().lock();
        try {
            return brokerCache.get(brokerId);
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

        BrokerInfo updatedBroker = null;
        Integer idOfRemovedBroker = null;

        if (event.getType() == Type.INITIALIZED) {
            signalInitializationDone();
        } else if (event.getType() == Type.CHILD_ADDED || event.getType() == Type.CHILD_UPDATED) {
            updatedBroker = handleBrokerUpdated(event.getData());
        } else if (event.getType() == Type.CHILD_REMOVED) {
            idOfRemovedBroker = handleBrokerRemoved(event.getData());
        }

        clearDataQuietly(event.getData());

        fireCacheListener(updatedBroker, idOfRemovedBroker);
    }

    private void clearDataQuietly(ChildData childData) {
        try {
            brokerPathCache.clearDataBytes(childData.getPath());
        } catch (Exception ex) {
            // ignore
        }
    }

    private BrokerInfo handleBrokerUpdated(ChildData childData) {
        lock.writeLock().lock();
        try {
            BrokerInfo broker = toBroker(childData);
            brokerCache.put(broker.broker().id(), broker);
            return broker;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private Integer handleBrokerRemoved(ChildData childData) {
        lock.writeLock().lock();
        try {
            Integer brokerId = getBrokerIdFromPath(childData.getPath());
            brokerCache.remove(brokerId);
            return brokerId;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private BrokerInfo toBroker(ChildData childData) {
        int brokerId = getBrokerId(childData);
        return BrokerIdZNode.decode(brokerId, childData.getData());
    }

    private Integer getBrokerId(ChildData childData) {
        return getBrokerIdFromPath(childData.getPath());
    }

    private Integer getBrokerIdFromPath(String path) {
        return Integer.valueOf(ZKPaths.getNodeFromPath(path));
    }

    private void fireCacheListener(BrokerInfo updatedBroker, Integer idOfRemovedBroker) {
        if (updatedBroker != null) {
            try {
                cacheListener.onBrokerUpdated(updatedBroker);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'broker updated' event. Broker = %s",
                                updatedBroker),
                        ex);
            }
        }
        if (idOfRemovedBroker != null) {
            try {
                cacheListener.onBrokerRemoved(idOfRemovedBroker);
            } catch (Exception ex) {
                LOGGER.error(
                        String.format(
                                "Failed to handle 'broker removed' event. Broker id = %s",
                                idOfRemovedBroker),
                        ex);
            }
        }
    }

    public static interface CacheListener {
        void onBrokerUpdated(BrokerInfo broker);
        void onBrokerRemoved(Integer brokerId);
    }

}
