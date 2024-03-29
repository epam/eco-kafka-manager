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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.HashMap;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.KafkaSchemaIdAwareUtils;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils;

/**
 * @author Andrei_Tytsik
 */
public class AvroRecordValueTabulator implements RecordValueTabulator<Object> {

    private final Config topicConfig;

    public AvroRecordValueTabulator(Config topicConfig) {
        Validate.notNull(topicConfig, "Topic config is null");

        this.topicConfig = topicConfig;
    }

    public Config getKafkaTopicConfig() {
        return topicConfig;
    }

    @Override
    public Map<String, Object> toTabularValue(ConsumerRecord<?, Object> record) {
        return AvroRecordValuesExtractor.getValuesAsMap(record);
    }

    @Override
    public Map<String, Object> getAttributes(ConsumerRecord<?, Object> record) {
        Validate.notNull(record, "Record is null");

        if (record.value() == null) {
            return null;
        }

        Map<String, Object> attributes = new HashMap<>();

        Object object = KafkaSchemaIdAwareUtils.extractGenericRecordOrValue(record);

        if(object instanceof GenericRecord genericRecord) {

            Schema schema = genericRecord.getSchema();

            attributes.put("fullName", schema.getFullName());
            attributes.put("type", schema.getType());
            if(schema.getDoc() != null) {
                attributes.put("doc", schema.getDoc());
            }
            attributes.putAll(schema.getObjectProps());
        }
        return attributes;
    }

    @Override
    public RecordSchema getSchema(ConsumerRecord<?, ?> record) {
        Validate.notNull(record, "Record is null");

        if (record.value() == null) {
            return null;
        }

        Object object = KafkaSchemaIdAwareUtils.extractGenericRecordOrValue(record);

        if(object instanceof GenericRecord genericRecord) {

            Schema schema = genericRecord.getSchema();

            String schemaName = schema.getFullName();
            String schemaKey = SchemaSubjectUtils.getSchemaSubjectKey(record.topic(), schemaName, topicConfig);
            String schemaValue = SchemaSubjectUtils.getSchemaSubjectValue(record.topic(), schemaName, topicConfig);
            String schemaAsString = schema.toString(true);

            return new RecordSchema(KafkaSchemaIdAwareUtils.extractSchemaId(record), schemaName, schemaKey, schemaValue,
                                    schemaAsString, TopicRecordFetchParams.DataFormat.AVRO);
        } else {
            return RecordSchema.DUMMY_AVRO_RECORD_SCHEMA;
        }

    }

}
