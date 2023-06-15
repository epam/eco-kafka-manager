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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.commons.kafka.helpers.FilterClausePredicate;
import com.epam.eco.kafkamanager.FilterClause;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

public abstract class FilterClauseAbstractPredicate<K, V> implements FilterClausePredicate<K, V> {

    public static final String KEY_ATTRIBUTE = "key";
    public static final String TOMBSTONE_ATTRIBUTE = "tombstones";

    public static final String[] RESERVED_ATTRIBUTES = {KEY_ATTRIBUTE,TOMBSTONE_ATTRIBUTE};
    protected final boolean areClausesEmpty;
    protected final List<FilterClause> keyClauses;
    protected final List<FilterClause> tombstoneClauses;
    protected final List<FilterClause> otherClauses;

    public FilterClauseAbstractPredicate(Map<String, List<FilterClause>> clauses) {
        areClausesEmpty = clauses.isEmpty();
        keyClauses = clauses.getOrDefault(KEY_ATTRIBUTE, Collections.emptyList());
        tombstoneClauses = clauses.getOrDefault(TOMBSTONE_ATTRIBUTE, Collections.emptyList());
        otherClauses = clauses.entrySet().stream()
                                  .filter(clause->!Arrays.asList(RESERVED_ATTRIBUTES).contains(clause.getKey()))
                                  .flatMap(clause->clause.getValue().stream())
                                  .collect(Collectors.toList());
    }

    @Override
    public boolean test(ConsumerRecord<K, V> record) {
        if(areClausesEmpty) {
            return true;
        } else {

            if(!keyClauses.isEmpty()) {
                if(!processKeyClauses(record)) return false;
            }

            if(!tombstoneClauses.isEmpty()) {
                if(!processTombstoneClauses(record)) return false;
            }

            if(otherClauses.isEmpty() && isNull(record.value())) return true;

            if(!otherClauses.isEmpty()) {
                return processValueClauses(record);
            }

            return true;
        }
    }

    private boolean processTombstoneClauses(ConsumerRecord<K, V> record) {
        boolean result = true;
        for(FilterClause clause : tombstoneClauses) {
            result = result && executeOperation(clause, record.value());
        }
        return result;
    }

    private boolean processKeyClauses(ConsumerRecord<K, V> record) {

        boolean result = true;

        for(FilterClause clause : keyClauses) {
            if(nonNull(clause.getValue())) {
                result = result && executeOperation(clause, record.key().toString());
            } else {
                return false;
            }
        }

        return result;
    }

    abstract boolean processValueClauses(ConsumerRecord<K,V> record);
    abstract boolean executeOperation(FilterClause clause, Object value);


}
