/*******************************************************************************
 *  Copyright 2022 EPAM Systems
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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords;
import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.Column;
import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.Record;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Andrei_Tytsik
 */
public class TabularRecordsTest {

    @Test
    public void testHasExpectedValuesAndIsIterable() throws Exception {
        TabularRecords records = TabularRecords.builder("topicName").
                addSelectedColumnName("columnA").
                addSelectedColumnName("columnB").
                addSelectedColumnNames(Arrays.asList("columnC", "columnD", "columnE")).
                addSelectedColumnName("columnF").
                addSelectedColumnName("columnX").
                addRecord(createTestRecord("key1", "columnA", "columnB")).
                addRecord(createTestRecord("key2", "columnC", "columnD")).
                addRecord(createTestRecord("key3", "columnE")).
                addRecord(createNullKeyTestRecord("columnF")).
                addRecord(createNullValueTestRecord("key4")).
                addRecord(createNullKeyNullValueTestRecord()).
                build();

        Assertions.assertNotNull(records);
        Assertions.assertEquals(6, records.size());

        Assertions.assertEquals("key1", records.getRecord(0).getKey());
        Assertions.assertTrue(records.getRecord(0).containsColumn("columnA"));
        Assertions.assertTrue(records.getRecord(0).containsColumn("columnB"));

        Assertions.assertEquals("key2", records.getRecord(1).getKey());
        Assertions.assertTrue(records.getRecord(1).containsColumn("columnC"));
        Assertions.assertTrue(records.getRecord(1).containsColumn("columnD"));

        Assertions.assertEquals("key3", records.getRecord(2).getKey());
        Assertions.assertTrue(records.getRecord(2).containsColumn("columnE"));

        Assertions.assertNull(records.getRecord(3).getKey());
        Assertions.assertTrue(records.getRecord(3).isNullKey());
        Assertions.assertTrue(records.getRecord(3).containsColumn("columnF"));

        Assertions.assertEquals("key4", records.getRecord(4).getKey());
        Assertions.assertTrue(records.getRecord(4).isNullValue());
        Assertions.assertTrue(records.getRecord(4).getColumnNames().isEmpty());

        Assertions.assertNull(records.getRecord(5).getKey());
        Assertions.assertTrue(records.getRecord(5).isNullKey());
        Assertions.assertTrue(records.getRecord(5).isNullValue());
        Assertions.assertTrue(records.getRecord(4).getColumnNames().isEmpty());

        List<Column> columns = records.listColumns();
        Assertions.assertNotNull(columns);
        Assertions.assertEquals(7, columns.size());

        Assertions.assertEquals("columnA", columns.get(0).getName());
        Assertions.assertTrue(columns.get(0).isPresent());
        Assertions.assertTrue(columns.get(0).isSelected());

        Assertions.assertEquals("columnB", columns.get(1).getName());
        Assertions.assertTrue(columns.get(1).isPresent());
        Assertions.assertTrue(columns.get(1).isSelected());

        Assertions.assertEquals("columnC", columns.get(2).getName());
        Assertions.assertTrue(columns.get(2).isPresent());
        Assertions.assertTrue(columns.get(2).isSelected());

        Assertions.assertEquals("columnD", columns.get(3).getName());
        Assertions.assertTrue(columns.get(3).isPresent());
        Assertions.assertTrue(columns.get(3).isSelected());

        Assertions.assertEquals("columnE", columns.get(4).getName());
        Assertions.assertTrue(columns.get(4).isPresent());
        Assertions.assertTrue(columns.get(4).isSelected());

        Assertions.assertEquals("columnF", columns.get(5).getName());
        Assertions.assertTrue(columns.get(5).isPresent());
        Assertions.assertTrue(columns.get(5).isSelected());

        Assertions.assertEquals("columnX", columns.get(6).getName());
        Assertions.assertFalse(columns.get(6).isPresent());
        Assertions.assertTrue(columns.get(6).isSelected());

        List<Column> columnsPresent = records.listPresentColumns();
        Assertions.assertNotNull(columnsPresent);
        Assertions.assertEquals(6, columnsPresent.size());
        Assertions.assertEquals("columnA", columnsPresent.get(0).getName());
        Assertions.assertEquals("columnB", columnsPresent.get(1).getName());
        Assertions.assertEquals("columnC", columnsPresent.get(2).getName());
        Assertions.assertEquals("columnD", columnsPresent.get(3).getName());
        Assertions.assertEquals("columnE", columnsPresent.get(4).getName());
        Assertions.assertEquals("columnF", columnsPresent.get(5).getName());

        List<Column> columnsSelected = records.listSelectedColumns();
        Assertions.assertNotNull(columnsSelected);
        Assertions.assertEquals(7, columnsSelected.size());
        Assertions.assertEquals("columnA", columnsSelected.get(0).getName());
        Assertions.assertEquals("columnB", columnsSelected.get(1).getName());
        Assertions.assertEquals("columnC", columnsSelected.get(2).getName());
        Assertions.assertEquals("columnD", columnsSelected.get(3).getName());
        Assertions.assertEquals("columnE", columnsSelected.get(4).getName());
        Assertions.assertEquals("columnF", columnsSelected.get(5).getName());
    }

    @Test
    public void testFailsOnIllegalColumnNames() throws Exception {
        assertThrows(IllegalArgumentException.class,()->{
            TabularRecords.builder("topicName").
                    addSelectedColumnNames(Arrays.asList("column1", null, "column2")).
                                  addRecord(createTestRecord("key", "column")).
                                  build();
        });
    }

    @Test
    public void testFailsOnNullRecords() throws Exception {
        assertThrows(Exception.class,()-> new TabularRecords(null,"topicName") );
    }

    @Test
    public void testAttributesHaveExpectedValues() throws Exception {
        Record record = new Record(createTestConsumerRecord(null), null, null, null, null);

        Assertions.assertNull(record.getAttributesJson());
        Assertions.assertNull(record.getAttributesPrettyJson());

        record = new Record(
                createTestConsumerRecord(null),
                null,
                Collections.singletonMap("attr_key", "attr_value"),
                new HashMap<>(), null);

        Assertions.assertNotNull(record.getAttributesJson());
        Assertions.assertEquals("{\"attr_key\":\"attr_value\"}", record.getAttributesJson());
        Assertions.assertNotNull(record.getAttributesPrettyJson());
    }

    private Record createNullKeyNullValueTestRecord() {
        return createTestRecord(null);
    }

    private Record createNullKeyTestRecord(String ... columnNames) {
        return createTestRecord(null, columnNames);
    }

    private Record createNullValueTestRecord(String key) {
        return createTestRecord(key);
    }

    private Record createTestRecord(String key, String ... columnNames) {
        Map<String, Object> values = null;
        if (columnNames != null && columnNames.length > 0) {
            values = new HashMap<>();
            for (String columnName : columnNames) {
                values.put(columnName, columnName + "_value");
            }
        }
        return new Record(createTestConsumerRecord(key), values, null, null, null);
    }

    private ConsumerRecord<?, ?> createTestConsumerRecord(String key) {
        return new ConsumerRecord<>(
                "topic",
                0,
                0,
                System.currentTimeMillis(),
                TimestampType.NO_TIMESTAMP_TYPE,
                1,
                1,
                key,
                null,
                new RecordHeaders(),
                Optional.empty());
    }

}
