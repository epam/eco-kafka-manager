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

import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAvroPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAvroPredicate;
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
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.MAP_FIELD_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.NULL_VALUE;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.PREFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.SUFFIX;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.TOPIC_NAME;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.generateConsumerRecordWithHeader;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseAvroPredicate;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getFilterClauseAvroPredicate;
import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.getGenericRecord;
import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAbstractPredicate.HEADERS_ATTRIBUTE;
import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAbstractPredicate.KEY_ATTRIBUTE;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseAvroPredicateTest {

    private static final String TEST_JSON = "{\"testField\": \"testValue\", \"innerField\" : { \"innerField2\":\"innerValue2\"}}";
    private static final Map<String,String> TEST_MAP = Map.of("testField","testValue", "testField2", "testValue2");


    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));
    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));
    }



    @Test
    public void testOrdinaryHeaderEquals() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.EQUALS, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_WRONG_VALUE))));
    }

    @Test
    public void testOrdinaryHeaderContains() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.CONTAINS, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_WRONG_VALUE+SUFFIX))));
    }
    @Test
    public void testOrdinaryHeaderStartsWith() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.STARTS_WITH, HEADER_SIMPLE_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, PREFIX+HEADER_WRONG_VALUE))));
    }

    @Test
    public void testOrdinaryHeaderNotEmpty() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(HEADERS_ATTRIBUTE, FilterOperationEnum.NOT_EMPTY, HEADER_EMPTY_FILTER_CLAUSE);
        Assertions.assertTrue(filterClausePredicate
                                      .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, HEADER_VALUE+SUFFIX))));
        Assertions.assertFalse(filterClausePredicate
                                       .test(generateConsumerRecordWithHeader(Map.of(HEADER_KEY, FIELD_VALUE_EMPTY))));

    }




    @Test
    public void testAvroOrdinaryFieldEquals() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_INCORRECT))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroOrdinaryFieldContains() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testAvroOrdinaryFieldStarts() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }


    @Test
    public void testAvroJsonFieldEquals() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroJsonFieldContains() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:test");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"innerField.innerField2:innerValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

    }

    @Test
    public void testAvroJsonFieldStartsWith() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:test");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"innerField.innerField2:inner");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroMapFieldEquals() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField:testValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField2:testValue");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroMapFieldContains() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:Value");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField2:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroMapFieldStartsWith() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField2:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, NULL_VALUE)));
    }

    @Test
    public void testAvroTombstonesExclude() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_TOMBSTONES, FilterOperationEnum.EXCLUDE, FIELD_VALUE_EMPTY);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }

    @Test
    public void testAvroTombstonesOnly() {

        FilterClauseAvroPredicate filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_TOMBSTONES, FilterOperationEnum.ONLY, FIELD_VALUE_EMPTY);

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, null)));

    }

}
