package com.epam.eco.kafkamanager.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.stream.Collectors;

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
