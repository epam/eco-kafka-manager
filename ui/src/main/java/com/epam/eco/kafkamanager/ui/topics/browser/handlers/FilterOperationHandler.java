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

import com.epam.eco.kafkamanager.FilterClause;

import static java.util.Objects.isNull;

public abstract class FilterOperationHandler<T> {

    public static final String KEY_VALUE_SEPARATOR = ":";
    public static final String PROPERTIES_SEPARATOR = ".";
    private final FilterClause clause;

    protected FilterOperationHandler(FilterClause clause) {
        this.clause = clause;
    }

    public FilterClause getClause() {
        return clause;
    }

    public boolean compare(T value) {
        FilterOperationEnum filterOperationEnum = FilterOperationEnum.getOperationEnum(clause.getOperation());
        return switch (filterOperationEnum) {
            case EQUALS -> equalValues(value);
            case CONTAINS -> contains(value);
            case STARTS_WITH -> startWith(value);
            case LIKE -> like(value);
            case NOT_EMPTY -> notEmpty(value);
            case EXCLUDE -> exclude(value);
            case ONLY -> only(value);
        };
    }

    abstract boolean equalValues(T value);
    abstract boolean contains(T value);
    abstract boolean startWith(T value);
    abstract boolean like(T value);
    abstract boolean notEmpty(T value);
    protected boolean exclude(T value) {
        return !isNull(value);
    }
    protected boolean only(T value) {
        return isNull(value);
    }

}
