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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.commons.kafka.helpers.FilterClausePredicate;
import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseHeaderPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseJsonKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseJsonValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseNoopPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseTombstonePredicate;

import static com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.HEADER_PREFIX;

/**
 * @author Mikhail_Vershkov
 */
public class FilterClauseCompositePredicate<K, V> implements FilterClausePredicate<K, V> {
    public static final String KEY_ATTRIBUTE = "key";
    public static final String TOMBSTONE_ATTRIBUTE = "tombstones";
    private final Predicate<ConsumerRecord<K, V>> keyPredicate;
    private final Predicate<ConsumerRecord<K, V>> valuePredicate;
    private final Predicate<ConsumerRecord<K, V>> headerPredicate;
    private final Predicate<ConsumerRecord<K, V>> tombstonePredicate;

    private enum KeyValueType {
        KEY, VALUE;
    }

    public FilterClauseCompositePredicate(TopicBrowseParams browseParams) {

        List<FilterClause> keyClauses = extractKeyClauses(browseParams);
        keyPredicate = resolveKeyValuePredicate(KeyValueType.KEY, browseParams.getKeyFormat(), keyClauses);

        List<FilterClause> headerClauses = extractHeaderClauses(browseParams);
        headerPredicate = new FilterClauseHeaderPredicate<>(headerClauses);

        List<FilterClause> valueClauses = extractValueClauses(browseParams);
        valuePredicate = resolveKeyValuePredicate(KeyValueType.VALUE, browseParams.getValueFormat(), valueClauses);

        List<FilterClause> tombstoneClauses = extractTombstoneClauses(browseParams);
        tombstonePredicate = new FilterClauseTombstonePredicate<>(tombstoneClauses);

    }

    @Override
    public boolean test(ConsumerRecord<K, V> consumerRecord) {
        return keyPredicate.test(consumerRecord)
            && headerPredicate.test(consumerRecord)
            && tombstonePredicate.test(consumerRecord)
            && valuePredicate.test(consumerRecord);
    }

    private Predicate<ConsumerRecord<K, V>> resolveKeyValuePredicate(KeyValueType keyValueType,
                                                                      TopicRecordFetchParams.DataFormat dataFormat,
                                                                      List<FilterClause> clauses) {
        return switch (dataFormat) {
            case AVRO -> (Predicate) (keyValueType == KeyValueType.KEY ?
                                             new FilterClauseAvroKeyPredicate(clauses) :
                                             new FilterClauseAvroValuePredicate(clauses));
            case STRING -> (Predicate) (keyValueType == KeyValueType.KEY ?
                                             new FilterClauseStringKeyPredicate(clauses) :
                                             new FilterClauseStringValuePredicate(clauses));
            case JSON_STRING -> (Predicate) (keyValueType == KeyValueType.KEY ?
                                             new FilterClauseJsonKeyPredicate(clauses) :
                                             new FilterClauseJsonValuePredicate(clauses));
            default -> (Predicate) new FilterClauseNoopPredicate();
        };
    }

    private static List<FilterClause> extractKeyClauses(TopicBrowseParams browseParams) {
        return browseParams.getFilterClausesAsMap().entrySet().stream()
                           .filter(clause -> KEY_ATTRIBUTE.equals(clause.getKey()))
                           .flatMap(entry -> entry.getValue().stream())
                           .collect(Collectors.toList());
    }

    private static List<FilterClause> extractValueClauses(TopicBrowseParams browseParams) {
        return browseParams.getFilterClausesAsMap().entrySet().stream()
                           .filter(clause -> (!KEY_ATTRIBUTE.equals(clause.getKey())
                                               && !clause.getKey().startsWith(HEADER_PREFIX)
                                               && !TOMBSTONE_ATTRIBUTE.equals(clause.getKey())))
                .flatMap(entry -> entry.getValue().stream())
                .toList();
    }

    private static List<FilterClause> extractTombstoneClauses(TopicBrowseParams browseParams) {
        return browseParams.getFilterClausesAsMap().entrySet().stream()
                           .filter(clause -> TOMBSTONE_ATTRIBUTE.equals(clause.getKey()))
                           .flatMap(entry -> entry.getValue().stream())
                           .collect(Collectors.toList());
    }

    private static List<FilterClause> extractHeaderClauses(TopicBrowseParams browseParams) {
        return browseParams.getFilterClausesAsMap().entrySet().stream()
                                                       .filter(entry -> entry.getKey().startsWith(HEADER_PREFIX))
                                                       .flatMap(entry -> entry.getValue().stream())
                                                       .map(clause -> new FilterClause(clause.getColumn().substring(HEADER_PREFIX.length()),
                                                                                       clause.getOperation(),
                                                                                       clause.getValue()))
                           .toList();
    }

    public Predicate<ConsumerRecord<K, V>> getKeyPredicate() {
        return keyPredicate;
    }

    public Predicate<ConsumerRecord<K, V>> getValuePredicate() {
        return valuePredicate;
    }

    public Predicate<ConsumerRecord<K, V>> getTonbstonePredicate() {
        return tombstonePredicate;
    }

    public Predicate<ConsumerRecord<K, V>> getHeaderPredicate() {
        return headerPredicate;
    }

}
