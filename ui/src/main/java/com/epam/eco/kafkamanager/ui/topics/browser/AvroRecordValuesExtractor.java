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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.KafkaExtendedDeserializerUtils;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class AvroRecordValuesExtractor {

    public static final String NA = "N/A";
    public static final String SEPARATOR = ".";

    public static Map<String, Object> getValuesAsMap(ConsumerRecord<?, Object> record) {
        Validate.notNull(record, "Record is null");
        Map<String, Object> map = KafkaExtendedDeserializerUtils.extractValuesAsMap(record);
        if(isNull(map) || map.isEmpty()) {
            Object genericRecord = KafkaExtendedDeserializerUtils.extractGenericRecordOrValue(record);
            return nonNull(genericRecord) ? doConvert(null, genericRecord) : null;
        } else {
            return map;
        }
    }

    private static Map<String, Object> doConvert(String path, Object value) {
        if (value instanceof GenericRecord) {
            return doConvertRecord(path, (GenericRecord)value);
        } else if (value instanceof Map) {
            return doConvertMap(path, (Map)value);
        } else if (value instanceof List) {
            return doConvertList(path, (List)value);
        } else {
            return doConvertOther(path, value);
        }
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, Object> doConvertList(String path, List list) {
        return doConvertOther(path, list);
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, Object> doConvertMap(String path, Map map) {
        return doConvertOther(path, map);
    }

    private static Map<String, Object> doConvertRecord(String path, GenericRecord record) {
        Map<String, Object> tabular = new LinkedHashMap<>();
        for (Schema.Field field : record.getSchema().getFields()) {
            String name = field.name();
            Object value = record.get(name);
            tabular.putAll(
                    doConvert(
                            append(path, name),
                            value));
        }
        return tabular;
    }

    private static Map<String, Object> doConvertOther(String path, Object value) {
        return Collections.singletonMap(
                orDefault(path, () -> value != null ? value.getClass().getSimpleName() : NA),
                value);
    }

    private static String orDefault(String value, Supplier<String> defaultValue) {
        return value != null ? value : defaultValue.get();
    }

    private static String append(String parent, String path) {
        return parent != null ? parent + SEPARATOR + path : path;
    }

}