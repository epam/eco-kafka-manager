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

import org.apache.avro.util.Utf8;
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

    private static final int PADDING = 4;
    private static final String EMPTY_MAP = "{}";
    private static final String HTML_NEW_LINE = "<br/>";
    private static final String JSON_NEW_LINE = "\n";
    private static final String JSON_START_BOLD = "";
    private static final String HTML_START_BOLD = "<b>";
    private static final String JSON_END_BOLD = "";
    private static final String HTML_END_BOLD = "</b>";
    private static final String JSON_SPACE = " ";
    private static final String HTML_SPACE = "&nbsp;";
    private static final String DELIMITER = "=";


    private static final Map<PrettyFormat, MapperConfig> MAPPER_CONFIG =
            Map.of(PrettyFormat.JSON,
                   new MapperConfig(JSON_NEW_LINE, JSON_START_BOLD, JSON_END_BOLD, JSON_SPACE, DELIMITER),
                   PrettyFormat.HTML,
                   new MapperConfig(HTML_NEW_LINE, HTML_START_BOLD, HTML_END_BOLD, HTML_SPACE, DELIMITER)
                  );

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        MAPPER.registerModule(new JavaTimeModule());
    }

    public static String toPretty(Map<?,?> map, PrettyFormat format) {
        return isMapKeyUtf8(map) ?
               toPrettyUtf8((Map<Utf8, ?>) map,format,PADDING) :
               toPrettyString((Map<String, ?>) map, format, PADDING);
    }

    private static String toPrettyString(Map<String,?> map, PrettyFormat format, int identity) {
        MapperConfig config = MAPPER_CONFIG.get(format);
        String spaces = StringUtils.repeat(config.getSpace(), identity);
        String spaces2 = StringUtils.repeat(config.getSpace(), identity - PADDING < 0 ? identity : identity - PADDING);
        return "{" + config.getNewLine() + map.entrySet()
                                              .stream()
                                              .map(entry -> spaces + "\"" + config.getStartBold() + entry.getKey() + config.getEndBold() + "\" " + config.getDelimiter() + " " +
                                                      objectToString(entry.getValue(), format, identity))
                                              .collect(Collectors.joining(", " + config.getNewLine())) + config.getNewLine() + spaces2 + "}";
    }

    private static String toPrettyUtf8(Map<Utf8,?> map, PrettyFormat format, int identity) {
        MapperConfig config = MAPPER_CONFIG.get(format);
        String spaces = StringUtils.repeat(config.getSpace(), identity);
        String spaces2 = StringUtils.repeat(config.getSpace(), identity - PADDING < 0 ? identity : identity - PADDING);
        return "{" + config.getNewLine() + map.entrySet()
                                              .stream()
                                              .map(entry -> spaces + "\"" + config.getStartBold() + entry.getKey().toString() + config.getEndBold() + "\" " + config.getDelimiter() + " " +
                                                      objectToString(entry.getValue(), format, identity))
                                              .collect(Collectors.joining(", " + config.getNewLine())) + config.getNewLine() + spaces2 + "}";
    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static String objectToString(Object object, PrettyFormat format, int identity) {
        String result;
        if (object instanceof Map) {
            Map mapObject = (Map) object;
            if(mapObject.isEmpty())  {
                return EMPTY_MAP;
            }
            return isMapKeyUtf8(mapObject) ?
                   toPrettyUtf8(mapObject, format,identity + PADDING) :
                   toPrettyString(mapObject, format,identity + PADDING);
        } else if (object instanceof Utf8) {
            try {
                result = MAPPER.writeValueAsString(object.toString());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return result;
        } else {
            try {
                result = MAPPER.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            return result;
        }
    }

    private static boolean isMapKeyUtf8(Map map) {
        return map.keySet().stream().anyMatch(Utf8.class::isInstance);
    }

    public enum PrettyFormat {
        JSON, HTML
    }

    private static class MapperConfig {
        private final String newLine;
        private final String startBold;
        private final String endBold;
        private final String space;

        private final String delimiter;

        public MapperConfig(String newLine, String startBold, String endBold, String space, String delimiter) {
            this.newLine = newLine;
            this.startBold = startBold;
            this.endBold = endBold;
            this.space = space;
            this.delimiter = delimiter;
        }

        public String getNewLine() {
            return newLine;
        }

        public String getStartBold() {
            return startBold;
        }

        public String getEndBold() {
            return endBold;
        }

        public String getSpace() {
            return space;
        }

        public String getDelimiter() {
            return delimiter;
        }
    }

}
