/*
 */
package com.epam.eco.kafkamanager.ui.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.ui.config.HeaderReplacement;
import com.epam.eco.kafkamanager.ui.config.TopicBrowser;
import com.epam.eco.kafkamanager.ui.topics.browser.ReplacementType;

import static io.micrometer.common.util.StringUtils.isNotEmpty;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */
public class HeaderUtils {

    public static Map<String,String> getReplacedHeaders(Map<String,String> headers,
                                                        List<HeaderReplacement> replacements) {
        return headers.entrySet().stream()
                .map(entry->new AbstractMap.SimpleEntry<>(entry.getKey(), getReplacedHeaderIfExists(entry, replacements)))
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue));
    }

    private static String getReplacedHeaderIfExists(
            Map.Entry<String,String> entry,
            List<HeaderReplacement> replacements
    ) {
        return replacements.stream()
                .filter(replacement -> replacement.getHeaderName()
                        .equals(entry.getKey()) && nonNull(replacement.getReplacement().getValue()))
                .map(replacement -> replacement.getReplacement().getValue().toString())
                .findFirst()
                .orElse(entry.getValue());
    }

    public static Map<String, String> getReplacedHeaderMap(
            ReplacementType replacementType,
            String headers,
            TopicBrowser topicBrowser
    ) throws JsonProcessingException {
        List<HeaderReplacement> replacements = resolveHeaderReplacementsByType(replacementType, topicBrowser);
        return getHeadersAsMap(headers, replacements);
    }

    public static List<HeaderReplacement> resolveHeaderReplacementsByType(
            ReplacementType replacementType,
            TopicBrowser topicBrowser
    ) {
        if(isNull(topicBrowser)) {
            return new ArrayList<>();
        }
        return replacementType.getReplacements().apply(topicBrowser);
    }

    public static Map<String, String> getHeadersAsMap(
            String headers,
            List<HeaderReplacement> replacements
    ) throws JsonProcessingException {
        Map<String, String> headerMap;
        if(isNotEmpty(headers)) {
            headerMap = new ObjectMapper().readValue(headers, HashMap.class);
            if(CollectionUtils.isNotEmpty(replacements)) {
                headerMap = getReplacedHeaders(headerMap, replacements);
            }
        } else {
            headerMap = Collections.emptyMap();
        }
        return headerMap;
    }

}