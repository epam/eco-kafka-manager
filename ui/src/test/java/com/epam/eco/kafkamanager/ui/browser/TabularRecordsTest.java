/*
 * Copyright 2019 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager.ui.browser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;
import org.junit.Assert;
import org.junit.Test;

import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords;
import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.Column;
import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.Record;

/**
 * @author Andrei_Tytsik
 */
public class TabularRecordsTest {

    @Test
    public void testHasExpectedValuesAndIsIterable() throws Exception {
        TabularRecords records = TabularRecords.builder().
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

        Assert.assertNotNull(records);
        Assert.assertEquals(6, records.size());

        Assert.assertEquals("key1", records.getRecord(0).getKey());
        Assert.assertTrue(records.getRecord(0).containsColumn("columnA"));
        Assert.assertTrue(records.getRecord(0).containsColumn("columnB"));

        Assert.assertEquals("key2", records.getRecord(1).getKey());
        Assert.assertTrue(records.getRecord(1).containsColumn("columnC"));
        Assert.assertTrue(records.getRecord(1).containsColumn("columnD"));

        Assert.assertEquals("key3", records.getRecord(2).getKey());
        Assert.assertTrue(records.getRecord(2).containsColumn("columnE"));

        Assert.assertEquals(null, records.getRecord(3).getKey());
        Assert.assertTrue(records.getRecord(3).isNullKey());
        Assert.assertTrue(records.getRecord(3).containsColumn("columnF"));

        Assert.assertEquals("key4", records.getRecord(4).getKey());
        Assert.assertTrue(records.getRecord(4).isNullValue());
        Assert.assertTrue(records.getRecord(4).getColumnNames().isEmpty());

        Assert.assertEquals(null, records.getRecord(5).getKey());
        Assert.assertTrue(records.getRecord(5).isNullKey());
        Assert.assertTrue(records.getRecord(5).isNullValue());
        Assert.assertTrue(records.getRecord(4).getColumnNames().isEmpty());

        List<Column> columns = records.listColumns();
        Assert.assertNotNull(columns);
        Assert.assertEquals(7, columns.size());

        Assert.assertEquals("columnA", columns.get(0).getName());
        Assert.assertEquals(true, columns.get(0).isPresent());
        Assert.assertEquals(true, columns.get(0).isSelected());

        Assert.assertEquals("columnB", columns.get(1).getName());
        Assert.assertEquals(true, columns.get(1).isPresent());
        Assert.assertEquals(true, columns.get(1).isSelected());

        Assert.assertEquals("columnC", columns.get(2).getName());
        Assert.assertEquals(true, columns.get(2).isPresent());
        Assert.assertEquals(true, columns.get(2).isSelected());

        Assert.assertEquals("columnD", columns.get(3).getName());
        Assert.assertEquals(true, columns.get(3).isPresent());
        Assert.assertEquals(true, columns.get(3).isSelected());

        Assert.assertEquals("columnE", columns.get(4).getName());
        Assert.assertEquals(true, columns.get(4).isPresent());
        Assert.assertEquals(true, columns.get(4).isSelected());

        Assert.assertEquals("columnF", columns.get(5).getName());
        Assert.assertEquals(true, columns.get(5).isPresent());
        Assert.assertEquals(true, columns.get(5).isSelected());

        Assert.assertEquals("columnX", columns.get(6).getName());
        Assert.assertEquals(false, columns.get(6).isPresent());
        Assert.assertEquals(true, columns.get(6).isSelected());

        List<Column> columnsPresent = records.listPresentColumns();
        Assert.assertNotNull(columnsPresent);
        Assert.assertEquals(6, columnsPresent.size());
        Assert.assertEquals("columnA", columnsPresent.get(0).getName());
        Assert.assertEquals("columnB", columnsPresent.get(1).getName());
        Assert.assertEquals("columnC", columnsPresent.get(2).getName());
        Assert.assertEquals("columnD", columnsPresent.get(3).getName());
        Assert.assertEquals("columnE", columnsPresent.get(4).getName());
        Assert.assertEquals("columnF", columnsPresent.get(5).getName());

        List<Column> columnsSelected = records.listSelectedColumns();
        Assert.assertNotNull(columnsSelected);
        Assert.assertEquals(7, columnsSelected.size());
        Assert.assertEquals("columnA", columnsSelected.get(0).getName());
        Assert.assertEquals("columnB", columnsSelected.get(1).getName());
        Assert.assertEquals("columnC", columnsSelected.get(2).getName());
        Assert.assertEquals("columnD", columnsSelected.get(3).getName());
        Assert.assertEquals("columnE", columnsSelected.get(4).getName());
        Assert.assertEquals("columnF", columnsSelected.get(5).getName());
        Assert.assertEquals("columnX", columnsSelected.get(6).getName());
    }

