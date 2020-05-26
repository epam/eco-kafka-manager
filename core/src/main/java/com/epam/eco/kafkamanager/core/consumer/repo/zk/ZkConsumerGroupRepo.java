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
package com.epam.eco.kafkamanager.core.consumer.repo.zk;

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
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.ConsumerGroupMemberInfo;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataKey;
import com.epam.eco.kafkamanager.ConsumerGroupRepo;
import com.epam.eco.kafkamanager.ConsumerGroupSearchCriteria;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.OffsetAndMetadataInfo;
import com.epam.eco.kafkamanager.core.consumer.repo.zk.ZkConsumerGroupCache.ConsumerGroup;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

/**
 * @author Andrei_Tytsik
 */
public class ZkConsumerGroupRepo extends AbstractKeyValueRepo<String, ConsumerGroupInfo, ConsumerGroupSearchCriteria> implements ConsumerGroupRepo, CachedRepo<String>, ZkConsumerGroupCache.CacheListener, MetadataUpdateListener, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(ZkConsumerGroupRepo.class);

    @Autowired
    private MetadataRepo metadataRepo;
    @Autowired
    private CuratorFramework curatorFramework;

    private ZkConsumerGroupCache groupCache;

    private final Map<String, ConsumerGroupInfo> groupInfoCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        initGroupCache();
        subscribeOnMetadataUpdates();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startGroupCache();

        LOGGER.info("Started");
    }

    @PreDestroy
    private void destroy() throws Exception {
        destroyGroupCache();

        LOGGER.info("Destroyed");
    }

    private void initGroupCache() {
        groupCache = new ZkConsumerGroupCache(curatorFramework, this);
    }

    private void startGroupCache() throws Exception {
        groupCache.start();
    }

    private void destroyGroupCache() throws Exception {
        groupCache.close();
    }

    private void subscribeOnMetadataUpdates() {
        metadataRepo.registerUpdateListener(this);
    }

    @Override
    public int size() {
        return groupCache.size();
    }

    @Override
    public boolean contains(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        return groupCache.contains(groupName);
    }

    @Override
    public ConsumerGroupInfo get(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        ConsumerGroupInfo groupInfo = getGroupFromInfoCacheOrCreate(groupName);
        if (groupInfo == null) {
            throw new NotFoundException(String.format("Consumer group not found by name '%s'", groupName));
        }

        return groupInfo;
    }

    @Override
    public List<ConsumerGroupInfo> values() {
        List<ConsumerGroupInfo> groupInfos = new ArrayList<>();
        groupCache.listGroupNames().forEach(groupName -> {
            ConsumerGroupInfo groupInfo = getGroupFromInfoCacheOrCreate(groupName);
            if (groupInfo != null) {
                groupInfos.add(groupInfo);
            }
        });
        Collections.sort(groupInfos);
        return groupInfos;
    }

    @Override
    public List<ConsumerGroupInfo> values(List<String> groupNames) {
        Validate.noNullElements(
                groupNames, "Collection of group names can't be null or contain null elements");

        List<ConsumerGroupInfo> groupInfos = new ArrayList<>();
        groupNames.forEach(groupName -> {
            ConsumerGroupInfo groupInfo = getGroupFromInfoCacheOrCreate(groupName);
            if (groupInfo != null) {
                groupInfos.add(groupInfo);
            }
        });
        Collections.sort(groupInfos);
        return groupInfos;
    }

    @Override
    public List<String> keys() {
        return groupCache.listGroupNames().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<ConsumerGroupInfo> groupsForTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        return values(groupCache.getGroupsByTopicAsList(topicName));
    }

    @Override
    public ConsumerGroupInfo unassignGroupFromTopic(String groupName, String topicName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void evict(String groupName) {
        removeGroupFromInfoCache(groupName);
    }

    @Override
    public void onGroupUpdated(ConsumerGroup group) {
        Validate.notNull(group, "Group is null");

        removeGroupFromInfoCache(group.name);
    }

    @Override
    public void onGroupRemoved(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        removeGroupFromInfoCache(groupName);
    }

    @Override
    public void onMetadataUpdated(MetadataKey key, Metadata metadata) {
        Validate.notNull(key, "Metadata key is null");
        Validate.notNull(metadata, "Metadata is null");

        if (key.getEntityType() != EntityType.CONSUMER_GROUP) {
            return;
        }

        removeGroupFromInfoCache(((ConsumerGroupMetadataKey)key).getGroupName());
    }

    @Override
    public void onMetadataRemoved(MetadataKey key) {
        Validate.notNull(key, "Metadata key is null");

        if (key.getEntityType() != EntityType.CONSUMER_GROUP) {
            return;
        }

        removeGroupFromInfoCache(((ConsumerGroupMetadataKey)key).getGroupName());
    }

    private void removeGroupFromInfoCache(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        groupInfoCache.remove(groupName);
    }

    private ConsumerGroupInfo getGroupFromInfoCacheOrCreate(String groupName) {
        return groupInfoCache.computeIfAbsent(
                groupName,
                key -> {
                    ConsumerGroup group = groupCache.getGroup(groupName);
                    return group != null ? toInfo(group) : null;
                });
    }

    private ConsumerGroupInfo toInfo(ConsumerGroup group) {
        List<ConsumerGroupMemberInfo> memberInfos = group.members.stream().
                map(member -> ConsumerGroupMemberInfo.builder().
                        clientId(member).
                        build()).
                collect(Collectors.toList());
        Map<TopicPartition, OffsetAndMetadataInfo> offsetAndMetadataInfos =
                group.offsets.entrySet().stream().
                collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> OffsetAndMetadataInfo.builder().
                                        topicPartition(entry.getKey()).
                                        offset(entry.getValue()).
                                        build()));
        return ConsumerGroupInfo.builder().
                name(group.name).
                offsetsAndMetadata(offsetAndMetadataInfos).
                members(memberInfos).
                storageType(StorageType.ZOOKEEPER).
                metadata(metadataRepo.get(ConsumerGroupMetadataKey.with(group.name))).
                build();
    }

}
