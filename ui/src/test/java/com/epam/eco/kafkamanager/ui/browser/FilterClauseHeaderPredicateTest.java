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
package com.epam.eco.kafkamanager.ui.browser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseHeaderPredicate;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_CORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_EMPTY;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_INCORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_EMPTY_FILTER_CLAUSE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_KEY;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_SIMPLE_FILTER_CLAUSE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_WRONG_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.NULL_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateConsumerRecordWithHeader;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateString;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseHeaderPredicate;
import static com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.HEADER_PREFIX;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseHeaderPredicateTest {

    private static final String TEST_STRING = "testField=testValue, innerField2=innerValue2";

    @Test
    public void testOrdinaryHeaderEquals() {

        FilterClauseHeaderPredicate filterClausePredicate =
                getFilterClauseHeaderPredicate(HEADER_PREFIX + HEADER_KEY, FilterOperationEnum.EQUALS, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                     .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE))));
        Assertions.assertFalse(filterClausePredicate
                     .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_WRONG_VALUE))));
    }

    @Test
    public void testOrdinaryHeaderContains() {

        FilterClauseHeaderPredicate filterClausePredicate =
                getFilterClauseHeaderPredicate(HEADER_PREFIX + HEADER_KEY, FilterOperationEnum.CONTAINS, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_WRONG_VALUE+SUFFIX))));
    }
    @Test
    public void testOrdinaryHeaderStartsWith() {

        FilterClauseHeaderPredicate filterClausePredicate =
                getFilterClauseHeaderPredicate(HEADER_PREFIX + HEADER_KEY, FilterOperationEnum.STARTS_WITH, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_WRONG_VALUE))));
    }

    @Test
    public void testOrdinaryHeaderNotEmpty() {

        FilterClauseHeaderPredicate filterClausePredicate =
                getFilterClauseHeaderPredicate(HEADER_PREFIX + HEADER_KEY, FilterOperationEnum.NOT_EMPTY, HEADER_EMPTY_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, FIELD_VALUE_EMPTY))));

    }



    @Test
    public void testHeaderOrdinaryFieldEquals() {

        FilterClauseHeaderPredicate filterClausePredicate =
                getFilterClauseHeaderPredicate(FIELD_NAME, FilterOperationEnum.EQUALS, FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, 0L, TimestampType.CREATE_TIME, 1, 1,
                                     KEY_VALUE, generateString(FIELD_VALUE_CORRECT),
                new RecordHeaders(List.of(new RecordHeader(FIELD_NAME,FIELD_VALUE_CORRECT.getBytes()))),
                Optional.of(1)
                )));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseHeaderPredicate(FIELD_NAME, FilterOperationEnum.EQUALS, FIELD_VALUE_INCORRECT);

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_STRING)));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

}
