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
package com.epam.eco.kafkamanager.ui.topics.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.epam.eco.kafkamanager.TopicInfo;

/**
 * @author Andrei_Tytsik
 */
public interface TopicExporter {

    public static final String HEADER_NAME = "Name";
    public static final String HEADER_PARTITION_COUNT = "Partition Count";
    public static final String HEADER_REPLICATION_FACTOR = "Replcation Factor";
    public static final String HEADER_CONFIG = "Config";
    public static final String HEADER_DESCRIPTION = "Description";
    public static final String HEADER_PARTITIONS = "Partitions";
    public static final String HEADER_PARTITION = "Partition";
    public static final String HEADER_PARTITION_LEADER = "Leader";
    public static final String HEADER_PARTITION_REPLICAS = "Replicas";
    public static final String HEADER_PARTITION_ISR = "Isr";

    public static final String[] HEADERS = new String[] {
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

    public static final String KEY_NAME = "name";
    public static final String KEY_PARTITION_COUNT = "partitionCount";
    public static final String KEY_REPLICATION_FACTOR = "replicationFactor";
    public static final String KEY_CONFIG = "config";
    public static final String KEY_DESCRIPTION = "description";
    public static final String KEY_PARTITIONS = "partitions";
    public static final String KEY_PARTITION = "partition";
    public static final String KEY_PARTITION_LEADER = "leader";
    public static final String KEY_PARTITION_REPLICAS = "replicas";
    public static final String KEY_PARTITION_ISR = "isr";

    void export(Collection<TopicInfo> topicInfos, Writer out) throws IOException;

}
