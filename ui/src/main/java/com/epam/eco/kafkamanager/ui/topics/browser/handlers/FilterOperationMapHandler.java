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

import org.apache.avro.util.Utf8;

import com.epam.eco.kafkamanager.FilterClause;

import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.KEY_VALUE_SEPARATOR;

/**
 * @author Mikhail_Vershkov
 */

public class FilterOperationMapHandler extends FilterOperationHandler<Map<Object,Object>> {
    private final static String DEFAULT_VALUE = "";
    private final String propertyName;

    public FilterOperationMapHandler(FilterClause clause) {
        super(clause);
        propertyName = getFilterPropertyName(clause);
    }
    @Override
    boolean equalValues(Map<Object,Object> value) {
        if(value.isEmpty()) {
            return false;
        }
        if(areValuesUtf8(value)) {
            if(propertyName.length()>0) {
                return FilterOperationUtils.strip(value.getOrDefault(new Utf8(propertyName), new Utf8(DEFAULT_VALUE)).toString())
                            .equals(getFilterPropertyValue(getClause()));
            }
            return value.containsValue(new Utf8(getClause().getValue()));
        } else {
            if(propertyName.length() > 0) {
                return value.getOrDefault(propertyName, DEFAULT_VALUE).toString().equals(
                        getFilterPropertyValue(getClause()));
            }
            return value.containsValue(getClause().getValue());
        }
    }
    @Override
    boolean contains(Map<Object,Object> value) {

        if(value.isEmpty()) {
            return false;
        }

        if(propertyName.isEmpty()) {
            return value.values().stream().anyMatch(
                    fieldValue -> fieldValue.toString().contains(getClause().getValue()));
        }

        if(areValuesUtf8(value)) {
            return value.getOrDefault(new Utf8(propertyName), new Utf8(DEFAULT_VALUE)).toString().contains(
                   getFilterPropertyValue(getClause()));
        } else {
            return value.getOrDefault(propertyName, DEFAULT_VALUE).toString().contains(
                   getFilterPropertyValue(getClause()));
        }

    }
    @Override
    boolean startWith(Map<Object,Object> value) {

        if(value.isEmpty()) {
            return false;
        }

        if(propertyName.isEmpty()) {
            return value.values().stream().anyMatch(
                    fieldValue -> fieldValue.toString().startsWith(getClause().getValue()));
        }

        if(areValuesUtf8(value)) {
            return value.getOrDefault(new Utf8(propertyName), new Utf8(DEFAULT_VALUE)).toString().startsWith(
                        getFilterPropertyValue(getClause()));
        } else {
            return value.getOrDefault(propertyName, DEFAULT_VALUE).toString().startsWith(
                        getFilterPropertyValue(getClause()));
        }

    }

    @Override
    boolean like(Map<Object, Object> value) {
        if(value.isEmpty()) {
            return false;
        }

        if(propertyName.isEmpty()) {
            return value.values().stream().anyMatch(
                    fieldValue -> FilterOperationUtils.like(getClause().getValue(), fieldValue.toString()));
        }

        if(areValuesUtf8(value)) {
            return FilterOperationUtils.like(getFilterPropertyValue(getClause()),
                                             value.getOrDefault(new Utf8(propertyName), new Utf8(DEFAULT_VALUE)).toString());
        } else {
            return FilterOperationUtils.like(getFilterPropertyValue(getClause()),value.getOrDefault(propertyName, DEFAULT_VALUE).toString());
        }
    }

    @Override
    boolean notEmpty(Map<Object, Object> value) {
        if(value.isEmpty()) {
            return false;
        }

        if(propertyName.isEmpty()) {
            return value.values().stream().anyMatch(
                    fieldValue -> FilterOperationUtils.notEmpty(getClause().getValue(), fieldValue.toString()));
        }

        if(areValuesUtf8(value)) {
            return FilterOperationUtils.notEmpty(getFilterPropertyValue(getClause()),
                                             value.getOrDefault(new Utf8(propertyName), new Utf8(DEFAULT_VALUE)).toString());
        } else {
            return FilterOperationUtils.notEmpty(getFilterPropertyValue(getClause()),value.getOrDefault(propertyName, DEFAULT_VALUE).toString());
        }
    }

    private String getFilterPropertyName(FilterClause clause) {
        String propertyName = "";
        if(clause.getValue().contains(KEY_VALUE_SEPARATOR)) {
            propertyName = clause.getValue().substring(0,clause.getValue().indexOf(KEY_VALUE_SEPARATOR));
        }
        return propertyName;
    }
    private String getFilterPropertyValue(FilterClause clause) {
        String propertyName = clause.getValue();
        if(clause.getValue().contains(KEY_VALUE_SEPARATOR)) {
            propertyName = clause.getValue().substring(clause.getValue().indexOf(KEY_VALUE_SEPARATOR)+1);
        }
        return propertyName;
    }
    private boolean areValuesUtf8(Map<Object,Object> value) {
        if(value.isEmpty()) {
            return false;
        }
        return value.values().stream().anyMatch(Utf8.class::isInstance);
    }
}
