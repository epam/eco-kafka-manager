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
package com.epam.eco.kafkamanager.udmetrics.library;

import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.udmetrics.Metric;
import com.epam.eco.kafkamanager.udmetrics.ScheduleCalculatedMetric;

import io.micrometer.core.instrument.Tags;

/**
 * @author Andrei_Tytsik
 */
public class TopicPartitionOffsetIncreaseMetric implements Metric, ScheduleCalculatedMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(TopicPartitionOffsetIncreaseMetric.class);

    public static final String TAG_TOPIC = "topic";
    public static final String TAG_PARTITION = "partition";

    private final TopicPartition topicPartition;

    private final Tags tags;

    private final KafkaManager kafkaManager;

    private final AtomicReference<OffsetRange> topicOffsetRangeOld = new AtomicReference<>();

    public TopicPartitionOffsetIncreaseMetric(
            TopicPartition topicPartition,
            KafkaManager kafkaManager) {
        Validate.notNull(kafkaManager, "Kafka manager is null");

        this.topicPartition = topicPartition;
        this.kafkaManager = kafkaManager;

        this.tags = Tags.of(
                TAG_TOPIC, topicPartition.topic(),
                TAG_PARTITION, "" + topicPartition.partition());
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public double value() {
        if (!checkTopicExists()) {
            return Double.NaN;
        }

        try {
            OffsetRange topicOffsetRange = getTopicOffsetRange();
            return calculateOffsetIncrease(topicOffsetRange);
        } catch (Exception ex) {
            LOGGER.error("Failed to calculate offset increase for topic partition", ex);
        }
        return Double.NaN;
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

    public TopicPartition getTopicPartition() {
        return topicPartition;
    }

    private double calculateOffsetIncrease(OffsetRange topicOffsetRangeNew) {
        OffsetRange topicOffsetRangeOld = this.topicOffsetRangeOld.get();
        if (
                topicOffsetRangeNew == null ||
                topicOffsetRangeOld == null ||
                topicOffsetRangeOld.getLargest() > topicOffsetRangeNew.getLargest()) {
            return Double.NaN;
        }
        return topicOffsetRangeNew.getLargest() - topicOffsetRangeOld.getLargest();
    }

    private OffsetRange getTopicOffsetRange() {
        TaskResult<Map<TopicPartition, OffsetRange>> currentResult =
                kafkaManager.getTopicOffsetRangeFetcherTaskExecutor().
                getResult(topicPartition.topic()).
                orElse(null);
        return
                currentResult != null ?
                currentResult.getValue().get(topicPartition) :
                null;
    }

    private void prefetchTopicOffsets() {
        kafkaManager.getTopicOffsetRangeFetcherTaskExecutor().submit(topicPartition.topic());
    }

    private boolean checkTopicExists() {
        boolean exists = kafkaManager.topicExists(topicPartition.topic());
        if (!exists) {
            LOGGER.warn("Topic '{}' doesn't exist", topicPartition.topic());
        }
        return exists;
    }

    @Override
    public String toString() {
        return tags.toString();
    }

}
