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
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import com.epam.eco.commons.kafka.helpers.FilterClausePredicate;
import com.epam.eco.kafkamanager.FilterClause;

import static com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.HEADER_PREFIX;
import static com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils.executeHeaderOperation;
import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */

public abstract class FilterClauseAbstractPredicate<K, V> implements FilterClausePredicate<K, V> {

    public static final String KEY_ATTRIBUTE = "key";
    public static final String TOMBSTONE_ATTRIBUTE = "tombstones";

    private static final Set<String> RESERVED_ATTRIBUTES = Set.of(KEY_ATTRIBUTE,TOMBSTONE_ATTRIBUTE);
    protected final boolean areClausesEmpty;
    protected final List<FilterClause> keyClauses;
    protected final List<FilterClause> headerClauses;
    protected final List<FilterClause> tombstoneClauses;
    protected final List<FilterClause> otherClauses;

    public FilterClauseAbstractPredicate(Map<String, List<FilterClause>> clauses) {
        areClausesEmpty = clauses.isEmpty();
        keyClauses = clauses.getOrDefault(KEY_ATTRIBUTE, Collections.emptyList());
        headerClauses = clauses.entrySet().stream()
                               .filter(entry -> entry.getKey().startsWith(HEADER_PREFIX))
                               .flatMap(entry->entry.getValue().stream())
                               .map(clause-> new FilterClause(clause.getColumn().substring(HEADER_PREFIX.length()),
                                                              clause.getOperation(), clause.getValue()))
                               .collect(Collectors.toList());
        tombstoneClauses = clauses.getOrDefault(TOMBSTONE_ATTRIBUTE, Collections.emptyList());
        otherClauses = clauses.entrySet().stream()
                                  .filter(clause->(!RESERVED_ATTRIBUTES.contains(clause.getKey()) && !clause.getKey().startsWith(HEADER_PREFIX)))
                                  .flatMap(clause->clause.getValue().stream())
                                  .collect(Collectors.toList());
    }

    @Override
    public boolean test(ConsumerRecord<K, V> record) {
        if(areClausesEmpty) {
            return true;
        }

        if(!keyClauses.isEmpty() && !processKeyClauses(record)) {
            return false;
        }

        if(!headerClauses.isEmpty() && !processHeaderClauses(record)) {
            return false;
        }

        if(!tombstoneClauses.isEmpty() && !processTombstoneClauses(record)) {
            return false;
        }

        if(otherClauses.isEmpty() && isNull(record.value())) return true;

        if(!otherClauses.isEmpty()) {
            return processValueClauses(record);
        }

        return true;

    }

    private boolean processHeaderClauses(ConsumerRecord<K, V> record) {
        List<Header> headers = Arrays.asList(record.headers().toArray());
        if(headers.isEmpty()) {
            return false;
        }
        Map<Object,Object> headersMap = headers.stream()
                                               .collect(Collectors.toMap(Header::key,
                                                                         header -> new String(header.value())));
        for (FilterClause clause : headerClauses) {
            if (!executeHeaderOperation(clause, headersMap)) {
                return false;
            }
        }
        return true;
    }

    private boolean processTombstoneClauses(ConsumerRecord<K, V> record) {
        for (FilterClause clause : tombstoneClauses) {
            if (!executeOperation(clause, record.value())) {
                return false;
            }
        }
        return true;
    }

    private boolean processKeyClauses(ConsumerRecord<K, V> record) {
        for (FilterClause clause : keyClauses) {
            if (clause.getValue() == null || !executeOperation(clause, record.key().toString())) {
                return false;
            }
        }
        return true;
    }

    abstract boolean processValueClauses(ConsumerRecord<K,V> record);
    abstract boolean executeOperation(FilterClause clause, Object value);


}
