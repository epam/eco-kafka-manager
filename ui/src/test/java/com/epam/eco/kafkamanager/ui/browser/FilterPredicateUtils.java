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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.jetbrains.annotations.NotNull;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAvroValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseHeaderPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseJsonKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseJsonValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringKeyPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseStringValuePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseTombstonePredicate;

import static java.time.LocalTime.now;

/**
 * @author Mikhail_Vershkov
 */

public class FilterPredicateUtils {

    public static final String NAME_SPACE = "testNameSpace";
    public static final String TOPIC_NAME = "testTopic";
    public static final String KEY_VALUE = "testKey";
    public static final String HEADER_KEY = "testHeaderKey";
    public static final String HEADER_VALUE = "testHeaderValue";
    public static final String HEADER_WRONG_VALUE = "testHeaderWrongValue";
    public static final String HEADER_SIMPLE_FILTER_CLAUSE = HEADER_KEY+":"+HEADER_VALUE;
    public static final String HEADER_EMPTY_FILTER_CLAUSE = HEADER_KEY+":";

    public static final String KEY_VALUE_WRONG = "testKeyWrong";
    public static final String FIELD_NAME = "testField";
    public static final String FIELD_TOMBSTONES = "tombstones";
    public static final String FIELD_VALUE_CORRECT = "testValue";
    public static final String FIELD_VALUE_INCORRECT = "testValueInvalid";
    public static final String RECORD_FIELD_NAME = "testSubRecord";
    public static final String RECORD_FIELD_TYPE = "testSubRecordType";
    public static final String RECORD_SUB_FIELD_NAME = "testSubField";
    public static final Object NULL_VALUE = null;
    public static final String FIELD_VALUE_EMPTY = "";
    public static final String MAP_FIELD_NAME = "mapField";
    public static final String PREFIX = "prefix";
    public static final String SUFFIX = "suffix";

    public static final Schema TEST_SUB_RECORD_SCHEMA = SchemaBuilder.record(RECORD_FIELD_TYPE)
            .fields()
            .nullableString(RECORD_SUB_FIELD_NAME, "null")
            .endRecord();

    public static final Schema TEST_SCHEMA = SchemaBuilder.record(NAME_SPACE)
            .fields()
            .nullableString(FIELD_NAME, "null")
            .name(RECORD_FIELD_NAME)
            .type(Schema.createUnion(Schema.create(Schema.Type.NULL), TEST_SUB_RECORD_SCHEMA))
            .noDefault()
            .name(MAP_FIELD_NAME)
            .type().map().values()
            .stringBuilder()
            .endString()
            .mapDefault(Map.of())
            .endRecord();

    @NotNull
    public static FilterClauseStringKeyPredicate getFilterClauseStringKeyPredicate(String clauseFieldName,
                                                                                  FilterOperationEnum operationEnum,
                                                                                  String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName,
                                                              operationEnum.getOperation(),
                                                              clauseValue));
        return new FilterClauseStringKeyPredicate(clauses);
    }

    @NotNull
    public static FilterClauseStringValuePredicate getFilterClauseStringValuePredicate(String clauseFieldName,
                                                                                       FilterOperationEnum operationEnum,
                                                                                       String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName,
                                                              operationEnum.getOperation(),
                                                              clauseValue));
        return new FilterClauseStringValuePredicate(clauses);
    }

    public static String generateString(String fieldValue) {
        return String.format("%s=%s",FIELD_NAME, fieldValue);
    }

    @NotNull
    public static FilterClauseHeaderPredicate getFilterClauseHeaderPredicate(String clauseFieldName,
                                                                             FilterOperationEnum operationEnum,
                                                                             String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName,
                                                              operationEnum.getOperation(),
                                                              clauseValue));
        return new FilterClauseHeaderPredicate(clauses);
    }

    @NotNull
    public static FilterClauseTombstonePredicate getFilterClauseTombstonePredicate(String clauseFieldName,
                                                                                          FilterOperationEnum operationEnum,
                                                                                          String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName,
                                                              operationEnum.getOperation(),
                                                              clauseValue));
        return new FilterClauseTombstonePredicate(clauses);
    }


    @NotNull
    public static FilterClauseAvroKeyPredicate getFilterClauseAvroKeyPredicate(String clauseFieldName,
                                                                               FilterOperationEnum operationEnum,
                                                                               String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName, operationEnum.getOperation(), clauseValue));
        return new FilterClauseAvroKeyPredicate(clauses);
    }

    public static FilterClauseAvroValuePredicate getFilterClauseAvroValuePredicate(String clauseFieldName,
                                                                                   FilterOperationEnum operationEnum,
                                                                                   String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName, operationEnum.getOperation(), clauseValue));
        return new FilterClauseAvroValuePredicate(clauses);
    }


    @NotNull
    public static GenericRecord getGenericRecord(String fieldName, Object object) {
        GenericRecord genericRecord = new GenericData.Record(TEST_SCHEMA);
        genericRecord.put(fieldName,object);
        return genericRecord;
    }

    @NotNull
    public static FilterClauseJsonKeyPredicate getFilterClauseJsonKeyPredicate(String clauseFieldName,
                                                                               FilterOperationEnum operationEnum,
                                                                               String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName, operationEnum.getOperation(), clauseValue));
        return new FilterClauseJsonKeyPredicate(clauses);
    }

    @NotNull
    public static FilterClauseJsonValuePredicate getFilterClauseJsonValuePredicate(String clauseFieldName,
                                                                                 FilterOperationEnum operationEnum,
                                                                                 String clauseValue) {
        List<FilterClause> clauses = List.of(new FilterClause(clauseFieldName, operationEnum.getOperation(), clauseValue));
        return new FilterClauseJsonValuePredicate(clauses);
    }

    public static String generateJson(String fieldValue) {
        return String.format("{\"%s\": \"%s\"}",FIELD_NAME, fieldValue);
    }

    private static Headers generateHeader(Map<String,Object> map) {
        RecordHeader[] headerList = map.entrySet().stream()
                .map(entry -> new RecordHeader(entry.getKey(),entry.getValue().toString().getBytes(StandardCharsets.UTF_8)))
                .toArray(RecordHeader[]::new);
        return new RecordHeaders(headerList);
    }

    public static ConsumerRecord<String,Object> generateConsumerRecordWithHeader(Map<String,Object> map) {
        return new ConsumerRecord<>(TOPIC_NAME, 0, 0L, now().toNanoOfDay(),
                             TimestampType.CREATE_TIME, 20, 128,
                             KEY_VALUE, KEY_VALUE, generateHeader(map),
                             Optional.of(1));
    }

}
