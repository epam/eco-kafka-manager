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

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.KafkaExtendedDeserializerUtils;

/**
 * @author Mikhail_Vershkov
 */

public class AvroRecordValuesExtractor {

    public static final String NA = "N/A";
    public static final String SEPARATOR = ".";

    public static Map<String, Object> getValuesAsFlattenedMap(ConsumerRecord<?, Object> record) {
        Validate.notNull(record, "Record is null");

        if (record.value() == null) {
            return null;
        }

        Map<String, Object> map = KafkaExtendedDeserializerUtils.extractValuesAsMap(record);
        if (map == null) {
            // Value is not a GenericRecord or was not deserialized by KafkaExtendedAvroDeserializer
            return doConvertToMap(null, record.value());
        } else {
            return flatValues(map, new LinkedHashMap<>(), null);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Map<String, Object> flatValues(Map<String, Object> map,
                                         Map<String, Object> flattenedMap,
                                         String path
    ) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String valuePath = append(path, entry.getKey());
            if (entry.getValue() instanceof Map valueMap) {
                flatValues(valueMap, flattenedMap, valuePath);
            } else {
                flattenedMap.put(valuePath, entry.getValue());
            }
        }
        return flattenedMap;
    }

    @SuppressWarnings("rawtypes")
    private static Map<String, Object> doConvertToMap(String path, Object value) {
        if (value instanceof GenericRecord generic) {
            return doConvertRecord(path, generic);
        } else if (value instanceof Map map) {
            return doConvertMap(path, map);
        } else if (value instanceof List list) {
            return doConvertList(path, list);
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
                    doConvertToMap(append(path, name), value)
            );
        }
        return tabular;
    }

    private static Map<String, Object> doConvertOther(String path, Object value) {
        return Collections.singletonMap(getValuePath(path, value), value);
    }

    private static String getValuePath(String path, Object value) {
        if (path != null) {
            return path;
        } else if (value != null) {
            return value.getClass().getSimpleName();
        } else {
            return NA;
        }
    }

    private static String append(String path, Object fieldName) {
        return path == null ? fieldName.toString() : path + SEPARATOR + fieldName;
    }

}