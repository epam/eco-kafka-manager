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
package com.epam.eco.kafkamanager.ui.topics.browser.handlers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.FilterClause;

import static com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationHandler.KEY_VALUE_SEPARATOR;
import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */

public final class FilterOperationUtils {

    private static final String LIKE_SQL_STYLE = "%";
    private static final String LIKE_JAVA_STYLE = "(.)+";

    public static boolean like(String regexp, String testString) {
        if(isNull(testString) || testString.isEmpty()) {
            return false;
        }
        return testString.matches(regexp.replace(LIKE_SQL_STYLE, LIKE_JAVA_STYLE));
    }
    public static boolean notEmpty(String testString) {
        return !StringUtils.isEmpty(testString);
    }

    public static boolean executeAvroJsonOperation(FilterClause filterClause, Object value) {
        if(isNull(value)) {
            return new FilterOperationTombstoneHandler(filterClause).compare(value);
        }
        if(filterClause.getValue().contains(KEY_VALUE_SEPARATOR)) {
            if(value instanceof Map) {
                Map<Object,Object> map = (Map<Object,Object>)value;
                return new FilterOperationMapHandler(filterClause).compare(map);
            } else {
                try {
                    new ObjectMapper().readTree(value.toString());
                    return new FilterOperationJsonHandler(filterClause).compare(value.toString());
                } catch (JsonProcessingException e) {
                    return false;
                }
            }
        } else {
            return new FilterOperationStringHandler(filterClause).compare(value.toString());
        }

    }

    public static boolean executeHeaderOperation(FilterClause filterClause, Map<Object,Object> headersMap) {
        if(filterClause.getValue().contains(KEY_VALUE_SEPARATOR)) {
            return new FilterOperationMapHandler(filterClause).compare(headersMap);
        } else {
            return new FilterOperationStringHandler(filterClause).compare(headersMap.toString());
        }
    }

    public static String stringifyValue(ConsumerRecord<String,Object> record) {
        String value;
        if(record.value() instanceof String) {
            value = (String) record.value();
        } else {
            value = record.value().toString();
        }
        return value;
    }
}
