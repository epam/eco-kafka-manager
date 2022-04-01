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
package com.epam.eco.kafkamanager.core.topic.repo.zk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.apache.curator.framework.CuratorFramework;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.concurrent.ResourceSemaphores;
import com.epam.eco.kafkamanager.AlreadyExistsException;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.PartitionInfo;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataKey;
import com.epam.eco.kafkamanager.TopicRepo;
import com.epam.eco.kafkamanager.TopicSearchCriteria;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.core.topic.repo.zk.ZkTopicCache.PartitionMetadata;
import com.epam.eco.kafkamanager.core.topic.repo.zk.ZkTopicCache.PartitionState;
import com.epam.eco.kafkamanager.core.topic.repo.zk.ZkTopicCache.Topic;
import com.epam.eco.kafkamanager.core.topic.repo.zk.ZkTopicConfigCache.TopicConfig;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

/**
 * @author Andrei_Tytsik
 */
public class ZkTopicRepo extends AbstractKeyValueRepo<String, TopicInfo, TopicSearchCriteria> implements TopicRepo, CachedRepo<String>, ZkTopicCache.CacheListener, ZkTopicConfigCache.CacheListener, MetadataUpdateListener, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZkTopicRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private MetadataRepo metadataRepo;
    @Autowired
    private CuratorFramework curatorFramework;

    private ZkTopicCache topicCache;
    private ZkTopicConfigCache topicConfigCache;

    private final Map<String, TopicInfo> topicInfoCache = new ConcurrentHashMap<>();

    private final ResourceSemaphores<String, TopicOperation> semaphores = new ResourceSemaphores<>();

    @PostConstruct
    private void init() {
        initTopicCache();
        initTopicConfigCache();
        subscribeOnMetadataUpdates();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startTopicCache();
        startTopicConfigCache();

        LOGGER.info("Started");
    }

    @PreDestroy
    private void destroy() throws Exception {
        destroyTopicCache();
        destroyTopicConfigCache();

        LOGGER.info("Destroyed");
    }

    private void initTopicCache() {
        topicCache = new ZkTopicCache(curatorFramework, this);
    }

    private void startTopicCache() throws Exception {
        topicCache.start();
    }

    private void destroyTopicCache() throws Exception {
        topicCache.close();
    }

    private void initTopicConfigCache() {
        topicConfigCache = new ZkTopicConfigCache(curatorFramework, this);
    }

    private void startTopicConfigCache() throws Exception {
        topicConfigCache.start();
    }

    private void destroyTopicConfigCache() throws Exception {
        topicConfigCache.close();
    }

    private void subscribeOnMetadataUpdates() {
        metadataRepo.registerUpdateListener(this);
    }

    @Override
    public int size() {
        return topicCache.size();
    }

    @Override
    public boolean contains(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        return topicCache.contains(topicName);
    }

    @Override
    public TopicInfo get(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        TopicInfo topicInfo = getTopicFromInfoCacheOrCreate(topicName);
        if (topicInfo == null) {
            throw new NotFoundException(String.format("Topic not found by name '%s'", topicName));
        }

        return topicInfo;
    }

    @Override
    public List<TopicInfo> values() {
        List<TopicInfo> topicInfos = new ArrayList<>();
        topicCache.listTopicNames().forEach(topicName -> {
            TopicInfo topicInfo = getTopicFromInfoCacheOrCreate(topicName);
            if (topicInfo != null) {
                topicInfos.add(topicInfo);
            }
        });
        Collections.sort(topicInfos);
        return topicInfos;
    }

    @Override
    public List<TopicInfo> values(List<String> topicNames) {
        Validate.notNull(topicNames, "Topic names list is null");
        Validate.noNullElements(topicNames, "Topic names list has null elements");

        List<TopicInfo> topicInfos = new ArrayList<>();
        topicNames.forEach(topicName -> {
            TopicInfo topicInfo = getTopicFromInfoCacheOrCreate(topicName);
            if (topicInfo != null) {
                topicInfos.add(topicInfo);
            }
        });
        Collections.sort(topicInfos);
        return topicInfos;
    }

    @Override
    public List<String> keys() {
        return topicCache.listTopicNames().stream().
                sorted().
                collect(Collectors.toList());
    }

    @Override
    public TopicInfo create(
            String topicName,
            int partitionCount,
            int replicationFactor,
            Map<String, String> config) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.isTrue(partitionCount > 0, "Partition count is invalid");
        Validate.isTrue(replicationFactor > 0, "Replication factor is invalid");

        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> semaphore = null;
        try {
            semaphore = topicCache.callIfTopicAbsentOrElseThrow(
                    topicName,
                    () -> {
                        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> updateSemaphore =
                                semaphores.createSemaphore(topicName, TopicOperation.UPDATE);

                        adminOperations.createTopic(
                            topicName,
                            partitionCount,
                            replicationFactor,
                            config);

                        return updateSemaphore;
                        },
                    () -> new AlreadyExistsException(String.format("Topic '%s' already exists", topicName)));

            semaphore.awaitUnchecked();

            return get(topicName);
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public TopicInfo updateConfig(String topicName, Map<String, String> configs) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.notNull(configs, "Map of configs is null");

        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> semaphore = null;
        try {
            semaphore = topicCache.callIfTopicPresentOrElseThrow(
                    topicName,
                    topic -> {
                        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> updateSemaphore =
                                semaphores.createSemaphore(topicName, TopicOperation.CONFIG_UPDATE);

                        adminOperations.alterTopicConfigs(topicName, configs);

                        return updateSemaphore;
                        },
                    () -> new NotFoundException(String.format("Topic '%s' doesn't exist", topicName)));

            semaphore.awaitUnchecked();

            return get(topicName);
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public TopicInfo createPartitions(String topicName, int newPartitionCount) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.isTrue(newPartitionCount > 0, "Partition count is invalid");

        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> semaphore = null;
        try {
            semaphore = topicCache.callIfTopicPresentOrElseThrow(
                    topicName,
                    topic -> {
                        Validate.isTrue(newPartitionCount > topic.partitions.size(), String.format(
                                "New partition count (%d) for topic '%s' is less or equal than current one (%d)",
                                newPartitionCount, topicName, topic.partitions.size()));

                        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> updateSemaphore =
                                semaphores.createSemaphore(topicName, TopicOperation.UPDATE);

                        adminOperations.createPartitions(topicName, newPartitionCount);

                        return updateSemaphore;
                    },
                    () -> new NotFoundException(String.format("Topic '%s' doesn't exist", topicName)));

            semaphore.awaitUnchecked();

            return get(topicName);
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void delete(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> semaphore = null;
        try {
            semaphore = topicCache.callIfTopicPresentOrElseThrow(
                    topicName,
                    topic -> {
                        ResourceSemaphores.ResourceSemaphore<String, TopicOperation> deleteSemaphore =
                                semaphores.createSemaphore(topicName, TopicOperation.DELETE);

                        adminOperations.deleteTopic(topicName);

                        return deleteSemaphore;
                    },
                    () -> new NotFoundException(String.format("Topic '%s' doesn't exist", topicName)));

            semaphore.awaitUnchecked();
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void evict(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        removeTopicFromInfoCache(topicName);
    }

    @Override
    public void onTopicConfigUpdated(TopicConfig topicConfig) {
        Validate.notNull(topicConfig, "Topic config can't be null");

        semaphores.signalDoneFor(topicConfig.name, TopicOperation.CONFIG_UPDATE);

        removeTopicFromInfoCache(topicConfig.name);
    }

    @Override
    public void onTopicConfigRemoved(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        semaphores.signalDoneFor(topicName, TopicOperation.CONFIG_DELETE);

        removeTopicFromInfoCache(topicName);
    }

    @Override
    public void onTopicUpdated(Topic topic) {
        Validate.notNull(topic, "Topic can't be null");

        semaphores.signalDoneFor(topic.name, TopicOperation.UPDATE);

        removeTopicFromInfoCache(topic.name);
    }

    @Override
    public void onTopicRemoved(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        semaphores.signalDoneFor(topicName, TopicOperation.DELETE);

        removeTopicFromInfoCache(topicName);
    }

    @Override
    public void onMetadataUpdated(MetadataKey key, Metadata metadata) {
        Validate.notNull(key, "Metadata key is null");
        Validate.notNull(metadata, "Metadata is null");

        if (key.getEntityType() != EntityType.TOPIC) {
            return;
        }

        removeTopicFromInfoCache(((TopicMetadataKey)key).getTopicName());
    }

    @Override
    public void onMetadataRemoved(MetadataKey key) {
        Validate.notNull(key, "Metadata key is null");

        if (key.getEntityType() != EntityType.TOPIC) {
            return;
        }

        removeTopicFromInfoCache(((TopicMetadataKey)key).getTopicName());
    }

    private void removeTopicFromInfoCache(String topicName) {
        topicInfoCache.remove(topicName);
    }

    private TopicInfo getTopicFromInfoCacheOrCreate(String topicName) {
        return topicInfoCache.computeIfAbsent(
                topicName,
                key -> {
                    Topic topic = topicCache.getTopic(topicName);
                    if (topic != null) {
                        TopicConfig topicConfig = topicConfigCache.getConfig(topicName);
                        return toInfo(topic, topicConfig);
                    }
                    return null;
                });
    }

    private TopicInfo toInfo(Topic topic, TopicConfig config) {
        return TopicInfo.builder().
                name(topic.name).
                partitions(toPartitions(topic)).
                config(config != null ? config.config : null).
                metadata(metadataRepo.get(TopicMetadataKey.with(topic.name))).
                build();
    }

    private Map<TopicPartition, PartitionInfo> toPartitions(Topic topic) {
        Map<TopicPartition, PartitionInfo> partitions = new HashMap<>();
        topic.partitions.keySet().forEach((id) -> {
            PartitionMetadata metadata = topic.partitions.get(id);
            PartitionState state = topic.states.get(id);
            partitions.put(
                    id,
                    PartitionInfo.builder().
                        id(id).
                        replicas(metadata.replicas).
                        leader(state != null ? state.leader : null).
                        isr(state != null ? state.isr: null).
                        build());
        });
        return partitions;
    }

}
