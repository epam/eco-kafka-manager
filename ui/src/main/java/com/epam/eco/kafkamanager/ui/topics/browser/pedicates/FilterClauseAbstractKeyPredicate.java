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

import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */

public abstract class FilterClauseAbstractKeyPredicate<K, V> implements Predicate<ConsumerRecord<K, V>> {

    public static final String KEY_ATTRIBUTE = "key";
    public static final String KEY_OF_NULL = "null";

    protected final List<FilterClause> clauses;

    public FilterClauseAbstractKeyPredicate(List<FilterClause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public boolean test(ConsumerRecord<K, V> record) {

        if(clauses.isEmpty()) {
            return true;
        }

        return processKeyClauses(record);

    }

    protected boolean processKeyClauses(ConsumerRecord<K, V> record) {
        for(FilterClause clause : clauses) {
            if(isNull(clause.getValue())) {
                continue;
            }
            if(!executeOperation(clause, getStringOfNullable(record))) {
                return false;
            }
        }
        return true;
    }

    private static <K, V> String getStringOfNullable(ConsumerRecord<K, V> record) {
        return isNull(record.key()) ? KEY_OF_NULL : record.key().toString();
    }

    abstract boolean executeOperation(FilterClause clause, Object value);


}
