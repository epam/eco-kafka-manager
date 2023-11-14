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

import com.epam.eco.kafkamanager.TopicInfo;

/**
 * @author Andrei_Tytsik
 */
public interface TopicExporter {

    String HEADER_NAME = "Name";
    String HEADER_PARTITION_COUNT = "Partition Count";
    String HEADER_REPLICATION_FACTOR = "Replcation Factor";
    String HEADER_CONFIG = "Config";
    String HEADER_DESCRIPTION = "Description";
    String HEADER_PARTITIONS = "Partitions";
    String HEADER_PARTITION = "Partition";
    String HEADER_PARTITION_LEADER = "Leader";
    String HEADER_PARTITION_REPLICAS = "Replicas";
    String HEADER_PARTITION_ISR = "Isr";

    String[] HEADERS = new String[] {
            HEADER_NAME,
            HEADER_PARTITION_COUNT,
            HEADER_REPLICATION_FACTOR,
            HEADER_CONFIG,
            HEADER_DESCRIPTION,
            HEADER_PARTITIONS,
            HEADER_PARTITION,
            HEADER_PARTITION_LEADER,
            HEADER_PARTITION_REPLICAS,
            HEADER_PARTITION_ISR
    };

    String KEY_NAME = "name";
    String KEY_PARTITION_COUNT = "partitionCount";
    String KEY_REPLICATION_FACTOR = "replicationFactor";
    String KEY_CONFIG = "config";
    String KEY_DESCRIPTION = "description";
    String KEY_PARTITIONS = "partitions";
    String KEY_PARTITION = "partition";
    String KEY_PARTITION_LEADER = "leader";
    String KEY_PARTITION_REPLICAS = "replicas";
    String KEY_PARTITION_ISR = "isr";

    void export(Collection<TopicInfo> topicInfos, Writer out) throws IOException;

}
