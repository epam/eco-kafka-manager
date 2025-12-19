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
package com.epam.eco.kafkamanager.core.consumer.repo.kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.concurrent.ResourceSemaphores;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.ConsumerGroupMemberInfo;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataKey;
import com.epam.eco.kafkamanager.ConsumerGroupRepo;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataKey;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.MetadataUpdateListener;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.OffsetAndMetadataInfo;
import com.epam.eco.kafkamanager.SearchCriteria;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.core.utils.ExceptionUtils;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import static java.util.Objects.nonNull;

/**
 * @author Andrei_Tytsik
 */
public class KafkaConsumerGroupRepo extends AbstractKeyValueRepo<String, ConsumerGroupInfo, SearchCriteria<ConsumerGroupInfo>> implements ConsumerGroupRepo, CachedRepo<String>, KafkaConsumerGroupCache.CacheListener, MetadataUpdateListener, AsyncStartingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerGroupRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;
    @Autowired
    private KafkaManagerProperties properties;
    @Autowired
    private MetadataRepo metadataRepo;

    private KafkaConsumerGroupCache groupCache;

    private final Map<String, ConsumerGroupInfo> groupInfoCache = new ConcurrentHashMap<>();

    private final ResourceSemaphores<String, ConsumerGroupOperation> semaphores = new ResourceSemaphores<>();

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
    private void destroy() {
        destroyGroupCache();

        LOGGER.info("Destroyed");
    }

    private void initGroupCache() {
        groupCache = new KafkaConsumerGroupCache(
                adminOperations,
                properties.getBootstrapServers(),
                properties.getClientConfig(),
                this);
    }

    private void startGroupCache() throws Exception {
        groupCache.start();
    }

    private void destroyGroupCache() {
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

        return adminOperations.consumerGroupExists(groupName);
    }

    @Override
    public ConsumerGroupInfo get(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        ConsumerGroupInfo groupInfo = fetchConsumerGroup(groupName);
        if (groupInfo == null) {
            removeFromCaches(groupName);
            throw new NotFoundException(String.format("Group not found by name '%s'", groupName));
        }
        groupInfoCache.put(groupName, groupInfo);
        return groupInfo;
    }

    private void removeFromCaches(String groupName) {
        removeGroupFromInfoCache(groupName);
        groupCache.removeGroup(groupName);
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
                groupNames, "Group name collection can't be null or contain null elements");

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
        return groupCache.listGroupNames().stream().
                sorted().
                collect(Collectors.toList());
    }

    @Override
    public List<ConsumerGroupInfo> groupsForTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        return values(
                groupCache.listGroupNamesOfTopic(topicName).stream().
                    sorted().
                    collect(Collectors.toList()));
    }

    @Override
    public ConsumerGroupInfo unassignGroupFromTopic(String groupName, String topicName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        ResourceSemaphores.ResourceSemaphore<String, ConsumerGroupOperation> semaphore = null;
        try {
            semaphore = groupCache.callInLock(() -> {
                ResourceSemaphores.ResourceSemaphore<String, ConsumerGroupOperation> deleteSemaphore =
                        semaphores.createSemaphore(groupName, ConsumerGroupOperation.DELETE);
                adminOperations.deleteConsumerGroup(groupName);
                return deleteSemaphore;
            });
            semaphore.awaitUnchecked();
        } finally {
            semaphores.removeSemaphore(semaphore);
        }
    }

    @Override
    public void evict(String groupName) {
        removeGroupFromInfoCache(groupName);
    }

    @Override
    public void onGroupMetadataUpdated(KafkaGroupMetadata groupMetadata) {
        Validate.notNull(groupMetadata, "Group metadata can't be null");

        removeGroupFromInfoCache(groupMetadata.getName());
    }

    @Override
    public void onGroupMetadataRemoved(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        semaphores.signalDoneFor(groupName, ConsumerGroupOperation.DELETE);
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
        Validate.notBlank(groupName, "Group name can't be null");

        groupInfoCache.remove(groupName);
    }

    private ConsumerGroupInfo getGroupFromInfoCacheOrCreate(String groupName) {

        return groupInfoCache.computeIfAbsent(
                groupName,
                key -> {
                    KafkaGroupMetadata groupMetadata = groupCache.getGroupMetadata(groupName);
                    return groupMetadata != null ? toConsumerGroupInfo(groupMetadata) :
                            fetchConsumerGroup(groupName);
                });
    }

    private ConsumerGroupInfo fetchConsumerGroup(String groupName) {
        ClientGroupMetadata clientGroupMetadata =
                ClientGroupMetadata.ofNullable(
                        ExceptionUtils.doQuietly(() -> adminOperations.describeConsumerGroup(groupName))
                );
        return buildConsumerGroupInfo(groupName, clientGroupMetadata);
    }

    @Nullable
    private ConsumerGroupInfo buildConsumerGroupInfo(
            String groupName,
            ClientGroupMetadata clientGroupMetadata
    ) {
        if (clientGroupMetadata == null) {
            return null;
        }
        Map<TopicPartition, OffsetAndMetadata> rawOffsetsMetadata =
                adminOperations.listConsumerGroupOffsets(groupName);

        Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata =
                rawOffsetsMetadata.entrySet().stream()
                .filter(offset -> nonNull(offset.getValue()))
                .collect(
                        Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> ClientOffsetAndMetadata.ofNullable(entry.getValue())));

        mergeOffsetsWithCachedData(groupName, offsetsMetadata);

        KafkaGroupMetadata kafkaGroupMetadata = new KafkaGroupMetadata(groupName);
        kafkaGroupMetadata.setGroupMetadata(clientGroupMetadata);
        kafkaGroupMetadata.setOffsetsMetadata(offsetsMetadata);
        return toConsumerGroupInfo(kafkaGroupMetadata);
    }


    /**
     * Merges the latest offset data from adminClient with particular cached offset data
     * provided from consumer_offsets topic because adminClient doesn't provide full set of
     * the offset metadata properties.
     * This ensures that we maintain the most up-to-date offset information while preserving
     * any additional metadata that might be present in the cached data.
     * @param groupName The group name
     * @param offsetsMetadata The current offsets and metadata fetched by adminClient
     */
    private void mergeOffsetsWithCachedData(
            String groupName,
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata
    ) {
        KafkaGroupMetadata cachedKafkaGroupMetadata = groupCache.getGroupMetadata(groupName);
        if (cachedKafkaGroupMetadata != null) {
            Map<TopicPartition, OffsetAndMetadataAdapter> cachedOffsets =
                    cachedKafkaGroupMetadata.getOffsetsMetadata();
            cachedOffsets.forEach((topic, cachedOffset) -> {
                if (offsetsMetadata.containsKey(topic)) {
                    OffsetAndMetadataAdapter offsetAndMetadata = offsetsMetadata.get(topic);
                    if (cachedOffset.getOffset() == offsetAndMetadata.getOffset()) {
                        offsetsMetadata.put(topic, cachedOffset);
                    }
                }
            });
        }
    }

    private ConsumerGroupInfo toConsumerGroupInfo(KafkaGroupMetadata metadata) {
        String groupName = metadata.getName();
        GroupMetadataAdapter groupMetadata = metadata.getGroupMetadata();
        List<ConsumerGroupMemberInfo> memberInfos = toMemberInfos(groupMetadata);
        Map<TopicPartition, OffsetAndMetadataInfo> offsetAndMetadataInfos =
                toOffsetAndMetadataInfos(metadata.getOffsetsMetadata());
        return ConsumerGroupInfo.builder().
                name(groupName).
                coordinator(groupMetadata.getCoordinator()).
                state(metadata.getGroupState()).
                protocolType(groupMetadata.getProtocolType()).
                partitionAssignor(groupMetadata.getPartitionAssignor()).
                members(memberInfos).
                offsetsAndMetadata(offsetAndMetadataInfos).
                offsetTimeSeries(groupCache.getOffsetTimeSeries(groupName)).
                storageType(StorageType.KAFKA).
                metadata(metadataRepo.get(ConsumerGroupMetadataKey.with(groupName))).
                build();
    }

    private static List<ConsumerGroupMemberInfo> toMemberInfos(GroupMetadataAdapter groupMetadata) {
        Collection<MemberMetadataAdapter> members = groupMetadata.getMembers();
        if (CollectionUtils.isEmpty(members)) {
            return Collections.emptyList();
        }

        return members.stream().
                map(metadata -> ConsumerGroupMemberInfo.builder().
                        clientId(metadata.getClientId()).
                        memberId(metadata.getMemberId()).
                        clientHost(metadata.getClientHost()).
                        rebalanceTimeoutMs(metadata.getRebalanceTimeoutMs()).
                        sessionTimeoutMs(metadata.getSessionTimeoutMs()).
                        assignment(metadata.getAssignment()).
                        build()).
                sorted().
                collect(Collectors.toList());
    }

    private static Map<TopicPartition, OffsetAndMetadataInfo> toOffsetAndMetadataInfos(
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsAndMetadata) {
        if (MapUtils.isEmpty(offsetsAndMetadata)) {
            return Collections.emptyMap();
        }

        Map<TopicPartition, OffsetAndMetadataInfo> offsetAndMetadataInfos = new HashMap<>();
        offsetsAndMetadata.forEach((topicPartition,offsetAndMetadata) -> {
            OffsetAndMetadataInfo offsetAndMetadataInfo = OffsetAndMetadataInfo.builder().
                    topicPartition(topicPartition).
                    offset(offsetAndMetadata.getOffset()).
                    metadata(offsetAndMetadata.getMetadata()).
                    commitDate(offsetAndMetadata.getCommitTimestamp()).
                    expireDate(offsetAndMetadata.getExpireTimestamp()).
                    build();
            offsetAndMetadataInfos.put(topicPartition, offsetAndMetadataInfo);
        });
        return offsetAndMetadataInfos;
    }

}
