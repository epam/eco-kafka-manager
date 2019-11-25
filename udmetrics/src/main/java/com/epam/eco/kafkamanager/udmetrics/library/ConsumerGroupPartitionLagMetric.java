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

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.eco.commons.kafka.KafkaUtils;
import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.udmetrics.Metric;
import com.epam.eco.kafkamanager.udmetrics.ScheduleCalculatedMetric;

import io.micrometer.core.instrument.Tags;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupPartitionLagMetric implements Metric, ScheduleCalculatedMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerGroupPartitionLagMetric.class);

    public static final String TAG_GROUP = "group";
    public static final String TAG_TOPIC = "topic";
    public static final String TAG_PARTITION = "partition";

    private final String groupName;
    private final TopicPartition partition;

    private final Tags tags;

    private final KafkaManager kafkaManager;

    public ConsumerGroupPartitionLagMetric(
            String groupName,
            TopicPartition topicPartition,
            KafkaManager kafkaManager) {
        Validate.notNull(kafkaManager, "Kafka manager is null");

        this.groupName = groupName;
        this.partition = topicPartition;
        this.kafkaManager = kafkaManager;

        this.tags = Tags.of(
                TAG_GROUP, groupName,
                TAG_TOPIC, topicPartition.topic(),
                TAG_PARTITION, "" + topicPartition.partition());
    }

    @Override
    public Tags getTags() {
        return tags;
    }

    @Override
    public double value() {
        if (!checkGroupExists()) {
            return Double.NaN;
        }

        try {
            Long offset = getPartitionOffset();
            OffsetRange offsetRange = getPartitionOffsetRange();

            return calculateLag(offsetRange, offset);
        } catch (Exception ex) {
            LOGGER.error("Failed to calculate consumer group lag: {}", ex.getMessage());
            return Double.NaN;
        }
    }

    @Override
    public void calculateValue() {
        if (!checkGroupExists()) {
            return;
        }

        try {
            prefetchTopicOffsets();
        } catch (Exception ex) {
            LOGGER.error("Failed to pre-fetch offsets for group topics", ex);
        }
    }

    public String getGroupName() {
        return groupName;
    }
    public TopicPartition getPartition() {
        return partition;
    }

    private double calculateLag(OffsetRange offsetRange, Long offset) {
        if (offsetRange == null || offset == null) {
            return Double.NaN;
        }

        long lag = KafkaUtils.calculateConsumerLag(offsetRange, offset);
        return lag >= 0 ? lag : 0;
    }

    private Long getPartitionOffset() {
        return kafkaManager.getConsumerGroup(groupName).getOffsets().get(partition);
    }

    private OffsetRange getPartitionOffsetRange() {
        TaskResult<Map<TopicPartition, OffsetRange>> currentResult =
                kafkaManager.getConsumerGroupTopicOffsetFetcherTaskExecutor().
                getResult(groupName).
                orElse(null);
        return
                currentResult != null ?
                currentResult.getValue().get(partition) :
                null;
    }

    private void prefetchTopicOffsets() {
        kafkaManager.getConsumerGroupTopicOffsetFetcherTaskExecutor().submit(groupName);
    }

    private boolean checkGroupExists() {
        boolean exists = kafkaManager.consumerGroupExists(groupName);
        if (!exists) {
            LOGGER.warn("Group '{}' doesn't exist", groupName);
        }
        return exists;
    }

    @Override
    public String toString() {
        return tags.toString();
    }

}
