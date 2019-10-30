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

import com.codahale.metrics.Gauge;

import com.epam.eco.commons.kafka.KafkaUtils;
import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.udmetrics.schedule.ScheduleCalculatedMetric;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupPartitionLagMetric implements Gauge<Long>, ScheduleCalculatedMetric {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerGroupPartitionLagMetric.class);

    private final String groupName;
    private final TopicPartition partition;

    private final KafkaManager kafkaManager;

    public ConsumerGroupPartitionLagMetric(
            String groupName,
            TopicPartition partition,
            KafkaManager kafkaManager) {
        Validate.notBlank(groupName, "Group name is blank");
        Validate.notNull(partition, "Partition is null");
        Validate.notNull(kafkaManager, "Kafka manager is null");

        this.groupName = groupName;
        this.partition = partition;
        this.kafkaManager = kafkaManager;
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

    @Override
    public Long getValue() {
        if (!checkGroupExists()) {
            return null;
        }

        try {
            Long offset = getPartitionOffset();
            OffsetRange offsetRange = getPartitionOffsetRange();

            return calculateLag(offsetRange, offset);
        } catch (Exception ex) {
            LOGGER.error("Failed to calculate consumer group lag: {}", ex.getMessage());
            return null;
        }
    }

    public String getGroupName() {
        return groupName;
    }
    public TopicPartition getPartition() {
        return partition;
    }

    private Long calculateLag(OffsetRange offsetRange, Long offset) {
        if (offsetRange == null || offset == null) {
            return null;
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

    public static ConsumerGroupPartitionLagMetric with(
            String consumerGroupName,
            TopicPartition topicPartition,
            KafkaManager kafkaManager) {
        return new ConsumerGroupPartitionLagMetric(
                consumerGroupName,
                topicPartition,
                kafkaManager);
    }

}
