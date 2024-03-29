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

import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringValuePredicate;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_TOMBSTONES;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_CORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_EMPTY;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE_WRONG;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.NULL_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateString;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseStringKeyPredicate;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseStringValuePredicate;
import static com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAbstractKeyPredicate.KEY_ATTRIBUTE;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseStringPredicateTest {

    private static final String TEST_STRING = "testField=testValue, innerField2=innerValue2";
    private static final String TEST_STRING_INCORRECT = "testField=tstValueIncorrect, innerField2=innerValueIncorrect2";

    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseStringKeyPredicate filterClausePredicate =
                getFilterClauseStringKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS, KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseStringKeyPredicate filterClausePredicate =
                getFilterClauseStringKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));
    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseStringKeyPredicate filterClausePredicate =
                getFilterClauseStringKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));
    }

    @Test
    public void testStringOrdinaryFieldContains() {

        FilterClauseStringValuePredicate filterClausePredicate =
                getFilterClauseStringValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,FIELD_NAME+"=test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_STRING)));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_STRING_INCORRECT)));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testStringOrdinaryFieldStarts() {

        FilterClauseStringValuePredicate filterClausePredicate =
                getFilterClauseStringValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH, FIELD_NAME+"="+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testStringTombstonesExclude() {

        FilterClauseStringValuePredicate filterClausePredicate =
                getFilterClauseStringValuePredicate(FIELD_TOMBSTONES, FilterOperationEnum.EXCLUDE, FIELD_VALUE_EMPTY);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }


}
