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

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.codahale.metrics.Metric;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.udmetrics.UDMetricConfig;
import com.epam.eco.kafkamanager.udmetrics.UDMetricCreator;
import com.epam.eco.kafkamanager.udmetrics.utils.MetricNameUtils;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupLagUDMCreator implements UDMetricCreator {

    public static final String TOPIC_NAMES = "topicNames";

    @Override
    public Map<String, Metric> create(UDMetricConfig config, KafkaManager kafkaManager) {
        Validate.notNull(config, "UDM config is null");
        Validate.notNull(kafkaManager, "KafkaManager is null");

        kafkaManager.getConsumerGroup(config.getResourceName()); // sanity check

        Map<String, Metric> metrics = new TreeMap<>();
        getTopicNames(config).forEach(topicName -> {
            TopicInfo topicInfo = kafkaManager.getTopic(topicName);
            topicInfo.getPartitions().keySet().forEach((topicPartition) -> {
                metrics.put(
                        MetricNameUtils.sanitizeName(topicPartition.toString()),
                        ConsumerGroupPartitionLagMetric.with(
                                config.getResourceName(),
                                topicPartition,
                                kafkaManager));
            });
        });

        return metrics;
    }

    @SuppressWarnings("unchecked")
    private List<String> getTopicNames(UDMetricConfig config) {
        List<String> topicNames = (List<String>)config.getConfig().get(TOPIC_NAMES);
        validateTopicNames(config, topicNames);
        return topicNames;
    }

    private void validateTopicNames(UDMetricConfig config, List<String> topicNames) {
        if (topicNames == null || topicNames.isEmpty()) {
            throw new RuntimeException(
                    String.format("UDM config %s: '%s' is missing", config, TOPIC_NAMES));
        }
        topicNames.forEach(topicName -> {
            if (StringUtils.isBlank(topicName)) {
                throw new RuntimeException(
                        String.format(
                                "UDM config %s: '%s' has null/blank elements",
                                config, TOPIC_NAMES));
            }
        });
    }

}
