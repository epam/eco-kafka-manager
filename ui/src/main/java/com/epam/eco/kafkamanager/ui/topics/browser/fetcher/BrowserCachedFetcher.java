/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.topics.browser.fetcher;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.FetchMode;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseNoopPredicate;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.OffsetDateTime.now;
import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */
public class BrowserCachedFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrowserCachedFetcher.class);
    private static final String PARTITION_OFFSET_DELIMITER = "_";
    private static final long DEFAULT_FETCH_TIMEOUT_IN_MILS = 10000L;
    private static final long DIVIDER_TO_REACH_MB = 1_000_000L;

    private final KafkaManager kafkaManager;
    private final long cacheThresholdInMb;
    private final long exparationTimeInMins;

    public BrowserCachedFetcher(KafkaManager kafkaManager,
                                KafkaManagerUiProperties properties) {
        this.kafkaManager = kafkaManager;
        cacheThresholdInMb = properties.getTopicBrowser().getCacheThresholdInMb();
        exparationTimeInMins = properties.getTopicBrowser().getCacheExpirationPeriodMin();
    }

    private static final Map<String, CacheContent> resultsCache = new ConcurrentHashMap<>();

    public void put(String sessionId, List<ConsumerRecord<Object,Object>> consumerRecords) {
        resultsCache.put(sessionId, new CacheContent(now().plusMinutes(exparationTimeInMins),
                 consumerRecords.stream().map(this::getConsumerRecordSize).reduce(Long::sum).orElse(0L),
                 consumerRecords));
    }

    private long getConsumerRecordSize(ConsumerRecord consumerRecord) {
        if(isNull(consumerRecord)) {
            return 0L;
        }
        int headersLength = 0;
        try {
            for(Header header: consumerRecord.headers()) {
               headersLength += header.key().getBytes().length + (isNull(header.value()) ? 0 : header.value().length);
            }
        } catch (Exception ex) {
            LOGGER.warn(ex.getMessage());
        }
        return consumerRecord.serializedKeySize()+consumerRecord.serializedValueSize()+headersLength;

    }

    public Optional<ConsumerRecord<Object,Object>> getConsumerRecord(String sessionId,
                                                                     String topicName,
                                                                     TopicRecordFetchParams.DataFormat keyFormat,
                                                                     TopicRecordFetchParams.DataFormat valueFormat,
                                                                     String recordId) {

        PartitionWithOffset partitionWithOffset = new PartitionWithOffset(recordId);

        if(getSizeMb(resultsCache)<cacheThresholdInMb
                && resultsCache.containsKey(sessionId)
                && resultsCache.get(sessionId).expirationTime().isAfter(now())
                && !resultsCache.get(sessionId).records().isEmpty()) {
            return resultsCache.get(sessionId).records().stream()
                    .filter(consumerRecord -> consumerRecord.partition()==partitionWithOffset.getPartition() &&
                                              consumerRecord.offset()==partitionWithOffset.getOffset()).findFirst();
        }

        TopicRecordFetchParams fetchParams = resolveCopyRecordFetchParams(keyFormat, valueFormat,
                                                                          partitionWithOffset.getPartition(),
                                                                          partitionWithOffset.getOffset());

        TaskResult<RecordFetchResult<Object, Object>> taskResult = kafkaManager.getTopicRecordFetcherTaskExecutor().
                executeDetailed(topicName, fetchParams);

        Optional<ConsumerRecord<Object,Object>> consumerRecord = Optional.empty();
        if(taskResult.getValue().count()>0) {
            consumerRecord = taskResult.getValue().getRecords().stream()
                    .filter(message->message.offset()==partitionWithOffset.getOffset())
                    .findFirst();
        }
        return consumerRecord;

    }

    private TopicRecordFetchParams resolveCopyRecordFetchParams(TopicRecordFetchParams.DataFormat keyFormat,
                                                                TopicRecordFetchParams.DataFormat valueFormat,
                                                                int partition,
                                                                long offset) {
        return new TopicRecordFetchParams( keyFormat, valueFormat,
                Map.of(partition, new OffsetRange(offset>0?offset-1:offset,true, offset>0?offset-1:offset,true)),
                1, DEFAULT_FETCH_TIMEOUT_IN_MILS, FetchMode.FETCH_FORWARD, 0L, false, 0L,
                new FilterClauseNoopPredicate()
        );
    }

    private long getSizeMb(Map<String, CacheContent> map) {
        return map.values().stream()
                .map(CacheContent::recordSize)
                .filter(size->size>0)
                .reduce(Long::sum)
                .map(sum->sum/DIVIDER_TO_REACH_MB)
                .orElse(0L);
    }

    public static class PartitionWithOffset {

        private final int partition;
        private final long offset;

        public PartitionWithOffset(String partitionOffset) {
            if (partitionOffset.contains(PARTITION_OFFSET_DELIMITER)) {
                String[] recordIdArray = partitionOffset.split(PARTITION_OFFSET_DELIMITER);
                partition = Integer.parseInt(recordIdArray[0]);
                offset = Long.parseLong(recordIdArray[1]);
            } else {
                throw new RuntimeException("Wrong format in \"partition_offset\" variable. (\"partition_offset\"=" + partitionOffset + ")");
            }
        }

        public int getPartition() {
            return partition;
        }

        public long getOffset() {
            return offset;
        }
    }

    protected record CacheContent(OffsetDateTime expirationTime, long recordSize, List<ConsumerRecord<Object, Object>> records) {
    }

    public static class BrowserCachedFetcherCleaner extends TimerTask {
        private static final String THREAD_NAME = "BrowserCachedFetcherCleaner";

        private BrowserCachedFetcherCleaner(long logIntervalMin) {
            Validate.isTrue(logIntervalMin>0);
            long logIntervalMs = logIntervalMin * 60 * 1000;
            new Timer(THREAD_NAME).schedule(this, logIntervalMs, logIntervalMs);
        }

        public static BrowserCachedFetcherCleaner with(long logIntervalMin) {
            return new BrowserCachedFetcherCleaner(logIntervalMin);
        }

        @Override
        public void run() {
            cleanup();
        }

        public void cleanup() {
            OffsetDateTime now = now();
            List<String> recordsToRemove = resultsCache.entrySet().stream()
                    .filter(cacheContent -> cacheContent.getValue().expirationTime().isBefore(now))
                    .map(Map.Entry::getKey)
                    .toList();
            recordsToRemove.forEach(resultsCache::remove);
        }
    }


}