    @Test(expected=Exception.class)
    public void testFailsOnIllegalColumnNames() throws Exception {
        TabularRecords.builder().
                addSelectedColumnNames(Arrays.asList("column1", null, "column2")).
                addRecord(createTestRecord("key", "column")).
                build();
    }

    @Test(expected=Exception.class)
    public void testFailsOnNullRecords() throws Exception {
        new TabularRecords(null);
    }

    @Test
    public void testColumnsNamesAreGroupped() throws Exception {
        TabularRecords records = TabularRecords.builder().
                addSelectedColumnNames(Arrays.asList(
                        "columnA", "columnB", "columnC", "columnD",
                        "columnE", "columnF", "columnG", "columnH",
                        "columnI", "columnJ")).
                build();

        List<Column> group0 = records.getColumnsGroup(0, 3);
        Assert.assertNotNull(group0);
        Assert.assertEquals(3, group0.size());
        Assert.assertEquals("columnA", group0.get(0).getName());
        Assert.assertEquals("columnB", group0.get(1).getName());
        Assert.assertEquals("columnC", group0.get(2).getName());

        List<Column> group1 = records.getColumnsGroup(1, 3);
        Assert.assertNotNull(group1);
        Assert.assertEquals(3, group1.size());
        Assert.assertEquals("columnD", group1.get(0).getName());
        Assert.assertEquals("columnE", group1.get(1).getName());
        Assert.assertEquals("columnF", group1.get(2).getName());

        List<Column> group2 = records.getColumnsGroup(2, 3);
        Assert.assertNotNull(group2);
        Assert.assertEquals(3, group2.size());
        Assert.assertEquals("columnG", group2.get(0).getName());
        Assert.assertEquals("columnH", group2.get(1).getName());
        Assert.assertEquals("columnI", group2.get(2).getName());

        List<Column> group3 = records.getColumnsGroup(3, 3);
        Assert.assertNotNull(group3);
        Assert.assertEquals(1, group3.size());
        Assert.assertEquals("columnJ", group3.get(0).getName());

        List<Column> group4 = records.getColumnsGroup(4, 3);
        Assert.assertNotNull(group4);
        Assert.assertEquals(0, group4.size());
    }

    @Test
    public void testColumnsNamesGroupSizeIsDetermined() throws Exception {
        TabularRecords records = TabularRecords.builder().
                addSelectedColumnNames(Arrays.asList(
                        "columnA", "columnB", "columnC", "columnD",
                        "columnE", "columnF", "columnG", "columnH",
                        "columnI", "columnJ")).
                build();

        int size = records.determineColumnsGroupSize(1, 1);
        Assert.assertEquals(10, size);

        size = records.determineColumnsGroupSize(2, 1);
        Assert.assertEquals(5, size);

        size = records.determineColumnsGroupSize(3, 1);
        Assert.assertEquals(4, size);

        size = records.determineColumnsGroupSize(4, 1);
        Assert.assertEquals(3, size);

        size = records.determineColumnsGroupSize(5, 1);
        Assert.assertEquals(2, size);

        size = records.determineColumnsGroupSize(6, 1);
        Assert.assertEquals(2, size);

        size = records.determineColumnsGroupSize(7, 1);
        Assert.assertEquals(2, size);

        size = records.determineColumnsGroupSize(8, 1);
        Assert.assertEquals(2, size);

        size = records.determineColumnsGroupSize(9, 1);
        Assert.assertEquals(2, size);

        size = records.determineColumnsGroupSize(10, 1);
        Assert.assertEquals(1, size);

        size = records.determineColumnsGroupSize(11, 1);
        Assert.assertEquals(1, size);

        size = records.determineColumnsGroupSize(12, 1);
        Assert.assertEquals(1, size);
    }

    @Test
    public void testAttributesHaveExpectedValues() throws Exception {
        Record record = new Record(createTestConsumerRecord(null), null, null);

        Assert.assertNull(record.getAttributesJson());
        Assert.assertNull(record.getAttributesPrettyJson());

        record = new Record(
                createTestConsumerRecord(null),
                null,
                Collections.singletonMap("attr_key", "attr_value"));

        Assert.assertNotNull(record.getAttributesJson());
        Assert.assertEquals("{\"attr_key\":\"attr_value\"}", record.getAttributesJson());
        Assert.assertNotNull(record.getAttributesPrettyJson());
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
        return new Record(createTestConsumerRecord(key), values, null);
    }

    private ConsumerRecord<?, ?> createTestConsumerRecord(String key) {
        return new ConsumerRecord<>(
                "topic",
                0,
                0,
                System.currentTimeMillis(),
                TimestampType.NO_TIMESTAMP_TYPE,
                1L,
                1,
                1,
                key,
                null);
    }

}
