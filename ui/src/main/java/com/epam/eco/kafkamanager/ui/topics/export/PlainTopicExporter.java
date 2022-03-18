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
package com.epam.eco.kafkamanager.ui.topics.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PartitionInfo;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class PlainTopicExporter implements TopicExporter {

    @Override
    public void export(Collection<TopicInfo> topicInfos, Writer out) throws IOException {
        for (TopicInfo topicInfo : topicInfos) {
            out.
                append(HEADER_NAME).append(": ").
                    append(topicInfo.getName()).append(" ").
                append(HEADER_PARTITION_COUNT).append(": ").
                    append("" + topicInfo.getPartitionCount()).append(" ").
                append(HEADER_REPLICATION_FACTOR).append(": ").
                    append("" + topicInfo.getReplicationFactor()).append(" ").
                append(HEADER_CONFIG).append(": ").
                    append(topicInfo.getConfig() != null ? MapperUtils.toJson(topicInfo.getConfig()) : "").append(" ").
                append(HEADER_DESCRIPTION).append(": ").append(
                        topicInfo.getMetadata().map(Metadata::getDescription).orElse("")).append("\n");

            for (PartitionInfo partitionInfo : topicInfo.getPartitions().values()) {
                out.
                    append("\t").
                    append(HEADER_PARTITION).append(": ").
                        append("" + partitionInfo.getId().partition()).append(" ").
                    append(HEADER_PARTITION_LEADER).append(": ").
                        append("" + partitionInfo.getLeader()).append(" ").
                    append(HEADER_PARTITION_REPLICAS).append(": ").
                        append(MapperUtils.toJson(partitionInfo.getReplicas())).append(" ").
                    append(HEADER_PARTITION_ISR).append(": ").
                        append(MapperUtils.toJson(partitionInfo.getIsr())).append("\n");
            }
        }
    }

}
