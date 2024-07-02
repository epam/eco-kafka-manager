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

import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroValuePredicate;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_CORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_VALUE_INCORRECT;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.KEY_VALUE_WRONG;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.MAP_FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.NULL_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseAvroKeyPredicate;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseAvroValuePredicate;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getGenericRecord;
import static com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAbstractKeyPredicate.KEY_ATTRIBUTE;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseAvroPredicateTest {

    private static final String TEST_JSON = "{\"testField\": \"testValue\", \"innerField\" : { \"innerField2\":\"innerValue2\"}}";
    private static final Map<String,String> TEST_MAP = Map.of("testField","testValue", "testField2", "testValue2");

    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseAvroKeyPredicate filterClausePredicate =
                getFilterClauseAvroKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseAvroKeyPredicate filterClausePredicate =
                getFilterClauseAvroKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));
    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseAvroKeyPredicate filterClausePredicate =
                getFilterClauseAvroKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));
    }

    @Test
    public void testNullKeyEquals() {

        FilterClauseAvroKeyPredicate filterClausePredicate =
                getFilterClauseAvroKeyPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS, "null");
        Assertions.assertTrue(filterClausePredicate
                                      .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, null, FIELD_VALUE_CORRECT)));
        Assertions.assertTrue(filterClausePredicate
                                      .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "null", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                      .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "not_null", FIELD_VALUE_CORRECT)));
    }

    @Test
    public void testAvroOrdinaryFieldEquals() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_INCORRECT))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroOrdinaryFieldContains() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testAvroOrdinaryFieldStarts() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }


    @Test
    public void testAvroJsonFieldEquals() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroJsonFieldContains() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:test");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"innerField.innerField2:innerValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testAvroJsonFieldStartsWith() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:test");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"innerField.innerField2:inner");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroMapFieldEquals() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField:testValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField2:testValue");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroMapFieldContains() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:Value");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField2:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroMapFieldStartsWith() {

        FilterClauseAvroValuePredicate filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField2:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroValuePredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

}
