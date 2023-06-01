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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.commons.kafka.helpers.FilterClausePredicate;
import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationStringHandler;

import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.KEY_ATTRIBUTE;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseStringPredicate<K,V> implements FilterClausePredicate<K,V> {

    private final boolean areClausesEmpty;
    private final List<FilterClause> keyClauses;
    private final List<FilterClause> otherClauses;
    public FilterClauseStringPredicate(Map<String, List<FilterClause>> clauses) {
        areClausesEmpty = clauses.isEmpty();
        keyClauses = clauses.getOrDefault(KEY_ATTRIBUTE, Collections.emptyList());
        this.otherClauses = clauses.entrySet().stream()
                                   .filter(clause->!KEY_ATTRIBUTE.equals(clause.getKey()))
                                   .flatMap(clause -> clause.getValue().stream())
                                   .collect(Collectors.toList());
    }

    @Override
    public boolean test(ConsumerRecord<K,V> record) {
        if(areClausesEmpty) {
            return true;
        } else {

            boolean result = true;

            if(!keyClauses.isEmpty()) {
                for(FilterClause clause : keyClauses) {
                    if(nonNull(clause.getValue())) {
                        result = result && executeOperation(clause, record.key().toString());
                    } else {
                        return false;
                    }
                }
                if(isNull(record.value())) {
                    return result;
                }
            }
            if(!result) return false;


            if(!otherClauses.isEmpty()) {

                String value = (String)record.value();

                for(FilterClause filterClause : otherClauses) {
                    if(nonNull(value)) {
                        result = result && executeOperation(filterClause, value);
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
    }

    private boolean executeOperation(FilterClause filterClause, Object value) {
        if(isNull(value)) {
            return false;
        }
        return new FilterOperationStringHandler(filterClause).compare(value.toString());
    }

}