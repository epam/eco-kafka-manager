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

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseStringPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;

import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.KEY_ATTRIBUTE;


/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseStringPredicateTest {
    private final static String TOPIC_NAME = "testTopic";
    private final static String KEY_VALUE = "testKey";
    private final static String KEY_VALUE_WRONG = "testKeyWrong";
    private final static String FIELD_NAME = "testField";
    private final static String FIELD_VALUE_CORRECT = "testValue";
    private final static String FIELD_VALUE_INCORRECT = "testValueInvalid";
    private final static String PREFIX = "prefix";
    private final static String SUFFIX = "suffix";
    private final static String TEST_STRING = "testField=testValue, innerField2=innerValue2";
    private final static String TEST_STRING_INCORRECT = "testField=tstValueIncorrect, innerField2=innerValueIncorrect2";


    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseStringPredicate<String,Object> filterClausePredicate =
                getFilterClauseStringPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS, KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseStringPredicate<String,Object> filterClausePredicate =
                getFilterClauseStringPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));
    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseStringPredicate<String,Object> filterClausePredicate =
                getFilterClauseStringPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));
    }


    @Test
    public void testStringOrdinaryFieldEquals() {

        FilterClauseStringPredicate<String,Object> filterClausePredicate =
                getFilterClauseStringPredicate(FIELD_NAME, FilterOperationEnum.EQUALS, FIELD_NAME+"="+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(FIELD_VALUE_CORRECT))));

        filterClausePredicate =
                getFilterClauseStringPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_INCORRECT);

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_STRING)));

    }

    @Test
    public void testStringOrdinaryFieldContains() {

        FilterClauseStringPredicate<String,Object> filterClausePredicate =
                getFilterClauseStringPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,FIELD_NAME+"=test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_STRING)));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_STRING_INCORRECT)));

    }

    @Test
    public void testStringOrdinaryFieldStarts() {

        FilterClauseStringPredicate<String,Object> filterClausePredicate =
                getFilterClauseStringPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH, FIELD_NAME+"="+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateString(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

    }
    
    @NotNull
    private FilterClauseStringPredicate<String, Object> getFilterClauseStringPredicate(String clauseFieldName,
                                                                                   FilterOperationEnum operationEnum,
                                                                                   String clauseValue) {
        Map<String, List<FilterClause>> clauses = Map.of(clauseFieldName,
                                                         List.of(new FilterClause(clauseFieldName,
                                                                                  operationEnum.getOperation(),
                                                                                  clauseValue)));
        return new FilterClauseStringPredicate<>(clauses);
    }

    private String generateString(String fieldValue) {
        return String.format("%s=%s",FIELD_NAME, fieldValue);
    }


}
