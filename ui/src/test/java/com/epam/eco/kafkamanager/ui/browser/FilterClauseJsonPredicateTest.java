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

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseJsonPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_TOMBSTONES;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_CORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_EMPTY;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_INCORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_EMPTY_FILTER_CLAUSE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_KEY;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_SIMPLE_FILTER_CLAUSE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.HEADER_WRONG_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE_WRONG;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.NULL_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateConsumerRecordWithHeader;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateJson;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseJsonPredicate;
import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAbstractPredicate.HEADERS_ATTRIBUTE;
import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAbstractPredicate.KEY_ATTRIBUTE;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseJsonPredicateTest {

    private static final String TEST_CLAUSE_SIMPLE_STRING = "testField:testValue";
    private static final String TEST_JSON = "{\"testField\": \"testValue\", \"innerField\" : { \"innerField2\":\"innerValue2\"}}";
    private static final String TEST_JSON_INCORRECT = "{\"testField\": \"tstValueInvalid\", \"innerField\" : { \"innerField2\":\"innerValueInvalid2\"}}";


    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS, KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));

    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));

    }



    @Test
    public void testOrdinaryHeaderEquals() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.EQUALS, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_WRONG_VALUE))));

        filterClausePredicate =
                getFilterClauseJsonPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.EQUALS, TEST_CLAUSE_SIMPLE_STRING);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of("testField","testValue"))));

    }

    @Test
    public void testOrdinaryHeaderContains() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.CONTAINS, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_WRONG_VALUE+SUFFIX))));
    }
    @Test
    public void testOrdinaryHeaderStartsWith() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.STARTS_WITH, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_WRONG_VALUE))));
    }

    @Test
    public void testOrdinaryHeaderNotEmpty() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.NOT_EMPTY, HEADER_EMPTY_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, FIELD_VALUE_EMPTY))));

    }


    @Test
    public void testJsonOrdinaryFieldEquals() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_CORRECT);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_INCORRECT);
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonOrdinaryFieldContains() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,FIELD_NAME+":test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON_INCORRECT)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonOrdinaryFieldStarts() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH, FIELD_NAME+":"+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateJson(FIELD_VALUE_CORRECT + SUFFIX))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateJson(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }


    @Test
    public void testJsonFieldEquals() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue");

        String json = TEST_JSON;

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonJsonFieldContains() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"innerField.innerField2:innerValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonJsonFieldStartsWith() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:test");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"innerField.innerField2:inner");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonTombstonesExclude() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_TOMBSTONES, FilterOperationEnum.EXCLUDE, FIELD_VALUE_EMPTY);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }

    @Test
    public void testJsonTombstonesOnly() {

        FilterClauseJsonPredicate filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_TOMBSTONES, FilterOperationEnum.ONLY, FIELD_VALUE_EMPTY);

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }

}
