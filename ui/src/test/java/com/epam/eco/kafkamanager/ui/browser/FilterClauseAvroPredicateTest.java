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

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAvroPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;

import static com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseUtils.KEY_ATTRIBUTE;


/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseAvroPredicateTest {
    private final static String NAME_SPACE = "testNameSpace";
    private final static String TOPIC_NAME = "testTopic";
    private final static String KEY_VALUE = "testKey";
    private final static String KEY_VALUE_WRONG = "testKeyWrong";
    private final static String FIELD_NAME = "testField";
    private final static String FIELD_VALUE_CORRECT = "testValue";
    private final static String FIELD_VALUE_INCORRECT = "testValueInvalid";
    private final static String MAP_FIELD_NAME = "mapField";
    private final static String PREFIX = "prefix";
    private final static String SUFFIX = "suffix";

    private final static String TEST_JSON = "{\"testField\": \"testValue\", \"innerField\" : { \"innerField2\":\"innerValue2\"}}";
    private final static Map<String,String> TEST_MAP = Map.of("testField","testValue", "testField2", "testValue2");

    private final static Schema testSchema = SchemaBuilder.record(NAME_SPACE)
                                                          .fields()
                                                          .nullableString(FIELD_NAME,"null")
                                                          .name(MAP_FIELD_NAME)
                                                          .type().map().values()
                                                                  .stringBuilder()
                                                                  .endString()
                                                                  .mapDefault(Map.of())
                                                          .endRecord();

    @Test
    public void testOrdinaryKeyEquals() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE_WRONG, KEY_VALUE)));
    }

    @Test
    public void testOrdinaryKeyContains() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(KEY_ATTRIBUTE, FilterOperationEnum.CONTAINS,"TestKey");
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "areTestKeyContains", KEY_VALUE)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyContains", KEY_VALUE)));
    }
    @Test
    public void testOrdinaryKeyStartsWith() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(KEY_ATTRIBUTE, FilterOperationEnum.STARTS_WITH,KEY_VALUE);
        Assertions.assertTrue(filterClausePredicate
                                  .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testKeyStartsWith", FIELD_VALUE_CORRECT)));
        Assertions.assertFalse(filterClausePredicate
                                   .test(new ConsumerRecord<>(TOPIC_NAME, 0, 0L, "testStartsWith", FIELD_VALUE_CORRECT)));
    }


    @Test
    public void testAvroOrdinaryFieldEquals() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_INCORRECT))));

    }

    @Test
    public void testAvroOrdinaryFieldContains() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"test");

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + SUFFIX))));

    }

    @Test
    public void testAvroOrdinaryFieldStarts() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,FIELD_VALUE_CORRECT);

        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,FIELD_VALUE_CORRECT + SUFFIX))));

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,PREFIX + FIELD_VALUE_CORRECT + SUFFIX))));

    }


    @Test
    public void testAvroJsonFieldEquals() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue2");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

    }

    @Test
    public void testAvroJsonFieldContains() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:test");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"innerField.innerField2:innerValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.CONTAINS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

    }

    @Test
    public void testAvroJsonFieldStartsWith() {

        FilterClauseAvroPredicate<String,Object> filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:test");
        GenericRecord genericRecord = getGenericRecord(FIELD_NAME,TEST_JSON);
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(FIELD_NAME,TEST_JSON))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"innerField.innerField2:inner");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.STARTS_WITH,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

        filterClausePredicate =
                getFilterClauseAvroPredicate(FIELD_NAME, FilterOperationEnum.EQUALS,"innerField.innerField2:innerValue22");

        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, genericRecord)));

    }

    @Test
    public void testAvroMapFieldEquals() {

        FilterClauseAvroPredicate<String, Object> filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField:testValue");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField2:testValue");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.EQUALS,"testField:testValue2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
    }

    @Test
    public void testAvroMapFieldContains() {

        FilterClauseAvroPredicate<String, Object> filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:Value");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField2:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
    }

    @Test
    public void testAvroMapFieldStartsWith() {

        FilterClauseAvroPredicate<String, Object> filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test");
        Assertions.assertTrue(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField2:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));

        filterClausePredicate =
                getFilterClauseAvroPredicate(MAP_FIELD_NAME,FilterOperationEnum.CONTAINS,"testField:test2");
        Assertions.assertFalse(filterClausePredicate.test(
                new ConsumerRecord<>(TOPIC_NAME, 0, 0L, KEY_VALUE, getGenericRecord(MAP_FIELD_NAME,TEST_MAP))));
    }

    @NotNull
    private FilterClauseAvroPredicate<String, Object> getFilterClauseAvroPredicate(String clauseFieldName,
                                                                                   FilterOperationEnum operationEnum,
                                                                                   String clauseValue) {
        Map<String, List<FilterClause>> clauses = Map.of(clauseFieldName,
                                                         List.of(new FilterClause(clauseFieldName,
                                                                                  operationEnum.getOperation(),
                                                                                  clauseValue)));
        return new FilterClauseAvroPredicate<>(clauses);
    }

    @NotNull
    private GenericRecord getGenericRecord(String fieldName, Object object) {
        GenericRecord genericRecord = new GenericData.Record(testSchema);
        genericRecord.put(fieldName,object);
        return genericRecord;
    }

}
