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

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.epam.eco.kafkamanager.FilterOperation;

/**
 * @author Mikhail_Vershkov
 */

public enum FilterOperationEnum {

    EQUALS(new FilterOperation("equals", "equals", "Any string")),
    CONTAINS(new FilterOperation("contains", "contains", "Any string")),
    STARTS_WITH(new FilterOperation("startsWith", "starts with", "Any string")),
    LIKE(new FilterOperation("like", "like", "%string%string%")),
    NOT_EMPTY(new FilterOperation("notEmpty", "not empty", "Leave it blank"));

    private final FilterOperation operation;

    FilterOperationEnum(FilterOperation operation) {
        this.operation = operation;
    }

    public FilterOperation getOperation() {
        return operation;
    }

    public static FilterOperationEnum getOperationEnum(FilterOperation filterOperation) {
        return Arrays.stream(FilterOperationEnum.values())
                     .filter(filterOperationEnum -> filterOperationEnum.getOperation().equals(filterOperation))
                     .findAny().orElseThrow();
    }

    public static Collection<FilterOperation> getFilterOperations() {
        return Arrays.stream(FilterOperationEnum.values()).map(
                FilterOperationEnum::getOperation).collect(Collectors.toList());
    }

}