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
package com.epam.eco.kafkamanager.udmetrics.library;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.Gauge;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.udmetrics.schedule.ScheduleCalculatedMetric;

/**
 * @author Andrei_Tytsik
 */
public class TopicPartitionOffsetIncreaseMetric implements Gauge<Long>, ScheduleCalculatedMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicPartitionOffsetIncreaseMetric.class);

    private final TopicPartition topicPartition;

    private final KafkaManager kafkaManager;

    private final AtomicReference<OffsetRange> topicOffsetRangeOld = new AtomicReference<>();

    public TopicPartitionOffsetIncreaseMetric(
            TopicPartition topicPartition,
            KafkaManager kafkaManager) {
        Validate.notNull(topicPartition, "Topic partition is null");
        Validate.notNull(kafkaManager, "Kafka Manager is null");

        this.topicPartition = topicPartition;
        this.kafkaManager = kafkaManager;
    }

    @Override
    public void calculateValue() {
        if (!checkTopicExists()) {
            return;
        }

        try {
            prefetchTopicOffsets();

            OffsetRange topicOffsetRange = getTopicOffsetRange();
            if (topicOffsetRange != null) {
                topicOffsetRangeOld.set(topicOffsetRange);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to pre-fetch topic offsets", ex);
        }
    }

    @Override
    public Long getValue() {
        if (!checkTopicExists()) {
            return null;
        }

        try {
            OffsetRange topicOffsetRange = getTopicOffsetRange();
            return calculateOffsetIncrease(topicOffsetRange);
        } catch (Exception ex) {
            LOGGER.error("Failed to calculate offset increase for topic partition", ex);
        }
        return null;
    }

    public TopicPartition getTopicPartition() {
        return topicPartition;
    }

    private Long calculateOffsetIncrease(OffsetRange topicOffsetRangeNew) {
        OffsetRange topicOffsetRangeOld = this.topicOffsetRangeOld.get();
        if (
                topicOffsetRangeNew == null ||
                topicOffsetRangeOld == null ||
                topicOffsetRangeOld.getLargest() > topicOffsetRangeNew.getLargest()) {
            return null;
        }
        return topicOffsetRangeNew.getLargest() - topicOffsetRangeOld.getLargest();
    }

    private OffsetRange getTopicOffsetRange() {
        TaskResult<Map<TopicPartition, OffsetRange>> currentResult =
                kafkaManager.getTopicOffsetFetcherTaskExecutor().
                getResult(topicPartition.topic()).
                orElse(null);
        return
                currentResult != null ?
                currentResult.getValue().get(topicPartition) :
                null;
    }

    private void prefetchTopicOffsets() {
        kafkaManager.getTopicOffsetFetcherTaskExecutor().submit(topicPartition.topic());
    }

    public static TopicPartitionOffsetIncreaseMetric with(
            TopicPartition topicPartition,
            KafkaManager kafkaManager) {
        return new TopicPartitionOffsetIncreaseMetric(topicPartition, kafkaManager);
    }

    private boolean checkTopicExists() {
        boolean exists = kafkaManager.topicExists(topicPartition.topic());
        if (!exists) {
            LOGGER.warn("Topic '{}' doesn't exist", topicPartition.topic());
        }
        return exists;
    }

}
