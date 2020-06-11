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
package com.epam.eco.kafkamanager.udmetrics.library;

import java.util.Collection;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.udmetrics.Metric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricCreator;
import com.epam.eco.kafkamanager.udmetrics.utils.MetricComparator;

/**
 * @author Andrei_Tytsik
 */
public class TopicOffsetIncreaseUDMCreator implements UDMetricCreator {

    @Override
    public Collection<Metric> create(String topicName, Map<String, Object> config, KafkaManager kafkaManager) {
        Validate.notNull(kafkaManager, "KafkaManager is null");

        TopicInfo topicInfo = kafkaManager.getTopic(topicName);

        Collection<Metric> metrics = new TreeSet<>(MetricComparator.INSTANCE);
        topicInfo.getPartitions().keySet().forEach(topicPartition -> metrics.add(
                new TopicPartitionOffsetIncreaseMetric(topicPartition, kafkaManager)));
        return metrics;
    }

}
