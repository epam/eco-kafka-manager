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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.concurrent.ResourceSemaphores;
import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataKey;
import com.epam.eco.kafkamanager.BrokerRepo;
import com.epam.eco.kafkamanager.BrokerSearchQuery;
import com.epam.eco.kafkamanager.EndPointInfo;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import kafka.cluster.EndPoint;


/**
 * @author Andrei_Tytsik
 */
public class ZkBrokerRepo extends AbstractKeyValueRepo<Integer, BrokerInfo, BrokerSearchQuery> implements BrokerRepo, CachedRepo<Integer>, ZkBrokerCache.CacheListener, ZkBrokerConfigCache.CacheListener, MetadataUpdateListener, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZkBrokerRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private MetadataRepo metadataRepo;
    @Autowired
    private CuratorFramework curatorFramework;

    private ZkBrokerCache brokerCache;
    private ZkBrokerConfigCache brokerConfigCache;

    private final Map<Integer, BrokerInfo> brokerInfoCache = new ConcurrentHashMap<>();

    private final ResourceSemaphores<Integer, BrokerOperation> semaphores = new ResourceSemaphores<>();

    @PostConstruct
    private void init() {
        initBrokerCache();
        initBrokerConfigCache();
        subscribeOnMetadataUpdates();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startBrokerCache();
        startBrokerConfigCache();

        LOGGER.info("Started");
    }

    @PreDestroy
    private void destroy() throws Exception {
        destroyBrokeCache();
        destroyBrokerConfigCache();

        LOGGER.info("Destroyed");
    }

    private void initBrokerCache() {
        brokerCache = new ZkBrokerCache(curatorFramework, this);
    }

    private void initBrokerConfigCache() {
        brokerConfigCache = new ZkBrokerConfigCache(curatorFramework, this);
    }

    private void startBrokerConfigCache() throws Exception {
        brokerConfigCache.start();
    }

    private void destroyBrokerConfigCache() throws Exception {
        brokerConfigCache.close();
    }

    private void startBrokerCache() throws Exception {
        brokerCache.start();
    }

    private void destroyBrokeCache() throws Exception {
        brokerCache.close();
    }

    private void subscribeOnMetadataUpdates() {
        metadataRepo.registerUpdateListener(this);
    }

    @Override
    public int size() {
        return brokerCache.size();
    }

    @Override
    public boolean contains(Integer brokerId) {
        Validate.notNull(brokerId, "Broker id is null");
        Validate.isTrue(brokerId >= 0, "Broker id is invalid");

        return brokerCache.contains(brokerId);
    }

    @Override
    public BrokerInfo get(Integer brokerId) {
        Validate.notNull(brokerId, "Broker id is null");
        Validate.isTrue(brokerId >= 0, "Broker id is invalid");

        BrokerInfo brokerInfo = getBrokerFromInfoCacheOrCreate(brokerId);
        if (brokerInfo == null) {
            throw new NotFoundException(String.format("Broker not found by id %d", brokerId));
        }

        return brokerInfo;
    }

    @Override
    public List<BrokerInfo> values() {
        List<BrokerInfo> brokerInfos = new ArrayList<>();
        brokerCache.listBrokerIds().forEach(brokerId -> {
            BrokerInfo brokerInfo = getBrokerFromInfoCacheOrCreate(brokerId);
            if (brokerInfo != null) {
                brokerInfos.add(brokerInfo);
            }
        });
        Collections.sort(brokerInfos);
        return brokerInfos;
    }

    @Override
    public List<BrokerInfo> values(List<Integer> brokerIds) {
        Validate.noNullElements(
                brokerIds, "Collection of broker ids can't be null or contain null elements");

        List<BrokerInfo> brokerInfos = new ArrayList<>();
        brokerIds.forEach(brokerId -> {
            BrokerInfo brokerInfo = getBrokerFromInfoCacheOrCreate(brokerId);
            if (brokerInfo != null) {
                brokerInfos.add(brokerInfo);
            }
        });
        Collections.sort(brokerInfos);
        return brokerInfos;
    }

    @Override
    public List<Integer> keys() {
        return new ArrayList<>(brokerCache.listBrokerIds());
    }

    @Override
    public void evict(Integer brokerId) {
        removeBrokerFromInfoCache(brokerId);
    }

    @Override
    public BrokerInfo updateConfig(int brokerId, Map<String, String> configs) {
        Validate.isTrue(brokerId >= 0, "Broker id is invalid: %d", brokerId);
        Validate.notNull(configs, "Map of configs is null");

        ResourceSemaphores.ResourceSemaphore<Integer, BrokerOperation> semaphore = null;
        try {
            semaphore = brokerCache.callIfBrokerPresentOrElseThrow(
                    brokerId,
                    broker -> {
                        ResourceSemaphores.ResourceSemaphore<Integer, BrokerOperation> updateSemaphore =
                                semaphores.createSemaphore(brokerId, BrokerOperation.UPDATE);

                        adminOperations.alterBrokerConfigs(brokerId, configs);

                        return updateSemaphore;
                        },
                    () -> new NotFoundException(String.format("Broker %d doesn't exist", brokerId)));

            semaphore.awaitUnchecked();

            return get(brokerId);
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void onBrokerConfigUpdated(ZkBrokerConfigCache.BrokerConfig brokerConfig) {
        Validate.notNull(brokerConfig, "Broker config can't be null");

        semaphores.signalDoneFor(brokerConfig.id, BrokerOperation.UPDATE);

        removeBrokerFromInfoCache(brokerConfig.id);
    }

    @Override
    public void onBrokerConfigRemoved(int brokerId) {
        Validate.isTrue(brokerId >= 0, "Broker id '%d' is invalid", brokerId);

        semaphores.signalDoneFor(brokerId, BrokerOperation.UPDATE);

        removeBrokerFromInfoCache(brokerId);
    }

    @Override
    public void onBrokerUpdated(kafka.zk.BrokerInfo broker) {
        Validate.notNull(broker, "Broker is null");

        removeBrokerFromInfoCache(broker.broker().id());
    }

    @Override
    public void onBrokerRemoved(Integer brokerId) {
        removeBrokerFromInfoCache(brokerId);
    }

    @Override
    public void onMetadataUpdated(MetadataKey key, Metadata metadata) {
        Validate.notNull(key, "Metadata key is null");
        Validate.notNull(metadata, "Metadata is null");

        if (key.getEntityType() != EntityType.BROKER) {
            return;
        }

        removeBrokerFromInfoCache(((BrokerMetadataKey)key).getBrokerId());
    }

    @Override
    public void onMetadataRemoved(MetadataKey key) {
        Validate.notNull(key, "Metadata key is null");

        if (key.getEntityType() != EntityType.BROKER) {
            return;
        }

        removeBrokerFromInfoCache(((BrokerMetadataKey)key).getBrokerId());
    }

    private void removeBrokerFromInfoCache(Integer brokerId) {
        Validate.notNull(brokerId, "Broker id is null");

        brokerInfoCache.remove(brokerId);
    }

    private BrokerInfo getBrokerFromInfoCacheOrCreate(Integer brokerId) {
        return brokerInfoCache.computeIfAbsent(
                brokerId,
                key -> {
                    kafka.zk.BrokerInfo broker = brokerCache.getBroker(brokerId);
                    if (broker != null) {
                        ZkBrokerConfigCache.BrokerConfig brokerConfig = brokerConfigCache.getConfig(brokerId);
                        return toInfo(broker, brokerConfig);
                    }
                    return null;
                });
    }

    private BrokerInfo toInfo(kafka.zk.BrokerInfo broker, ZkBrokerConfigCache.BrokerConfig config) {
        return BrokerInfo.builder().
                id(broker.broker().id()).
                endPoints(toEndPointInfo(ScalaConversions.asJavaList(broker.broker().endPoints()))).
                rack(ScalaConversions.asOptional(broker.broker().rack()).orElse(null)).
                version(broker.version()).
                jmxPort(broker.jmxPort()).
                config(config != null ? config.config : null).
                metadata(metadataRepo.get(BrokerMetadataKey.with(broker.broker().id()))).
                build();
    }

    private List<EndPointInfo> toEndPointInfo(List<EndPoint> endPoints) {
        return endPoints.stream().
                map(endPoint -> new EndPointInfo(
                        endPoint.securityProtocol(),
                        endPoint.host(),
                        endPoint.port())).
                collect(Collectors.toList());

    }

}
