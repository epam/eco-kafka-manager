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
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseNoopPredicate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.time.OffsetDateTime.now;

/**
 * @author Mikhail_Vershkov
 */
public class BrowserCachedFetcher {

    private static final String PARTITION_OFFSET_DELIMITER = "_";
    private static final long DEFAULT_FETCH_TIMEOUT = 10000L;
    private static final long EXPIRATION_TIME_IN_MINUTES = 30L;

    private final KafkaManager kafkaManager;
    public BrowserCachedFetcher(KafkaManager kafkaManager) {
        this.kafkaManager = kafkaManager;
    }

    private static final Map<String, CacheContent> resultsCache = new ConcurrentHashMap<>();

    public void put(String sessionId, List<ConsumerRecord<Object,Object>> consumerRecords) {
        resultsCache.put(sessionId, new CacheContent(now().plusMinutes(EXPIRATION_TIME_IN_MINUTES), consumerRecords));
    }

    public Optional<ConsumerRecord<Object,Object>> getConsumerRecord(String sessionId,
                                            String topicName,
                                            TopicRecordFetchParams.DataFormat keyFormat,
                                            TopicRecordFetchParams.DataFormat valueFormat,
                                            String recordId) {

        PartitionWithOffset partitionWithOffset = new PartitionWithOffset(recordId);

        if(resultsCache.containsKey(sessionId)
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
            consumerRecord = taskResult.getValue().getRecords().stream().filter(record->record.offset()==partitionWithOffset.getOffset()).findFirst();
        }
        return consumerRecord;

    }

    private TopicRecordFetchParams resolveCopyRecordFetchParams(TopicRecordFetchParams.DataFormat keyFormat,
                                                                TopicRecordFetchParams.DataFormat valueFormat,
                                                                int partition,
                                                                long offset) {
        return new TopicRecordFetchParams( keyFormat, valueFormat,
                Map.of(partition, new OffsetRange(offset>0?offset-1:offset,true, offset>0?offset-1:offset,true)),
                1, DEFAULT_FETCH_TIMEOUT, FetchMode.FETCH_FORWARD, 0L, false, 0L,
                new FilterClauseNoopPredicate()
        );
    }

    private static class PartitionWithOffset {
        private final int partition;
        private final long offset;
        PartitionWithOffset(String partitionOffset) {
            if(partitionOffset.contains(PARTITION_OFFSET_DELIMITER)) {
                String[] recordIdArray = partitionOffset.split(PARTITION_OFFSET_DELIMITER);
                partition = Integer.parseInt(recordIdArray[0]);
                offset = Long.parseLong(recordIdArray[1]);
            } else {
                throw new RuntimeException("Wrong format in \"partition_offset\" variable.");
            }
        }
        public int getPartition() {
            return partition;
        }
        public long getOffset() {
            return offset;
        }
    }

    protected record CacheContent(OffsetDateTime expirationTime, List<ConsumerRecord<Object, Object>> records) {
    }

    public static class BrowserCachedFetcherCleaner extends TimerTask {
        private static final String THREAD_NAME = "BrowserCachedFetcherCleaner";

        public BrowserCachedFetcherCleaner(Long logIntervalMin) {
            long logIntervalMs = logIntervalMin * 60 * 1000;
            new Timer().schedule(this, logIntervalMs, logIntervalMs);
        }

        public static BrowserCachedFetcherCleaner with(Long logIntervalMin) {
            return new BrowserCachedFetcherCleaner(logIntervalMin);
        }

        @Override
        public void run() {
            Thread.currentThread().setName(THREAD_NAME);
            cleanup();
        }

        public void cleanup() {
            List<String> recordsToRemove = resultsCache.entrySet().stream()
                    .filter(cacheContent -> cacheContent.getValue().expirationTime().isBefore(now()))
                    .map(Map.Entry::getKey)
                    .toList();
            recordsToRemove.forEach(resultsCache::remove);
        }
    }


}
