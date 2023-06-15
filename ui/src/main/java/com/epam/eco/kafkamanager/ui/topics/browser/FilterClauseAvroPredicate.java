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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils;

import static com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils.executeAvroJsonOperation;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseAvroPredicate<K, V> extends FilterClauseAbstractPredicate<K, V> {


    public FilterClauseAvroPredicate(Map<String, List<FilterClause>> clauses) {
        super(clauses);
    }

    @Override
    protected boolean processValueClauses(ConsumerRecord<K,V> record) {

        boolean result = true;

        if(!otherClauses.isEmpty()) {

            Map<String, Object> mapOfValues = AvroRecordValuesExtractor.getValuesAsMap((ConsumerRecord<?, Object>) record);

            for(FilterClause filterClause : otherClauses) {
                if(nonNull(mapOfValues) && nonNull(mapOfValues.get(filterClause.getColumn()))) {
                    Object value = mapOfValues.get(filterClause.getColumn());
                    result = result && executeAvroJsonOperation(filterClause, value);
                } else {
                    result = false;
                }
                if(!result) {
                    break;
                }
            }
        }
        return result;
    }

    @Override
    boolean executeOperation(FilterClause clause, Object value) {
        return FilterOperationUtils.executeAvroJsonOperation(clause,value);
    }
}
