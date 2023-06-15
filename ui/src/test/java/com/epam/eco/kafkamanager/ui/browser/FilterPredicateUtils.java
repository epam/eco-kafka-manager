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
import org.jetbrains.annotations.NotNull;

import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseAvroPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseJsonPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.FilterClauseStringPredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;

/**
 * @author Mikhail_Vershkov
 */

public class FilterPredicateUtils {

    public static final String NAME_SPACE = "testNameSpace";
    public static final String TOPIC_NAME = "testTopic";
    public static final String KEY_VALUE = "testKey";
    public static final String KEY_VALUE_WRONG = "testKeyWrong";
    public static final String FIELD_NAME = "testField";
    public static final String FIELD_TOMBSTONES = "tombstones";
    public static final String FIELD_VALUE_CORRECT = "testValue";
    public static final String FIELD_VALUE_INCORRECT = "testValueInvalid";
    public static final Object NULL_VALUE = null;
    public static final String FIELD_VALUE_EMPTY = "";
    public static final String MAP_FIELD_NAME = "mapField";
    public static final String PREFIX = "prefix";
    public static final String SUFFIX = "suffix";

    public static final Schema TEST_SCHEMA = SchemaBuilder.record(NAME_SPACE)
                                                          .fields()
                                                          .nullableString(FIELD_NAME,"null")
                                                          .name(MAP_FIELD_NAME)
                                                          .type().map().values()
                                                          .stringBuilder()
                                                          .endString()
                                                          .mapDefault(Map.of())
                                                          .endRecord();

    @NotNull
    public static FilterClauseStringPredicate<String, Object> getFilterClauseStringPredicate(String clauseFieldName,
                                                                                       FilterOperationEnum operationEnum,
                                                                                       String clauseValue) {
        Map<String, List<FilterClause>> clauses = Map.of(clauseFieldName,
                                                         List.of(new FilterClause(clauseFieldName,
                                                                                  operationEnum.getOperation(),
                                                                                  clauseValue)));
        return new FilterClauseStringPredicate<>(clauses);
    }

    public static String generateString(String fieldValue) {
        return String.format("%s=%s",FIELD_NAME, fieldValue);
    }

    @NotNull
    public static FilterClauseAvroPredicate<String, Object> getFilterClauseAvroPredicate(String clauseFieldName,
                                                                                   FilterOperationEnum operationEnum,
                                                                                   String clauseValue) {
        Map<String, List<FilterClause>> clauses = Map.of(clauseFieldName,
                                                         List.of(new FilterClause(clauseFieldName,
                                                                                  operationEnum.getOperation(),
                                                                                  clauseValue)));
        return new FilterClauseAvroPredicate<>(clauses);
    }

    @NotNull
    public static GenericRecord getGenericRecord(String fieldName, Object object) {
        GenericRecord genericRecord = new GenericData.Record(TEST_SCHEMA);
        genericRecord.put(fieldName,object);
        return genericRecord;
    }

    @NotNull
    public static FilterClauseJsonPredicate<String, Object> getFilterClauseJsonPredicate(String clauseFieldName,
                                                                                   FilterOperationEnum operationEnum,
                                                                                   String clauseValue) {
        Map<String, List<FilterClause>> clauses = Map.of(clauseFieldName,
                                                         List.of(new FilterClause(clauseFieldName,
                                                                                  operationEnum.getOperation(),
                                                                                  clauseValue)));
        return new FilterClauseJsonPredicate<>(clauses);
    }

    public static String generateJson(String fieldValue) {
        return String.format("{\"%s\": \"%s\"}",FIELD_NAME, fieldValue);
    }


}
