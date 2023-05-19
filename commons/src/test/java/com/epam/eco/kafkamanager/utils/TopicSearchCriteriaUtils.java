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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.TopicPartition;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PartitionInfo;
import com.epam.eco.kafkamanager.TopicInfo;

/**
 * @author Mikhail_Vershkov
 */

public class TopicSearchCriteriaUtils {

    public static final Map<String,String> CONFIG_MAP_COMPACT = Map.of("cleanup.policy","compact", "min.cleanable.dirty.ratio", "0.1");
    public static final Map<String,String> CONFIG_MAP_DELETE = Map.of("cleanup.policy","delete", "min.cleanable.dirty.ratio", "0.5");

    public static TopicInfo generateTopicInfo(String topicName,
                                        int partitionNumber,
                                        int replicationFactor,
                                        Map<String, String> config,
                                        String description) {
        return generateTopicInfo(topicName,partitionNumber,replicationFactor,config, description, true);
    }
    public static TopicInfo generateTopicInfo(String topicName,
                                        int partitionNumber,
                                        int replicationFactor,
                                        Map<String, String> config,
                                        String description,
                                        boolean isr) {

        Map<TopicPartition, PartitionInfo> partitionInfoMap = new HashMap<>();
        List<Integer> replicas = new ArrayList<>();
        for(int ii=0;ii<replicationFactor;ii++) {
            replicas.add(ii);
        }

        for(int ii=0;ii<partitionNumber;ii++) {
            TopicPartition topicPartition = new TopicPartition(topicName,ii);
            partitionInfoMap.put(topicPartition, new PartitionInfo(topicPartition, replicas ,ii, isr ? List.of(ii) : List.of()));
        }

        return new TopicInfo(topicName, partitionInfoMap, config, new Metadata(description, Map.of(), new Date(), "User"));
    }
}
