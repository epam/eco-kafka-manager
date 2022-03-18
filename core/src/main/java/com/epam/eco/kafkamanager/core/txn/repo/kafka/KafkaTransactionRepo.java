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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.Validate;
import org.apache.commons.math3.stat.descriptive.SummaryStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.Statistics;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.TransactionMetadataInfo;
import com.epam.eco.kafkamanager.TransactionRepo;
import com.epam.eco.kafkamanager.TransactionSearchCriteria;
import com.epam.eco.kafkamanager.TransactionTimeSeries;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBean;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import kafka.coordinator.transaction.TransactionMetadata;

/**
 * @author Andrei_Tytsik
 */
public class KafkaTransactionRepo extends AbstractKeyValueRepo<String, TransactionInfo, TransactionSearchCriteria> implements TransactionRepo, CachedRepo<String>, KafkaTransactionCache.CacheListener, AsyncStartingBean {

    private final static Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionRepo.class);

    @Autowired
    private KafkaManagerProperties properties;

    private KafkaTransactionCache transactionCache;

    private final Map<String, TransactionInfo> transactionInfoCache = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        initTransactionCache();

        LOGGER.info("Initialized");
    }

    @Override
    public void startAsync() throws Exception {
        startTransactionCache();

        LOGGER.info("Started");
    }

    @PreDestroy
    private void destroy() {
        destroyTransactionCache();

        LOGGER.info("Destroyed");
    }

    private void initTransactionCache() {
        transactionCache = new KafkaTransactionCache(
                properties.getBootstrapServers(),
                properties.getClientConfig(),
                properties.getTransactionStoreBootstrapTimeoutInMs(),
                properties.getTransactionStoreBootstrapDataFreshness(),
                this);
    }

    private void startTransactionCache() throws Exception {
        transactionCache.start();
    }

    private void destroyTransactionCache() {
        transactionCache.close();
    }

    @Override
    public int size() {
        return transactionCache.size();
    }

    @Override
    public boolean contains(String transactionalId) {
        Validate.notBlank(transactionalId, "Transactional Id can't be blank");

        return transactionCache.contains(transactionalId);
    }

    @Override
    public TransactionInfo get(String transactionalId) {
        Validate.notBlank(transactionalId, "Transactional Id can't be blank");

        TransactionInfo transactionInfo = getTransactionFromInfoCacheOrCreate(transactionalId);
        if (transactionInfo == null) {
            throw new NotFoundException(String.format("Transaction not found by id '%s'", transactionalId));
        }

        return transactionInfo;
    }

    @Override
    public List<TransactionInfo> values() {
        List<TransactionInfo> transactionInfos = new ArrayList<>();
        transactionCache.listTransactionalIds().forEach(transactionalId -> {
            TransactionInfo transactionInfo = getTransactionFromInfoCacheOrCreate(transactionalId);
            if (transactionInfo != null) {
                transactionInfos.add(transactionInfo);
            }
        });
        Collections.sort(transactionInfos);
        return transactionInfos;
    }

    @Override
    public List<TransactionInfo> values(List<String> transactionalIds) {
        Validate.noNullElements(
                transactionalIds, "Collection of Transactional Ids can't be null or contain null elements");

        List<TransactionInfo> transactionInfos = new ArrayList<>();
        transactionalIds.forEach(transactionalId -> {
            TransactionInfo transactionInfo = getTransactionFromInfoCacheOrCreate(transactionalId);
            if (transactionInfo != null) {
                transactionInfos.add(transactionInfo);
            }
        });
        Collections.sort(transactionInfos);
        return transactionInfos;
    }

    @Override
    public List<String> keys() {
        return transactionCache.listTransactionalIds().stream().
                sorted().
                collect(Collectors.toList());
    }

    @Override
    public List<TransactionInfo> transactionsForTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        return values(
                transactionCache.listTransactionalIdsOfTopic(topicName).stream().
                    sorted().
                    collect(Collectors.toList()));
    }

    @Override
    public void evict(String transactionalId) {
        removeTransactionFromInfoCache(transactionalId);
    }

    @Override
    public void onTransactionUpdated(TransactionProjection transaction) {
        Validate.notNull(transaction, "Transaction can't be null");

        removeTransactionFromInfoCache(transaction.getTransactionalId());
    }

    @Override
    public void onTransactionRemoved(String transactionalId) {
        removeTransactionFromInfoCache(transactionalId);
    }

    private void removeTransactionFromInfoCache(String transactionalId) {
        Validate.notBlank(transactionalId, "Transactional Id can't be blank");

        transactionInfoCache.remove(transactionalId);
    }

    private TransactionInfo getTransactionFromInfoCacheOrCreate(String transactionalId) {
        return transactionInfoCache.computeIfAbsent(
                transactionalId,
                key -> {
                    TransactionProjection transaction = transactionCache.getTransaction(transactionalId);
                    return transaction != null ? toTransactionInfo(transaction) : null;
                });
    }

    private TransactionInfo toTransactionInfo(TransactionProjection transaction) {
        TransactionInfo.Builder builder = TransactionInfo.builder();
        transaction.getHistory().forEach(
                metadata -> builder.metadata(toTransactionMetadataInfo(metadata)));

        String transactionalId = transaction.getTransactionalId();
        TransactionTimeSeries successTimeSeries = transactionCache.getSuccessTimeSeries(transactionalId);
        TransactionTimeSeries failTimeSeries = transactionCache.getFailTimeSeries(transactionalId);
        SummaryStatistics stats = transaction.getExecTimeStats();
        return builder.
            topicNames(transaction.getTopics()).
            successTimeSeries(
                    successTimeSeries != null ?
                    successTimeSeries :
                    TransactionTimeSeries.unmodifiableEmpty(transactionalId)).
            failTimeSeries(
                    failTimeSeries != null ?
                    failTimeSeries :
                    TransactionTimeSeries.unmodifiableEmpty(transactionalId)).
            execTimeStats(Statistics.with(stats.getMax(), stats.getMin(), stats.getMean())).
            build();
    }

    private TransactionMetadataInfo toTransactionMetadataInfo(TransactionMetadata metadata) {
        return TransactionMetadataInfo.builder().
                transactionalId(metadata.transactionalId()).
                producerId(metadata.producerId()).
                producerEpoch(metadata.producerEpoch()).
                timeoutMs(metadata.txnTimeoutMs()).
                state(metadata.state()).
                partitions(new ArrayList<>(ScalaConversions.asJavaSet(metadata.topicPartitions()))).
                startDate(metadata.txnStartTimestamp()).
                lastUpdateDate(metadata.txnLastUpdateTimestamp()).
                build();
    }

}
