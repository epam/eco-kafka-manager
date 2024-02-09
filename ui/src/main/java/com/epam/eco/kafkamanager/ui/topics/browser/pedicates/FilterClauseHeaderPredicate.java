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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;

import com.epam.eco.kafkamanager.FilterClause;

import static com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationUtils.executeHeaderOperation;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseHeaderPredicate<K, V> implements Predicate<ConsumerRecord<K, V>> {

    protected final List<FilterClause> clauses;

    public FilterClauseHeaderPredicate(List<FilterClause> clauses) {
        this.clauses = clauses;
    }

    @Override
    public boolean test(ConsumerRecord<K, V> record) {
        if(clauses.isEmpty()) {
            return true;
        }
        List<Header> headers = Arrays.asList(record.headers().toArray());
        if(headers.isEmpty()) {
            return false;
        }
        Map<Object, Object> headersMap = headers.stream().collect(
                Collectors.toMap(Header::key, header -> new String(header.value())));
        for(FilterClause clause : clauses) {
            if(!executeHeaderOperation(clause, headersMap)) {
                return false;
            }
        }
        return true;
    }

}
