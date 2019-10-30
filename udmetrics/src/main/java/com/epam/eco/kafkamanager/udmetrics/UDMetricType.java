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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.epam.eco.kafkamanager.udmetrics.library.ConsumerGroupLagUDMCreator;
import com.epam.eco.kafkamanager.udmetrics.library.TopicOffsetIncreaseUDMCreator;
import com.epam.eco.kafkamanager.udmetrics.utils.MetricNameUtils;

/**
 * @author Andrei_Tytsik
 */
public enum UDMetricType {

    CONSUMER_GROUP_LAG(new ConsumerGroupLagUDMCreator()) {
        @Override
        public String description() {
            return
                    "Calculates consumer group lag. " +
                    "Separate metric is created for each topic-partition.";
        }
        @Override
        public Map<String, Object> configTemplate() {
            Map<String, Object> template = new HashMap<>();
            template.put(ConsumerGroupLagUDMCreator.TOPIC_NAMES, null);
            return template;
        }
        @Override
        public String formatMetricName(String consumerGroup) {
            return MetricNameUtils.sanitizeAndConcatenateNames("consumer_group_lag", consumerGroup);
        }
    },

    TOPIC_OFFSET_INCREASE(new TopicOffsetIncreaseUDMCreator()) {
        @Override
        public String description() {
            return
                    "Calculates topic offset increase. " +
                    "Separate metric is created for each topic-partition.";
        }
        @Override
        public Map<String, Object> configTemplate() {
            return Collections.emptyMap();
        }
        @Override
        public String formatMetricName(String topic) {
            return MetricNameUtils.sanitizeAndConcatenateNames("topic_offset_increase", topic);
        }
    };

    private final UDMetricCreator creator;

    UDMetricType(UDMetricCreator creator) {
        this.creator = creator;
    }

    public abstract String description();
    public abstract Map<String, Object> configTemplate();
    public abstract String formatMetricName(String resourceName);

    public UDMetricCreator creator() {
        return creator;
    }

}
