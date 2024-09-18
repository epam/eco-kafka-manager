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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.KafkaExtendedDeserializerUtils;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.ui.config.TopicBrowser;
import com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils;

import static java.util.Objects.isNull;

/**
 * @author Andrei_Tytsik
 */
public class AvroRecordValueTabulator implements RecordValueTabulator<Object> {

    private static final String NULL_FORMAT = "null";
    private static final String EMPTY_FORMAT = "";

    private final Config topicConfig;
    private final TopicBrowser topicBrowser;

    public AvroRecordValueTabulator(Config topicConfig, TopicBrowser topicBrowser) {
        Validate.notNull(topicConfig, "Topic config is null");

        this.topicConfig = topicConfig;
        this.topicBrowser = topicBrowser;
    }

    public Config getKafkaTopicConfig() {
        return topicConfig;
    }

    @Override
    public Map<String, Object> toTabularValue(ConsumerRecord<?, Object> record) {
        Map<String, Object> map = AvroRecordValuesExtractor.getValuesAsMap(record);
        return isNull(map) ? null : flatValues(map, new HashMap<>(), "");
    }

    public Map<String,Object> flatValues(@Nonnull Map<String, Object> map,
                                         Map<String, Object> flatedMap,
                                         String prefix) {

        for(Map.Entry<String,Object> entry: map.entrySet()) {
            if(entry.getValue() instanceof Map) {
                flatValues((Map)entry.getValue(), flatedMap, entry.getKey());
            } else {
                flatedMap.put( prefixedKey(prefix, entry), convertValue(entry.getValue()) );
            }
        }
        return flatedMap;
    }

    private static String prefixedKey(
            String prefix,
            Map.Entry<String, Object> entry
    ) {
        return prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
    }

    private String convertValue(Object value) {
         if(value instanceof LocalDateTime) {
             return ((LocalDateTime)value).format(DateTimeFormatter.ofPattern(topicBrowser.getDateTimeFormat()));
         } else if(value instanceof LocalDate) {
             return ((LocalDate)value).format(DateTimeFormatter.ofPattern(topicBrowser.getDateFormat()));
         } else if(value instanceof LocalTime) {
             return ((LocalTime)value).format(DateTimeFormatter.ofPattern(topicBrowser.getTimeFormat()));
         } else {
             return isNull(value) ? getNullRepresentation() : value.toString();
         }
    }

    private String getNullRepresentation() {
        return topicBrowser.getEmptyIfNull() ? EMPTY_FORMAT : NULL_FORMAT;
    }

    @Override
    public Map<String, Object> getAttributes(ConsumerRecord<?, Object> record) {
        Validate.notNull(record, "Record is null");

        if (record.value() == null) {
            return null;
        }

        Map<String, Object> attributes = new HashMap<>();

        Object object = KafkaExtendedDeserializerUtils.extractGenericRecordOrValue(record);

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

        Object object = KafkaExtendedDeserializerUtils.extractGenericRecordOrValue(record);

        if(object instanceof GenericRecord genericRecord) {

            Schema schema = genericRecord.getSchema();

            String schemaName = schema.getFullName();
            String schemaKey = SchemaSubjectUtils.getSchemaSubjectKey(record.topic(), schemaName, topicConfig);
            String schemaValue = SchemaSubjectUtils.getSchemaSubjectValue(record.topic(), schemaName, topicConfig);
            String schemaAsString = schema.toString(true);

            return new RecordSchema(KafkaExtendedDeserializerUtils.extractSchemaId(record), schemaName, schemaKey, schemaValue,
                                    schemaAsString, TopicRecordFetchParams.DataFormat.AVRO);
        } else {
            return RecordSchema.DUMMY_AVRO_RECORD_SCHEMA;
        }

    }

}
