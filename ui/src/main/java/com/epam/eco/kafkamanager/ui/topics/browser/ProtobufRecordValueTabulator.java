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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import com.epam.eco.kafkamanager.TopicRecordFetchParams;

/**
 * @author Andrei_Tytsik
 */
public class ProtobufRecordValueTabulator implements RecordValueTabulator<Object> {

    public static final String NA = "N/A";
    public static final String SEPARATOR = ".";

    private final Config kafkaTopicConfig;

    public ProtobufRecordValueTabulator(Config kafkaTopicConfig) {
        this.kafkaTopicConfig = kafkaTopicConfig;
    }

    @Override
    public Map<String, Object> toTabularValue(ConsumerRecord<?, Object> record) {
        Validate.notNull(record, "Record is null");

        return record.value()!=null ? doConvert(null, record.value()) : null;
    }

    @Override
    public Map<String, Object> getAttributes(ConsumerRecord<?, Object> record) {
        Validate.notNull(record, "Record is null");

        if(record.value()==null || !(record.value() instanceof DynamicMessage)) {
            return null;
        }

        // Schema schema = ((GenericContainer) record.value()).getSchema();
        DynamicMessage message = (DynamicMessage) record.value();

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("fullName", message.getDescriptorForType().getName());
        attributes.put("type", message.getDescriptorForType().toString());
        message.getAllFields().keySet().stream()
                .filter(key -> "docId".equals(key.getName()))
                .forEach(key -> attributes.put("doc", message.getAllFields().get(key)));

        message.getAllFields().keySet().stream()
                .filter(key -> !"docId".equals(key.getName()))
                .forEach(key -> attributes.put(key.getName(), message.getAllFields().get(key)));

        return attributes;
    }

    @Override
    public RegistrySchema getSchema(ConsumerRecord<?, ?> record) {
        DynamicMessage message = (DynamicMessage) record.value();
        return new RegistrySchema(record, message.getDescriptorForType().getFullName(), kafkaTopicConfig, TopicRecordFetchParams.DataFormat.PROTOCOL_BUFFERS);
    }

    @Override
    public Config getKafkaTopicConfig() {
        return this.kafkaTopicConfig;
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Object> doConvert(String path, Object value) {
        if(value instanceof DynamicMessage) {
            return doConvertRecord(path, (DynamicMessage) value);
        } else if(value instanceof Map) {
            return doConvertMap(path, (Map) value);
        } else if(value instanceof List) {
            return doConvertList(path, (List) value);
        } else {
            return doConvertOther(path, value);
        }
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Object> doConvertList(String path, List list) {
        return doConvertOther(path, list);
    }

    @SuppressWarnings("rawtypes")
    private Map<String, Object> doConvertMap(String path, Map map) {
        return doConvertOther(path, map);
    }

    private Map<String, Object> doConvertRecord(String path, DynamicMessage message) {
        Map<String, Object> tabular = new LinkedHashMap<>();
        for(Descriptors.FieldDescriptor field : message.getAllFields().keySet()) {
            String name = field.getName();
            Object value = message.getAllFields().get(field);
            tabular.putAll(
                    doConvert(
                            append(path, name),
                            value));
        }
        return tabular;
    }

    private Map<String, Object> doConvertOther(String path, Object value) {
        return Collections.singletonMap(
                orDefault(path, () -> value!=null ? value.getClass().getSimpleName() : NA),
                value);
    }

    private String orDefault(String value, Supplier<String> defaultValue) {
        return value!=null ? value : defaultValue.get();
    }

    private String append(String parent, String path) {
        return parent!=null ? parent + SEPARATOR + path : path;
    }

}
