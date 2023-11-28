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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.cache.KafkaCache;
import com.epam.eco.commons.kafka.consumer.bootstrap.EndOffsetInitializer;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.OffsetTimeSeries;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.BaseKey;
import kafka.coordinator.group.GroupMetadata;
import kafka.coordinator.group.GroupMetadataKey;
import kafka.coordinator.group.OffsetKey;

import static java.util.Objects.nonNull;

/**
 * @author Andrei_Tytsik
 */
class KafkaConsumerGroupCache implements com.epam.eco.commons.kafka.cache.CacheListener<BaseKey, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerGroupCache.class);

    private final KafkaAdminOperations adminOperations;

    private final KafkaCache<BaseKey, Object> metadataCache;

    private final Map<String, KafkaGroupMetadata> groupCache = new HashMap<>();
    private final Map<String, Set<String>> topicGroups = new HashMap<>();
    private final Map<String, Map<TopicPartition, OffsetTimeSeries>> offsetTimeSeries =
            new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public KafkaConsumerGroupCache(
            KafkaAdminOperations adminOperations,
            String bootstrapServers,
            Map<String, Object> consumerConfig,
            CacheListener cacheListener) {
        Validate.notNull(adminOperations, "Admin Operations can't be null");
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        this.adminOperations = adminOperations;
        this.metadataCache = KafkaCache.<BaseKey, Object>builder().
                bootstrapServers(bootstrapServers).
                topicName(Topic.GROUP_METADATA_TOPIC_NAME).
                bootstrapTimeoutInMs(1).
                consumerConfig(consumerConfig).
                offsetInitializer(EndOffsetInitializer.INSTANCE).
                keyValueDecoder(new ServerGroupMetadataDecoder()).
                consumerParallelismAvailableCores().
                storeData(false).
                listener(this).
                build();
        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        bootstrapInitialData();
        startMetadataCache();

        LOGGER.info("Started");
    }

    public void close()  {
        destroyMetadataCache();

        LOGGER.info("Closed");
    }

    public int size() {
        lock.readLock().lock();
        try {
            return groupCache.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> listGroupNames() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(groupCache.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean contains(String groupName) {
        lock.readLock().lock();
        try {
            return groupCache.containsKey(groupName);
        } finally {
            lock.readLock().unlock();
        }
    }

    public KafkaGroupMetadata getGroupMetadata(String groupName) {
        lock.readLock().lock();
        try {
            KafkaGroupMetadata metadata = groupCache.get(groupName);
            return metadata != null ? metadata.copyOf() : null;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> listGroupNamesOfTopic(String topicName) {
        lock.readLock().lock();
        try {
            Set<String> groupNames = topicGroups.get(topicName);
            return groupNames != null ? new ArrayList<>(groupNames) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public <T> T callInLock(Callable<T> callable) {
        Validate.notNull(callable, "Callable can't be null");

        lock.readLock().lock();
        try {
            try {
                return callable.call();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } finally {
            lock.readLock().unlock();
        }
    }

    public Map<TopicPartition, OffsetTimeSeries> getOffsetTimeSeries(String groupName) {
        lock.readLock().lock();
        try {
            Map<TopicPartition, OffsetTimeSeries> groupTimeSeries = offsetTimeSeries.get(groupName);
            return
                    groupTimeSeries != null ?
                    groupTimeSeries.entrySet().stream().collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().unmodifiableCopy())) :
                    Collections.emptyMap();
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void onCacheUpdated(Map<BaseKey, Object> cacheUpdate) {
        if (MapUtils.isEmpty(cacheUpdate)) {
            return;
        }

        Map<String, GroupMetadataAdapter> groupUpdates = new HashMap<>();
        Map<String, Map<TopicPartition, OffsetAndMetadataAdapter>> offsetUpdates = new HashMap<>();

        cacheUpdate.forEach((key, value) -> {
            if (key instanceof GroupMetadataKey groupMetadataKey) {
                GroupMetadataAdapter groupMetadata =
                        ServerGroupMetadata.ofNullable((GroupMetadata)value);

                String groupName = groupMetadataKey.key();
                groupUpdates.put(groupName, groupMetadata);
            } else if (key instanceof OffsetKey offsetKey) {
                OffsetAndMetadataAdapter offsetAndMetadata =
                        ServerOffsetAndMetadata.ofNullable((OffsetAndMetadata)value);

                String groupName = offsetKey.key().group();
                TopicPartition topicPartition = offsetKey.key().topicPartition();

                Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata =
                        offsetUpdates.computeIfAbsent(
                                groupName,
                                k -> new HashMap<>());

                offsetsMetadata.put(topicPartition, offsetAndMetadata);
            } else {
                LOGGER.warn("Ignoring unsupported group metadata record: key={}, value={}", key, value);
            }
        });

        applyUpdates(groupUpdates, offsetUpdates, UpdateMode.UPDATE);
    }

    private void bootstrapInitialData() {
        Map<String, GroupMetadataAdapter> groupUpdates =
                toClientGroupMetadataUpdates(adminOperations.describeAllConsumerGroups());
        if (MapUtils.isEmpty(groupUpdates)) {
            return;
        }

        Map<String, Map<TopicPartition, OffsetAndMetadataAdapter>> offsetUpdates =
                toClientOffsetMetadataUpdates(adminOperations.listAllConsumerGroupOffsets());

        applyUpdates(groupUpdates, offsetUpdates, UpdateMode.SET);
    }

    private void startMetadataCache() throws Exception {
        metadataCache.start();
    }

    private void destroyMetadataCache() {
        metadataCache.close();
    }

    private void applyUpdates(
            Map<String, GroupMetadataAdapter> groupUpdates,
            Map<String, Map<TopicPartition, OffsetAndMetadataAdapter>> offsetUpdates,
            UpdateMode mode) {
        if (MapUtils.isEmpty(groupUpdates) && MapUtils.isEmpty(offsetUpdates)) {
            return;
        }

        Map<String, KafkaGroupMetadata> effectiveUpdate = new HashMap<>();

        lock.writeLock().lock();
        try {
            if (!MapUtils.isEmpty(groupUpdates)) {
                groupUpdates.forEach((groupName, update) -> {
                    try {
                        KafkaGroupMetadata groupMetadata = getGroupMetadata(groupName, update != null);
                        if (groupMetadata != null) {
                            groupMetadata.setGroupMetadata(update);

                            boolean removed = removeGroupMetadataIfInvalid(groupMetadata);
                            if (removed) {
                                effectiveUpdate.put(groupMetadata.getName(), null);
                            } else {
                                effectiveUpdate.put(groupMetadata.getName(), groupMetadata);
                            }
                        }
                    } catch (Exception ex) {
                        LOGGER.error(
                                String.format(
                                        "Failed to apply group metadata update: group=%s, update=%s",
                                        groupName, update),
                                ex);
                    }
                });
            }
            if (!MapUtils.isEmpty(offsetUpdates)) {
                offsetUpdates.forEach((groupName, update) -> {
                    try {
                        KafkaGroupMetadata groupMetadata = getGroupMetadata(groupName, false);
                        if (groupMetadata == null && isCleanUpUpdate(update)) {
                            return;
                        }
                        if (groupMetadata == null) {
                            throw new RuntimeException("Group not found");
                        }

                        if (mode == UpdateMode.SET) {
                            groupMetadata.setOffsetsMetadata(update);
                            setTopicGroups(groupName, update);
                            setOffsetTimeSeries(groupName, update);
                        } else if (mode == UpdateMode.UPDATE) {
                            groupMetadata.updateOffsetsMetadata(update);
                            updateTopicGroups(groupName, update);
                            updateOffsetTimeSeries(groupName, update);
                        } else {
                            throw new IllegalArgumentException("Unknown update mode " + mode);
                        }

                        effectiveUpdate.put(groupMetadata.getName(), groupMetadata);
                    } catch (Exception ex) {
                        LOGGER.error(
                                String.format(
                                        "Failed to apply offset metadata update: group=%s, update=%s",
                                        groupName, update),
                                ex);
                    }
                });
            }
        } finally {
            lock.writeLock().unlock();
        }

        fireCacheListener(effectiveUpdate);
    }

    private KafkaGroupMetadata getGroupMetadata(String groupName, boolean createIfAbsent) {
        KafkaGroupMetadata groupMetadata = groupCache.get(groupName);
        if (groupMetadata == null && createIfAbsent) {
            groupMetadata = new KafkaGroupMetadata(groupName);
            groupCache.put(groupName, groupMetadata);
        }
        return groupMetadata;
    }

    private boolean removeGroupMetadataIfInvalid(KafkaGroupMetadata groupMetadata) {
        if (groupMetadata.isValid()) {
            return false;
        }

        return groupCache.remove(groupMetadata.getName()) != null;
    }

    private void setTopicGroups(
            String groupName,
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata) {
        if (MapUtils.isEmpty(offsetsMetadata)) {
            return;
        }

        Set<String> topicNames = offsetsMetadata.entrySet().stream().
                filter(entry -> entry.getValue() != null).
                map(entry -> entry.getKey().topic()).
                collect(Collectors.toSet());
        topicNames.forEach(topicName -> {
            Set<String> groups = topicGroups.get(topicName);
            if (groups == null) {
                groups = new HashSet<>();
                topicGroups.put(topicName, groups);
            }
            groups.add(groupName);
        });
    }

    private boolean isCleanUpUpdate(Map<TopicPartition, OffsetAndMetadataAdapter> update) {
        for (Map.Entry<TopicPartition, OffsetAndMetadataAdapter> entry : update.entrySet()){
            if (entry.getValue() != null) {
                return false;
            }
        }
        return true;
    }

    private void updateTopicGroups(
            String groupName,
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata) {
        if (MapUtils.isEmpty(offsetsMetadata)) {
            return;
        }

        offsetsMetadata.forEach((topicPartition, offsetAndMetadata) -> {
            String topicName = topicPartition.topic();
            Set<String> groups = topicGroups.get(topicName);
            if (offsetAndMetadata != null) { // add
                if (groups == null) {
                    groups = new HashSet<>();
                    topicGroups.put(topicName, groups);
                }
                groups.add(groupName);
            } else { // remove
                if (groups != null) {
                    groups.remove(groupName);
                    if (groups.isEmpty()) {
                        topicGroups.remove(topicName);
                    }
                }
            }
        });
    }

    private void setOffsetTimeSeries(
            String groupName,
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata) {
        if (MapUtils.isEmpty(offsetsMetadata)) {
            return;
        }

        offsetsMetadata.entrySet().stream().
            // client API gives no timestampts...
            filter(entry -> entry.getValue() != null && entry.getValue().getCommitTimestamp() != null).
            forEach(entry -> {
                TopicPartition topicPartition = entry.getKey();
                OffsetAndMetadataAdapter offsetAndMetadata = entry.getValue();

                Map<TopicPartition, OffsetTimeSeries> groupTimeSeries = offsetTimeSeries.get(groupName);
                if (groupTimeSeries == null) {
                    groupTimeSeries = new HashMap<>();
                    offsetTimeSeries.put(groupName, groupTimeSeries);
                }

                OffsetTimeSeries partitionTimeSeries = groupTimeSeries.get(topicPartition);
                if (partitionTimeSeries == null) {
                    partitionTimeSeries = new OffsetTimeSeries(topicPartition);
                    groupTimeSeries.put(topicPartition, partitionTimeSeries);
                }

                partitionTimeSeries.append(
                        offsetAndMetadata.getCommitTimestamp(),
                        offsetAndMetadata.getOffset());
            });
    }

    private void updateOffsetTimeSeries(
            String groupName,
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata) {
        if (MapUtils.isEmpty(offsetsMetadata)) {
            return;
        }

        offsetsMetadata.entrySet().stream().
            // client API gives no timestampts...
            filter(entry -> entry.getValue() == null || entry.getValue().getCommitTimestamp() != null).
            forEach(entry -> {
                TopicPartition topicPartition = entry.getKey();
                OffsetAndMetadataAdapter offsetAndMetadata = entry.getValue();

                Map<TopicPartition, OffsetTimeSeries> groupTimeSeries = offsetTimeSeries.get(groupName);
                if (offsetAndMetadata != null) {
                    if (groupTimeSeries == null) {
                        groupTimeSeries = new HashMap<>();
                        offsetTimeSeries.put(groupName, groupTimeSeries);
                    }

                    OffsetTimeSeries partitionTimeSeries = groupTimeSeries.get(topicPartition);
                    if (partitionTimeSeries == null) {
                        partitionTimeSeries = new OffsetTimeSeries(topicPartition);
                        groupTimeSeries.put(topicPartition, partitionTimeSeries);
                    }

                    partitionTimeSeries.append(
                            offsetAndMetadata.getCommitTimestamp(),
                            offsetAndMetadata.getOffset());
                } else {
                    if (groupTimeSeries != null) {
                        groupTimeSeries.remove(topicPartition);
                        if (groupTimeSeries.isEmpty()) {
                            offsetTimeSeries.remove(groupName);
                        }
                    }
                }
            });
    }

    private void fireCacheListener(Map<String, KafkaGroupMetadata> update) {
        update.forEach((name, metadata) -> {
            if (metadata != null) {
                try {
                    cacheListener.onGroupMetadataUpdated(metadata);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'group metadata updated'. Group = %s",
                                    metadata),
                            ex);
                }
            } else {
                try {
                    cacheListener.onGroupMetadataRemoved(name);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'group metadata removed'. Group name = %s",
                                    name),
                            ex);
                }
            }
        });
    }

    private static Map<String, GroupMetadataAdapter> toClientGroupMetadataUpdates(
            Map<String, ConsumerGroupDescription> rawGroups) {
        if (MapUtils.isEmpty(rawGroups)) {
            return Collections.emptyMap();
        }

        return rawGroups.entrySet().stream().
                collect(
                        Collectors.toMap(
                                entry -> entry.getKey(),
                                entry -> ClientGroupMetadata.ofNullable(entry.getValue())));
    }

    private static Map<String, Map<TopicPartition, OffsetAndMetadataAdapter>> toClientOffsetMetadataUpdates(
            Map<String, Map<TopicPartition, org.apache.kafka.clients.consumer.OffsetAndMetadata>> rawOffsets) {
        if (MapUtils.isEmpty(rawOffsets)) {
            return Collections.emptyMap();
        }

        Map<String, Map<TopicPartition, OffsetAndMetadataAdapter>> offsetUpdates = new HashMap<>();
        rawOffsets.forEach((groupName, rawOffsetsMetadata) -> {
            Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata = rawOffsetsMetadata.entrySet().stream()
                    .filter(offset -> nonNull(offset.getValue()))
                    .collect(
                            Collectors.toMap(
                                    Map.Entry::getKey,
                                    entry -> ClientOffsetAndMetadata.ofNullable(entry.getValue())));
            offsetUpdates.put(groupName, offsetsMetadata);
        });
        return offsetUpdates;
    }

    private enum UpdateMode {
        SET, UPDATE
    }

    public interface CacheListener {
        void onGroupMetadataUpdated(KafkaGroupMetadata groupMetadata);
        void onGroupMetadataRemoved(String groupName);
    }

}
