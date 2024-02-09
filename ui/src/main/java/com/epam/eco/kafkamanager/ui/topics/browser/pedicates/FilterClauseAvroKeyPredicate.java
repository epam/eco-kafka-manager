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
package com.epam.eco.kafkamanager.ui.topics.browser.pedicates;

import java.util.List;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseAvroKeyPredicate extends FilterClauseAbstractKeyPredicate<String, Object> {


    public FilterClauseAvroKeyPredicate(List<FilterClause> clauses) {
        super(clauses);
    }

    @Override
    boolean executeOperation(FilterClause clause, Object value) {
        return FilterOperationUtils.executeAvroJsonOperation(clause,value);
    }
}
