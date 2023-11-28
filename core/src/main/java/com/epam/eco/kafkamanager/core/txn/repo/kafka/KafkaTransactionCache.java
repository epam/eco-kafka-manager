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
package com.epam.eco.kafkamanager.core.txn.repo.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.internals.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.TransactionState;
import com.epam.eco.commons.kafka.cache.CacheListener;
import com.epam.eco.commons.kafka.cache.ProjectingKafkaCache;
import com.epam.eco.commons.kafka.consumer.bootstrap.TimestampOffsetInitializer;
import com.epam.eco.commons.kafka.serde.SimpleTransactionMetadataDecoder;
import com.epam.eco.kafkamanager.DatePeriod;
import com.epam.eco.kafkamanager.TransactionTimeSeries;

import kafka.coordinator.transaction.TransactionMetadata;

/**
 * @author Andrei_Tytsik
 */
class KafkaTransactionCache implements CacheListener<String, TransactionProjection> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionCache.class);

    private final ProjectingKafkaCache<String, TransactionMetadata, TransactionProjection> transactionCache;

    private final Map<String, Set<String>> topicsOfTransaction = new HashMap<>();
    private final Map<String, Set<String>> transactionsOfTopic = new HashMap<>();
    private final Map<String, TransactionTimeSeries> successTimeSeries = new HashMap<>();
    private final Map<String, TransactionTimeSeries> failTimeSeries = new HashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final CacheListener cacheListener;

    public KafkaTransactionCache(
            String bootstrapServers,
            Map<String, Object> consumerConfig,
            long bootstrapTimeoutInMs,
            DatePeriod dataFreshness,
            CacheListener cacheListener) {
        Validate.notNull(cacheListener, "Cache Listener can't be null");

        this.transactionCache = ProjectingKafkaCache.<String, TransactionMetadata, TransactionProjection>builder().
                bootstrapServers(bootstrapServers).
                topicName(Topic.TRANSACTION_STATE_TOPIC_NAME).
                bootstrapTimeoutInMs(bootstrapTimeoutInMs).
                consumerConfig(consumerConfig).
                offsetInitializer(
                        TimestampOffsetInitializer.forNowMinus(
                                dataFreshness.amount(),
                                dataFreshness.unit())).
                keyValueDecoder(new SimpleTransactionMetadataDecoder()).
                consumerParallelismAvailableCores().
                projectionBuilder(this::buildTransactionProjection).
                listener(this).
                build();
        this.cacheListener = cacheListener;
    }

    public void start() throws Exception {
        startTransactionCache();

        LOGGER.info("Started");
    }

    public void close() {
        destroyTransactionCache();

        LOGGER.info("Closed");
    }

    public int size() {
        return transactionCache.size();
    }

    public List<String> listTransactionalIds() {
        return new ArrayList<>(transactionCache.keysAsList());
    }

    public boolean contains(String transactionalId) {
        return transactionCache.containsKey(transactionalId);
    }

    public TransactionProjection getTransaction(String transactionalId) {
        return transactionCache.get(transactionalId);
    }

    public List<String> listTransactionalIdsOfTopic(String topicName) {
        lock.readLock().lock();
        try {
            Set<String> transactionalIds = transactionsOfTopic.get(topicName);
            return transactionalIds != null ? new ArrayList<>(transactionalIds) : Collections.emptyList();
        } finally {
            lock.readLock().unlock();
        }
    }

    public TransactionTimeSeries getSuccessTimeSeries(String transactionalId) {
        lock.readLock().lock();
        try {
            return successTimeSeries.get(transactionalId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public TransactionTimeSeries getFailTimeSeries(String transactionalId) {
        lock.readLock().lock();
        try {
            return failTimeSeries.get(transactionalId);
        } finally {
            lock.readLock().unlock();
        }
    }

    private TransactionProjection buildTransactionProjection(
            TransactionProjection previous,
            String transactionalId,
            List<TransactionMetadata> transactionLog) {
        TransactionProjection.Builder builder = previous != null ? previous.toBuilder() : null;
        for (TransactionMetadata metadata : transactionLog) {
            if (metadata == null) {
                builder = null;
                continue;
            }
            if (builder == null) {
                builder = TransactionProjection.builder();
            }
            builder.append(metadata);
        }
        return builder != null ? builder.build() : null;
    }

    @Override
    public void onCacheUpdated(Map<String, TransactionProjection> update) {
        Validate.notNull(update, "Update can't be null");

        if (update.isEmpty()) {
            return;
        }

        lock.writeLock().lock();
        try {
            update.forEach((transactionalId, transaction) -> {
                updateTransactionsOfTopic(transactionalId, transaction);
                updateSuccessTimeSeries(transactionalId, transaction);
                updateFailTimeSeries(transactionalId, transaction);
            });
        } finally {
            lock.writeLock().unlock();
        }

        fireCacheListener(update);
    }

    private void startTransactionCache() throws Exception {
        transactionCache.start();
    }

    private void destroyTransactionCache() {
        transactionCache.close();
    }

    private void updateTransactionsOfTopic(String transactionalId, TransactionProjection transaction) {
        Set<String> oldTopics = topicsOfTransaction.getOrDefault(transactionalId, Collections.emptySet());
        Set<String> newTopics =
                transaction != null ?
                transaction.getTopics() :
                Collections.emptySet();

        if (oldTopics.equals(newTopics)) {
            return;
        }

        for (String oldTopic : oldTopics) {
            if (!newTopics.contains(oldTopic)) {
                Set<String> transactionalIds = transactionsOfTopic.get(oldTopic);
                if (transactionalIds != null) {
                    transactionalIds.remove(transactionalId);
                    if (transactionalIds.isEmpty()) {
                        transactionsOfTopic.remove(oldTopic);
                    }
                }
            }
        }

        for (String newTopic : newTopics) {
            if (!oldTopics.contains(newTopic)) {
                Set<String> transactionalIds = transactionsOfTopic.computeIfAbsent(newTopic, k -> new HashSet<>());
                transactionalIds.add(transactionalId);
            }
        }

        if (newTopics.isEmpty()) {
            topicsOfTransaction.remove(transactionalId);
        } else {
            topicsOfTransaction.put(transactionalId, newTopics);
        }
    }

    private void updateSuccessTimeSeries(String transactionalId, TransactionProjection transaction) {
        if (transaction == null) {
            successTimeSeries.remove(transactionalId);
            return;
        }

        TransactionTimeSeries timeSeries = successTimeSeries.get(transactionalId);
        if (timeSeries == null) {
            timeSeries = new TransactionTimeSeries(transactionalId);
            successTimeSeries.put(transactionalId, timeSeries);
        }

        TransactionMetadata current = transaction.getCurrent();
        TransactionState state = TransactionState.fromScala(current.state());
        if (TransactionState.COMPLETE_COMMIT == state) {
            timeSeries.append(current.txnLastUpdateTimestamp(), 1);
        }
    }

    private void updateFailTimeSeries(String transactionalId, TransactionProjection transaction) {
        if (transaction == null) {
            failTimeSeries.remove(transactionalId);
            return;
        }

        TransactionTimeSeries timeSeries = failTimeSeries.get(transactionalId);
        if (timeSeries == null) {
            timeSeries = new TransactionTimeSeries(transactionalId);
            failTimeSeries.put(transactionalId, timeSeries);
        }

        TransactionMetadata current = transaction.getCurrent();
        TransactionState state = TransactionState.fromScala(current.state());
        if (
                TransactionState.COMPLETE_ABORT == state ||
                TransactionState.DEAD == state) {
            timeSeries.append(current.txnLastUpdateTimestamp(), 1);
        }
    }

    private void fireCacheListener(Map<String, TransactionProjection> update) {
        update.forEach((transactionalId, transaction) -> {
            if (transaction != null) {
                try {
                    cacheListener.onTransactionUpdated(transaction);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'transaction updated' event. Transaction = %s",
                                    transaction),
                            ex);
                }
            } else {
                try {
                    cacheListener.onTransactionRemoved(transactionalId);
                } catch (Exception ex) {
                    LOGGER.error(
                            String.format(
                                    "Failed to handle 'transaction removed' event. Transactional Id = %s",
                                    transactionalId),
                            ex);
                }
            }
        });
    }

    public interface CacheListener {
        void onTransactionUpdated(TransactionProjection transaction);
        void onTransactionRemoved(String transactionalId);
    }

}
