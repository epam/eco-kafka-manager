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
import java.util.function.Predicate;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseTombstonePredicate<K, V> implements Predicate<ConsumerRecord<K, V>> {

    protected final List<FilterClause> clauses;

    public FilterClauseTombstonePredicate(List<FilterClause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public boolean test(ConsumerRecord<K, V> record) {
        if(clauses.isEmpty()) {
            return true;
        }
        return processTombstoneClauses(record);
    }

    private boolean processTombstoneClauses(ConsumerRecord<K, V> record) {
        for(FilterClause clause : clauses) {
            if(!FilterOperationUtils.executeAvroJsonOperation(clause, record.value())) {
                return false;
            }
        }
        return true;
    }


}