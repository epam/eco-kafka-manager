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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseJsonKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseJsonValuePredicate;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_CORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_INCORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE_WRONG;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.NULL_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateJson;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseJsonKeyPredicate;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseJsonValuePredicate;
import static com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAbstractKeyPredicate.KEY_ATTRIBUTE;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseJsonPredicateTest {

    private static final String TEST_JSON = "{\"testField\": \"testValue\", \"innerField\" : { \"innerField2\":\"innerValue2\"}}";
    private static final String TEST_JSON_INCORRECT = "{\"testField\": \"tstValueInvalid\", \"innerField\" : { \"innerField2\":\"innerValueInvalid2\"}}";

    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseJsonKeyPredicate filterClausePredicate =
                getFilterClauseJsonKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS, KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseJsonKeyPredicate filterClausePredicate =
                getFilterClauseJsonKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));

    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseJsonKeyPredicate filterClausePredicate =
                getFilterClauseJsonKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));

    }



    @Test
    public void testJsonOrdinaryFieldEquals() {

        FilterClauseJsonValuePredicate filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_CORRECT);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_INCORRECT);
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonOrdinaryFieldContains() {

        FilterClauseJsonValuePredicate filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,FIELD_NAME+":test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON_INCORRECT)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonOrdinaryFieldStarts() {

        FilterClauseJsonValuePredicate filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH, FIELD_NAME+":"+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateJson(FIELD_VALUE_CORRECT + SUFFIX))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateJson(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }


    @Test
    public void testJsonFieldEquals() {

        FilterClauseJsonValuePredicate filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue");

        String json = TEST_JSON;

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonJsonFieldContains() {

        FilterClauseJsonValuePredicate filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"innerField.innerField2:innerValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testJsonJsonFieldStartsWith() {

        FilterClauseJsonValuePredicate filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:test");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"innerField.innerField2:inner");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseJsonValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

}
