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

import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class FilterOperationStringHandler extends FilterOperationHandler<String> {

    public FilterOperationStringHandler(FilterClause clause) {
        super(clause);
    }
    @Override
    boolean equalValues(String value) {
        return nonNull(value) && value.trim().equals(getClause().getValue());
    }
    @Override
    boolean contains(String value) {
        return nonNull(value) && value.trim().contains(getClause().getValue());
    }
    @Override
    boolean startWith(String value) {
        return nonNull(value) && value.trim().startsWith(getClause().getValue());
    }

    @Override
    boolean like(String value) {
        return nonNull(value) && FilterOperationUtils.like(getClause().getValue(), value);
    }

    @Override
    boolean notEmpty(String value) {
        return nonNull(value) && FilterOperationUtils.notEmpty(value);
    }
}