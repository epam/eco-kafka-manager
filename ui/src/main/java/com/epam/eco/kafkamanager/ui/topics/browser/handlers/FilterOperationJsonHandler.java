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

import java.util.function.BiPredicate;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.epam.eco.kafkamanager.FilterClause;

import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.KEY_VALUE_SEPARATOR;
import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.PROPERTIES_SEPARATOR;

/**
 * @author Mikhail_Vershkov
 */

public class FilterOperationJsonHandler extends FilterOperationHandler<String> {
    private final ObjectMapper mapper;
    public FilterOperationJsonHandler(FilterClause clause) {
        super(clause);
        mapper = new ObjectMapper();
    }
    @Override
    boolean equalValues(String value) {
        try {
            JsonNode node = mapper.readTree(value);
            return compareValue(node,
                                getFieldName(getClause().getValue()),
                                getFieldValue(getClause().getValue()),
                                String::equals);
        } catch (JsonProcessingException e) {
            return value.equals(getClause().getValue());
        }
    }
    @Override
    boolean contains(String value) {
        try {
            JsonNode node = mapper.readTree(value);
            return compareValue(node,
                                getFieldName(getClause().getValue()),
                                getFieldValue(getClause().getValue()),
                                String::contains);
        } catch (JsonProcessingException e) {
            return value.contains(getClause().getValue());
        }
    }
    @Override
    boolean startWith(String value) {
        try {
            JsonNode node = mapper.readTree(value);
            return compareValue(node,
                                getFieldName(getClause().getValue()),
                                getFieldValue(getClause().getValue()),
                                String::startsWith);
        } catch (JsonProcessingException e) {
            return value.startsWith(getClause().getValue());
        }
    }

    @Override
    boolean like(String value) {
        try {
            JsonNode node = mapper.readTree(value);
            return compareValue(node,
                                getFieldName(getClause().getValue()),
                                getFieldValue(getClause().getValue()),
                                FilterOperationUtils::like);
        } catch (JsonProcessingException e) {
            return FilterOperationUtils.like(getClause().getValue(), value);
        }
    }

    @Override
    boolean notEmpty(String value) {
        try {
            JsonNode node = mapper.readTree(value);
            return compareValue(node,
                                getFieldName(getClause().getValue()),
                                getFieldValue(getClause().getValue()),
                                (filterClause,testValue)->FilterOperationUtils.notEmpty(testValue));
        } catch (JsonProcessingException e) {
            return FilterOperationUtils.like(getClause().getValue(), value);
        }
    }

    private String getFieldName(String value) {
       Validate.notNull(value);
       return value.substring(0,value.indexOf(KEY_VALUE_SEPARATOR)).trim();
    }
    private String getFieldValue(String value) {
        Validate.notNull(value);
        return value.substring(value.indexOf(KEY_VALUE_SEPARATOR)+1).trim();
    }

    private boolean compareValue(JsonNode node,
                                 String fieldName,
                                 String testValue,
                                 BiPredicate<String,String> compareMethod) {
        if(node.isArray()) {
            ArrayNode arrayNode = (ArrayNode) node;
            for(JsonNode singleNode: arrayNode) {
                boolean result = compareValue(singleNode, fieldName, testValue, compareMethod);
                if(result) {
                    return true;
                }
            }
        } else {
            if(fieldName.contains(PROPERTIES_SEPARATOR)) {
                String currentFieldName = fieldName.substring(0,fieldName.indexOf(PROPERTIES_SEPARATOR));
                if(node.has(currentFieldName)) {
                    String restFieldName = fieldName.substring(fieldName.indexOf(PROPERTIES_SEPARATOR) + 1);
                    return compareValue(node.get(currentFieldName),restFieldName,testValue,compareMethod);
                } else {
                    return false;
                }

            } else {
                return node.has(fieldName) && compareMethod.test(node.get(fieldName).asText(), testValue);
            }
        }
        return false;
    }

}
