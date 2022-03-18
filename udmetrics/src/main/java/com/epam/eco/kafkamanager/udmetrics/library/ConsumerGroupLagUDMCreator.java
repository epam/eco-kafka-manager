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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.udmetrics.Metric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricCreator;
import com.epam.eco.kafkamanager.udmetrics.utils.MetricComparator;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupLagUDMCreator implements UDMetricCreator {

    public static final String TOPIC_NAMES = "topicNames";

    @Override
    public Collection<Metric> create(String groupName, Map<String, Object> config, KafkaManager kafkaManager) {
        Validate.notNull(config, "Config is null");
        Validate.notNull(kafkaManager, "KafkaManager is null");

        kafkaManager.getConsumerGroup(groupName); // sanity check

        Collection<Metric> metrics = new TreeSet<>(MetricComparator.INSTANCE);
        getTopicNames(config).forEach(topicName -> {
            TopicInfo topicInfo = kafkaManager.getTopic(topicName);
            topicInfo.getPartitions().keySet().forEach(topicPartition -> metrics.add(
                    new ConsumerGroupPartitionLagMetric(
                            groupName,
                            topicPartition,
                            kafkaManager)));
        });
        return metrics;
    }

    @Override
    public Map<String, Object> configTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put(TOPIC_NAMES, null);
        return template;
    }

    @SuppressWarnings("unchecked")
    private List<String> getTopicNames(Map<String, Object> config) {
        List<String> topicNames = (List<String>)config.get(TOPIC_NAMES);
        validateTopicNames(config, topicNames);
        return topicNames;
    }

    private void validateTopicNames(Map<String, Object> config, List<String> topicNames) {
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
