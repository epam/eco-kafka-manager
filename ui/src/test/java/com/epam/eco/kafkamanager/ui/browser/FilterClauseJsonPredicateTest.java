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
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseJsonPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;

import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.KEY_ATTRIBUTE;


/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseJsonPredicateTest {
    private final static String TOPIC_NAME = "testTopic";
    private final static String KEY_VALUE = "testKey";
    private final static String KEY_VALUE_WRONG = "testKeyWrong";
    private final static String FIELD_NAME = "testField";
    private final static String FIELD_VALUE_CORRECT = "testValue";
    private final static String FIELD_VALUE_INCORRECT = "testValueInvalid";
    private final static String PREFIX = "prefix";
    private final static String SUFFIX = "suffix";
    private final static String TEST_JSON = "{\"testField\": \"testValue\", \"innerField\" : { \"innerField2\":\"innerValue2\"}}";
    private final static String TEST_JSON_INCORRECT = "{\"testField\": \"tstValueInvalid\", \"innerField\" : { \"innerField2\":\"innerValueInvalid2\"}}";


    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS, KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));
    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));
    }


    @Test
    public void testJsonOrdinaryFieldEquals() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_NAME+":"+FIELD_VALUE_INCORRECT);

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

    }

    @Test
    public void testJsonOrdinaryFieldContains() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,FIELD_NAME+":test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON_INCORRECT)));

    }

    @Test
    public void testJsonOrdinaryFieldStarts() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH, FIELD_NAME+":"+FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateJson(FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, generateJson(PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

    }


    @Test
    public void testJsonFieldEquals() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue");

        String json = TEST_JSON;

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, json)));

    }

    @Test
    public void testJsonJsonFieldContains() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"innerField.innerField2:innerValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

    }

    @Test
    public void testJsonJsonFieldStartsWith() {

        FilterClauseJsonPredicate<String,Object> filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"innerField.innerField2:inner");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

        filterClausePredicate =
                getFilterClauseJsonPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, TEST_JSON)));

    }

    @NotNull
    private FilterClauseJsonPredicate<String, Object> getFilterClauseJsonPredicate(String clauseFieldName,
                                                                                   FilterOperationEnum operationEnum,
                                                                                   String clauseValue) {
        Map<String, List<FilterClause>> clauses = Map.of(clauseFieldName,
                                                         List.of(new FilterClause(clauseFieldName,
                                                                                  operationEnum.getOperation(),
                                                                                  clauseValue)));
        return new FilterClauseJsonPredicate<>(clauses);
    }

    private String generateJson(String fieldValue) {
        return String.format("{\"%s\": \"%s\"}",FIELD_NAME, fieldValue);
    }

}
