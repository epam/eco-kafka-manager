/*******************************************************************************
 *  Copyright 2024 EPAM Systems
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
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.AvroRecordValuesExtractor;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils;

import static com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils.executeAvroJsonOperation;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseAvroValuePredicate extends FilterClauseAbstractValuePredicate<String, Object> {


    public FilterClauseAvroValuePredicate(List<FilterClause> clauses) {
        super(clauses);
    }

    @Override
    protected boolean processValueClauses(ConsumerRecord<String,Object> record) {
        if (clauses.isEmpty()) {
            return true;
        }
        Map<String, Object> mapOfValues = AvroRecordValuesExtractor.getValuesAsMap(record);
        for (FilterClause filterClause : clauses) {
            if (mapOfValues != null && mapOfValues.containsKey(filterClause.getColumn())) {
                Object value = mapOfValues.get(filterClause.getColumn());
                if (!executeAvroJsonOperation(filterClause, value)) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    @Override
    boolean executeOperation(FilterClause clause, Object value) {
        return FilterOperationUtils.executeAvroJsonOperation(clause,value);
    }
}
