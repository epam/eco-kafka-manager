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
package com.epam.eco.kafkamanager.ui.topics.export;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class JsonTopicExporter implements TopicExporter {

    @Override
    public void export(Collection<TopicInfo> topicInfos, Writer out) throws IOException {
        List<Object> records = new ArrayList<>(topicInfos.size());
        for (TopicInfo topicInfo : topicInfos) {
            records.add(toJsonRecord(topicInfo));
        }
        MapperUtils.writeAsPrettyJson(out, records);
    }

    private Map<String, Object> toJsonRecord(TopicInfo topicInfo) {
        Map<String, Object> record = new LinkedHashMap<>();

        record.put(KEY_NAME, topicInfo.getName());
        record.put(KEY_PARTITION_COUNT, topicInfo.getPartitionCount());
        record.put(KEY_REPLICATION_FACTOR, topicInfo.getReplicationFactor());
        record.put(KEY_CONFIG, topicInfo.getConfigOverrides());
        Metadata metadata = topicInfo.getMetadata().orElse(null);
        record.put(KEY_DESCRIPTION, metadata != null ? metadata.getDescription() : null);

        List<Object> partitionRecords = new ArrayList<>();
        record.put(KEY_PARTITIONS, partitionRecords);

        topicInfo.getPartitions().values().forEach(partitionInfo -> {
            Map<String, Object> partitionRecord = new LinkedHashMap<>();

            partitionRecord.put(KEY_PARTITION, partitionInfo.getId().partition());
            partitionRecord.put(KEY_PARTITION_LEADER, partitionInfo.getLeader());
            partitionRecord.put(KEY_PARTITION_REPLICAS, partitionInfo.getReplicas());
            partitionRecord.put(KEY_PARTITION_ISR, partitionInfo.getIsr());

            partitionRecords.add(partitionRecord);
        });

        return record;
    }

}
