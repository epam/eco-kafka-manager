package com.epam.eco.kafkamanager.ui.utils;
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

import org.apache.kafka.clients.admin.Config;

import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class SchemaSubjectUtils {

    public static final String KEY_STRATEGY_PROPERTY = "confluent.key.subject.name.strategy";
    public static final String VALUE_STRATEGY_PROPERTY = "confluent.value.subject.name.strategy";

    public static final String TOPIC_NAME_STRATEGY = "io.confluent.kafka.serializers.subject.TopicNameStrategy";
    public static final String RECORD_NAME_STRATEGY = "io.confluent.kafka.serializers.subject.RecordNameStrategy";
    public static final String TOPIC_RECORD_NAME_STRATEGY = "io.confluent.kafka.serializers.subject.TopicRecordNameStrategy";

    public static String getSchemaSubjectKey(String topicName, String recordName, Config kafkaTopicConfig) {
        if(nonNull(kafkaTopicConfig.get(KEY_STRATEGY_PROPERTY))) {
            String strategy = kafkaTopicConfig.get(KEY_STRATEGY_PROPERTY).value();
            if(strategy.equals(TOPIC_NAME_STRATEGY)) {
                return topicName + "-key";
            } else if(strategy.equals(RECORD_NAME_STRATEGY)) {
                return recordName;
            } else if(strategy.equals(TOPIC_RECORD_NAME_STRATEGY)) {
                return topicName + "-" + recordName;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

    public static String getSchemaSubjectValue(String topicName, String recordName, Config kafkaTopicConfig) {
        if(nonNull(kafkaTopicConfig.get(VALUE_STRATEGY_PROPERTY))) {
            String strategy = kafkaTopicConfig.get(KEY_STRATEGY_PROPERTY).value();
            if(strategy.equals(TOPIC_NAME_STRATEGY)) {
                return topicName + "-value";
            } else if(strategy.equals(RECORD_NAME_STRATEGY)) {
                return recordName;
            } else if(strategy.equals(TOPIC_RECORD_NAME_STRATEGY)) {
                return topicName + "-" + recordName;
            } else {
                return "";
            }
        } else {
            return "";
        }
    }

}
