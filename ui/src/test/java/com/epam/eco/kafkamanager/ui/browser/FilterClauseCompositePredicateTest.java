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
package com.epam.eco.kafkamanager.ui.browser;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.ui.topics.browser.filter.FilterClauseCompositePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseHeaderPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.TopicBrowseParams;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseTombstonePredicate;

import static com.epam.eco.kafkamanager.ui.browser.FilterPredicateUtils.FIELD_TOMBSTONES;
import static com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.HEADER_PREFIX;
import static com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAbstractKeyPredicate.KEY_ATTRIBUTE;

/**
 * @author Mikhail_Vershkov
 */

public class FilterClauseCompositePredicateTest {

    private static final String KEY_VALUE = "keyValue";
    private static final String TEST_TOPIC = "testTopic";
    private static final String COLUMN_NAME = "testColumn";
    private static final String COLUMN_VALUE = "testValue";
    private static final String HEADER_NAME = "headerName";
    private static final String HEADER_VALUE = "testHeaderValue";
    private static final String WRONG_VALUE = "wrongValue";
    private static TopicBrowseParams browseParams;

    Schema schema = new Schema.Parser().parse("{\"namespace\": \"example.avro\"," +
                                                      " \"type\": \"record\"," +
                                                      " \"name\": \"Test\"," +
                                                      " \"fields\": [" +
                                                      "     {\"name\": \"testColumn\", \"type\": \"string\"}" +
                                                      " ]" +
                                                      "}");
    private static FilterClauseCompositePredicate predicate;

    private static ObjectMapper MAPPER = new ObjectMapper();

    @BeforeAll
    public static void beforeAll() throws JsonProcessingException {
        browseParams = TopicBrowseParams.with(
                Map.of(TopicBrowseParams.KEY_FORMAT, TopicRecordFetchParams.DataFormat.STRING.name(),
                       TopicBrowseParams.VALUE_FORMAT, TopicRecordFetchParams.DataFormat.AVRO.name(),
                       TopicBrowseParams.FILTER_CLAUSE,
                       MAPPER.writeValueAsString(
                               List.of(
                                       new FilterClause(KEY_ATTRIBUTE, FilterOperationEnum.EQUALS.getOperation(), KEY_VALUE),
                                       new FilterClause(COLUMN_NAME, FilterOperationEnum.EQUALS.getOperation(), COLUMN_VALUE),
                                       new FilterClause(FIELD_TOMBSTONES, FilterOperationEnum.ONLY.getOperation(), null),
                                       new FilterClause(HEADER_PREFIX + HEADER_NAME, FilterOperationEnum.CONTAINS.getOperation(), HEADER_VALUE)
                               )

                           )
                      ));
        predicate = new FilterClauseCompositePredicate(browseParams);
    }

    @Test
    public void testPredicatesType() {
        Assertions.assertInstanceOf(FilterClauseStringKeyPredicate.class, predicate.getKeyPredicate());
        Assertions.assertInstanceOf(FilterClauseAvroValuePredicate.class, predicate.getValuePredicate());
        Assertions.assertInstanceOf(FilterClauseHeaderPredicate.class, predicate.getHeaderPredicate());
        Assertions.assertInstanceOf(FilterClauseTombstonePredicate.class, predicate.getTonbstonePredicate());
    }

    @Test
    public void testKeyPredicate() {
        Assertions.assertTrue(predicate.getKeyPredicate().test(new ConsumerRecord<>(TEST_TOPIC,0,0L, KEY_VALUE, COLUMN_VALUE)));
        Assertions.assertFalse(predicate.getKeyPredicate().test(new ConsumerRecord<>(TEST_TOPIC,0,0L, WRONG_VALUE, COLUMN_VALUE)));
    }

    @Test
    public void testValuePredicate() {
        GenericRecord record = new GenericData.Record(schema);
        record.put(COLUMN_NAME, COLUMN_VALUE);
        Assertions.assertTrue(predicate.getValuePredicate().test(new ConsumerRecord<>(TEST_TOPIC, 0, 0L, KEY_VALUE,record)));
        record.put(COLUMN_NAME, WRONG_VALUE);
        Assertions.assertFalse(predicate.getValuePredicate().test(new ConsumerRecord<>(TEST_TOPIC, 0, 0L, KEY_VALUE,record)));
    }

    @Test
    public void testHeaderPredicate() {
        Headers headers = new RecordHeaders(List.of(new RecordHeader(HEADER_NAME, HEADER_VALUE.getBytes())));
        Assertions.assertTrue(predicate.getHeaderPredicate().test(new ConsumerRecord<>(TEST_TOPIC, 0, 0L, 0L,
                                                                                       TimestampType.CREATE_TIME, 1, 1,
                                                                                       KEY_VALUE, COLUMN_VALUE,
                                                                                       headers,
                                                                                       Optional.of(1))));
        headers = new RecordHeaders(List.of(new RecordHeader(HEADER_NAME, WRONG_VALUE.getBytes())));
        Assertions.assertFalse(predicate.getHeaderPredicate().test(new ConsumerRecord<>(TEST_TOPIC, 0, 0L, 0L,
                                                                                       TimestampType.CREATE_TIME, 1, 1,
                                                                                       KEY_VALUE, COLUMN_VALUE,
                                                                                       headers,
                                                                                       Optional.of(1))));

    }

    @Test
    public void testTombstonePredicate() {
        Assertions.assertTrue(predicate.getTonbstonePredicate().test(new ConsumerRecord<>(TEST_TOPIC, 0, 0L, KEY_ATTRIBUTE, null)));
        Assertions.assertFalse(predicate.getTonbstonePredicate().test(new ConsumerRecord<>(TEST_TOPIC, 0, 0L, KEY_ATTRIBUTE, COLUMN_VALUE)));
    }

}