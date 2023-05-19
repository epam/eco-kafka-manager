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
package com.epam.eco.kafkamanager.utils;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.epam.eco.kafkamanager.TopicInfo;

/**
 * @author Mikhail_Vershkov
 */

public class TopicInfoGenerator {

    public static Map<String,TopicInfo> generate(Map<String,RecordConfig> configMap) {
        return configMap.entrySet().stream().map(
                record -> (TopicInfo)TopicSearchCriteriaUtils.generateTopicInfo(record.getKey(),
                                                                                record.getValue().getPartitionNumber(),
                                                                                record.getValue().getReplicationFactor(),
                                                                                record.getValue().getConfig(),
                                                                                record.getValue().getDescription(),
                                                                                record.getValue().isIsr()))
                              .collect(Collectors.toMap(TopicInfo::getName, Function.identity()));
    }

    public static class RecordConfig {
        private final String topicName;
        private final int partitionNumber;
        private final int replicationFactor;
        private final Map<String, String> config;
        private final String description;
        boolean isr;

        public RecordConfig(String topicName, int partitionNumber, int replicationFactor, Map<String, String> config, String description, boolean isr) {
            this.topicName = topicName;
            this.partitionNumber = partitionNumber;
            this.replicationFactor = replicationFactor;
            this.config = config;
            this.description = description;
            this.isr = isr;
        }

        public String getTopicName() {
            return topicName;
        }

        public int getPartitionNumber() {
            return partitionNumber;
        }

        public int getReplicationFactor() {
            return replicationFactor;
        }

        public Map<String, String> getConfig() {
            return config;
        }

        public String getDescription() {
            return description;
        }

        public boolean isIsr() {
            return isr;
        }
    }

}
