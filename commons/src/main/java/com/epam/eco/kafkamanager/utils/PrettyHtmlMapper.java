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
package com.epam.eco.kafkamanager.utils;

import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * @author Mikhail_Vershkov
 */
public class PrettyHtmlMapper {

    public static final int PADDING = 4;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        MAPPER.registerModule(new JavaTimeModule());
    }

    public static String toPrettyHtml(Map<String, ?> map) {
        return toPrettyHtml(map, PADDING);
    }

    public static String toPrettyHtml(Map<String, ?> map, int identity) {
        String spaces = StringUtils.repeat("&nbsp;", identity);
        String spaces2 = StringUtils.repeat("&nbsp;", identity - PADDING < 0 ? identity : identity - PADDING);
        return "{<br/>" + map.entrySet()
                .stream()
                .map(entry -> spaces + "\"<b>" + entry.getKey() + "</b>\": " + objectToString(entry.getValue(), identity))
                .collect(Collectors.joining(", <br/>")) + "<br/>" + spaces2 + "}";
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static String objectToString(Object object, int identity) {
        String result;
        if (object instanceof Map) {
            return toPrettyHtml((Map) object, identity + PADDING);
        } else {
            try {
                result = MAPPER.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
    }


}
