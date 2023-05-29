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

import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.FilterClause;


public abstract class FilterOperationHandler<T> {

    private final FilterClause clause;

    public FilterOperationHandler(FilterClause clause) {
        this.clause = clause;
    }

    public FilterClause getClause() {
        return clause;
    }

    public boolean compare(T value) {
        Validate.notNull(value,"Value is null!");
        FilterOperationEnum filterOperationEnum = FilterOperationEnum.getOperationEnum(clause.getOperation());
        switch (filterOperationEnum) {
            case EQUALS:
                return equalValues(value);
            case CONTAINS:
                return contains(value);
            case STARTS_WITH:
                return startWith(value);
            case LIKE:
                return like(value);
            case NOT_EMPTY:
                return notEmpty(value);
        }
        return false;
    }

    abstract boolean equalValues(T value);
    abstract boolean contains(T value);
    abstract boolean startWith(T value);
    abstract boolean like(T value);
    abstract boolean notEmpty(T value);

}
