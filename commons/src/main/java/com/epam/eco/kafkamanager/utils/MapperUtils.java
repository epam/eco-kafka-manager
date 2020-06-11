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
package com.epam.eco.kafkamanager.utils;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


/**
 * @author Andrei_Tytsik
 */
public class MapperUtils { // TODO: better exception handling.

    private static final ObjectMapper MAPPER = new ObjectMapper();
    static {
        MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    public static byte[] toBytes(Object value) {
        try {
            return MAPPER.writeValueAsBytes(value);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static String toJson(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static String toPrettyJson(String json) {
        try {
            Object jsonObject = MAPPER.readValue(json, Object.class);
            return toPrettyJson(jsonObject);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static String toPrettyJson(Object value) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(value);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static Map<String, Object> jsonToMap(String json) {
        try {
            return MAPPER.reader().forType(Map.class).readValue(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static <T> T jsonToBean(String json, Class<T> type) {
        try {
            return MAPPER.reader().forType(type).readValue(json);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static <T> T bytesToObject(byte[] bytes, Class<T> type) {
        try {
            return MAPPER.reader().forType(type).readValue(bytes);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> bytesToMap(byte[] bytes) {
        return bytesToObject(bytes, Map.class);
    }

    public static <T> T convert(Object object, Class<T> type) {
        return MAPPER.convertValue(object, type);
    }

    public static void writeAsJson(Writer out, Object value) {
        try {
            MAPPER.writeValue(out, value);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static void writeAsPrettyJson(Writer out, Object value) {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(out, value);
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
