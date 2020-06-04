/*
 * Copyright 2020 EPAM Systems
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
package com.epam.eco.kafkamanager.core.topic.exec;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import javax.annotation.PreDestroy;
import javax.cache.CacheManager;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.TopicOffsetRangeFetcher;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.OffsetTimeSeries;
import com.epam.eco.kafkamanager.TopicOffsetRangeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;
import com.epam.eco.kafkamanager.exec.AbstractAsyncStatefullTaskExecutor;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 *
 */
public class TopicOffsetRangeFetcherTaskExecutorImpl extends
        AbstractAsyncStatefullTaskExecutor<String, Map<TopicPartition, OffsetRange>> implements
        TopicOffsetRangeFetcherTaskExecutor {

    @Autowired
    private KafkaManager kafkaManager;
    @Autowired
    protected KafkaManagerProperties properties;

    private final Map<String, Map<TopicPartition, OffsetTimeSeries>> offsetTimeSeries =
            new ConcurrentHashMap<>();

    public TopicOffsetRangeFetcherTaskExecutorImpl(CacheManager cacheManager) {
        super(cacheManager);
    }

    public TopicOffsetRangeFetcherTaskExecutorImpl(ExecutorService executor, CacheManager cacheManager) {
        super(executor, cacheManager);
    }

    @PreDestroy
    public void destroy() {
        close();
    }

    @Override
    protected TaskResult<Map<TopicPartition, OffsetRange>> doExecute(String topicName) {
        TaskResult<Map<TopicPartition, OffsetRange>> result = TaskResult.of(() -> {
            // sanity check just for case topic doesn't exist
            kafkaManager.getTopic(topicName);

            return TopicOffsetRangeFetcher.
                    with(properties.getCommonConsumerConfig()).
                    fetchForTopics(topicName);
        });

        updateOffsetTimeSeries(topicName, result);

        return result;
    }

    @Override
    public Map<TopicPartition, OffsetTimeSeries> getOffsetTimeSeries(String topicName) {
        Map<TopicPartition, OffsetTimeSeries> topicTimeSeries =
                offsetTimeSeries.get(topicName);
        return
                topicTimeSeries != null ?
                        topicTimeSeries.entrySet().stream().collect(Collectors.toMap(
                                Entry::getKey,
                                e -> e.getValue().unmodifiableCopy())) :
                        Collections.emptyMap();
    }

    private void updateOffsetTimeSeries(
            String topicName,
            TaskResult<Map<TopicPartition, OffsetRange>> result) {
        if (!result.isSuccessful() || result.getValue().isEmpty()) {
            return;
        }

        Map<TopicPartition, OffsetRange> offsets = result.getValue();

        LocalDateTime dateTime = result.getFinishedAt().
                toInstant().
                atZone(ZoneId.systemDefault()).
                toLocalDateTime();

        Map<TopicPartition, OffsetTimeSeries> topicTimeSeries =
                offsetTimeSeries.computeIfAbsent(topicName, k -> new HashMap<>());

        for (Entry<TopicPartition, OffsetRange> entry : offsets.entrySet()) {
            TopicPartition topicPartition = entry.getKey();
            OffsetRange offsetRange = entry.getValue();

            OffsetTimeSeries partitionTimeSeries = topicTimeSeries.get(topicPartition);
            if (partitionTimeSeries == null) {
                partitionTimeSeries = new OffsetTimeSeries(topicPartition);
                topicTimeSeries.put(topicPartition, partitionTimeSeries);
            }

            partitionTimeSeries.append(dateTime, offsetRange.getLargest());
        }
    }

}
