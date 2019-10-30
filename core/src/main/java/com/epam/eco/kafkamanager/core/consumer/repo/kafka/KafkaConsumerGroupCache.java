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
package com.epam.eco.kafkamanager.core.consumer.repo.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.internals.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.cache.KafkaCache;
import com.epam.eco.commons.kafka.consumer.bootstrap.TimestampOffsetInitializer;
import com.epam.eco.kafkamanager.DatePeriod;
import com.epam.eco.kafkamanager.OffsetTimeSeries;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.BaseKey;

/**
 * @author Andrei_Tytsik
 */
class KafkaConsumerGroupCache implements com.epam.eco.commons.kafka.cache.CacheListener<BaseKey, KafkaMetadataRecord<?, ?>> {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerGroupCache.class);

    private final KafkaCache<BaseKey, KafkaMetadataRecord<?, ?>> metadataCache;

    private final Map<String, KafkaGroupMetadata> groupCache = new HashMap<>();
    private final Map<String, Set<String>> groupsOfTopic = new HashMap<>();
    private final Map<String, Map<TopicPartition, OffsetTimeSeries>> offsetTimeSeries =
            new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public KafkaConsumerGroupCache(
            String bootstrapServers,
            Map<String, Object> consumerConfig,
            long bootstrapTimeoutInMs,
            DatePeriod dataFreshness,
            CacheListener cacheListener) {
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        this.metadataCache = KafkaCache.<BaseKey, KafkaMetadataRecord<?, ?>>builder().
                bootstrapServers(bootstrapServers).
                topicName(Topic.GROUP_METADATA_TOPIC_NAME).
                bootstrapTimeoutInMs(bootstrapTimeoutInMs).
                consumerConfig(consumerConfig).
                offsetInitializer(
                        TimestampOffsetInitializer.forNowMinus(
                                dataFreshness.amount(),
                                dataFreshness.unit())).
                keyValueDecoder(new KafkaGroupMetadataDecoder()).
                consumerParallelismAvailableCores().
                storeData(false).
                listener(this).
                build();
        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
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
            Set<String> groupNames = groupsOfTopic.get(topicName);
            return groupNames != null ? new ArrayList<>(groupNames) : Collections.emptyList();
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
    public void onCacheUpdated(Map<BaseKey, KafkaMetadataRecord<?, ?>> update) {
        Validate.notNull(update, "Update can't be null");

        if (update.isEmpty()) {
            return;
        }

        Map<String, KafkaGroupMetadata> effectiveUpdate = new HashMap<>();

        lock.writeLock().lock();
        try {
            update.values().forEach(record -> {
                try {
                    KafkaGroupMetadata groupMetadata = getGroupMetadata(record.getGroupName(), true);
                    if (record instanceof KafkaOffsetMetadataRecord) {
                        KafkaOffsetMetadataRecord offsetMetadataRecord = (KafkaOffsetMetadataRecord)record;
                        groupMetadata.updateOffsetAndMetadata(
                                offsetMetadataRecord.getKey(),
                                offsetMetadataRecord.getValue());
                        updateGroupsOfTopic(offsetMetadataRecord);
                        updateOffsetTimeSeries(offsetMetadataRecord);
                    } else if (record instanceof KafkaGroupMetadataRecord) {
                        KafkaGroupMetadataRecord groupMetadataRecord = (KafkaGroupMetadataRecord)record;
                        groupMetadata.updateGroupMetadata(groupMetadataRecord.getValue());
                    } else {
                        LOGGER.warn("Ignoring unsupported metadata record = {}", record);
                    }

                    boolean removed = removeGroupMetadataIfHasNoMetadata(groupMetadata);

                    if (removed) {
                        effectiveUpdate.put(groupMetadata.getName(), null);
                    } else {
                        effectiveUpdate.put(groupMetadata.getName(), groupMetadata);
                    }
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'group metadata updated' event. Record = %s",
                                    record),
                            ex);
                }
            });
        } finally {
            lock.writeLock().unlock();
        }

        fireCacheListener(effectiveUpdate);
    }

    private void startMetadataCache() throws Exception {
        metadataCache.start();
    }

    private void destroyMetadataCache() {
        metadataCache.close();
    }

    private KafkaGroupMetadata getGroupMetadata(String groupName, boolean createIfMissing) {
        KafkaGroupMetadata groupMetadata = groupCache.get(groupName);
        if (groupMetadata == null && createIfMissing) {
            groupMetadata = new KafkaGroupMetadata(groupName);
            groupCache.put(groupName, groupMetadata);
        }
        return groupMetadata;
    }

    private void updateGroupsOfTopic(KafkaOffsetMetadataRecord offsetMetadataRecord) {
        String groupName = offsetMetadataRecord.getGroupName();
        String topicName = offsetMetadataRecord.getKey().key().topicPartition().topic();
        Set<String> groupNames = groupsOfTopic.get(topicName);
        if (offsetMetadataRecord.getValue() != null) { // add
            if (groupNames == null) {
                groupNames = new HashSet<>();
                groupsOfTopic.put(topicName, groupNames);
            }
            groupNames.add(groupName);
        } else { // remove
            if (groupNames != null) {
                groupNames.remove(groupName);
                if (groupNames.isEmpty()) {
                    groupsOfTopic.remove(topicName);
                }
            }
        }
    }

    private void updateOffsetTimeSeries(KafkaOffsetMetadataRecord offsetMetadataRecord) {
        String groupName = offsetMetadataRecord.getGroupName();
        TopicPartition topicPartition = offsetMetadataRecord.getKey().key().topicPartition();
        Map<TopicPartition, OffsetTimeSeries> groupTimeSeries =
                offsetTimeSeries.get(groupName);
        OffsetAndMetadata offsetAndMetadata = offsetMetadataRecord.getValue();
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
                    offsetAndMetadata.commitTimestamp(),
                    offsetAndMetadata.offset());
        } else {
            if (groupTimeSeries != null) {
                groupTimeSeries.remove(topicPartition);
                if (groupTimeSeries.isEmpty()) {
                    offsetTimeSeries.remove(groupName);
                }
            }
        }
    }

    private boolean removeGroupMetadataIfHasNoMetadata(KafkaGroupMetadata groupMetadata) {
        if (groupMetadata.hasMetadata()) {
            return false;
        }

        return groupCache.remove(groupMetadata.getName()) != null;
    }

    private void fireCacheListener(Map<String, KafkaGroupMetadata> update) {
        update.forEach((name, metadata) -> {
            if (metadata != null) {
                try {
                    cacheListener.onGroupMetadataUpdated(metadata);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'group metadata updated' event. Group = %s",
                                    metadata),
                            ex);
                }
            } else {
                try {
                    cacheListener.onGroupMetadataRemoved(name);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'group metadata removed' event. Group name = %s",
                                    name),
                            ex);
                }
            }
        });
    }

    public static interface CacheListener {
        void onGroupMetadataUpdated(KafkaGroupMetadata groupMetadata);
        void onGroupMetadataRemoved(String groupName);
    }

}
