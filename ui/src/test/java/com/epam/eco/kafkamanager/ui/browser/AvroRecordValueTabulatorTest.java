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
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

import com.epam.eco.kafkamanager.ui.topics.browser.AvroRecordValueTabulator;
import com.epam.eco.kafkamanager.ui.topics.browser.RegistrySchema;

import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.KEY_STRATEGY_PROPERTY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.RECORD_NAME_STRATEGY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.TOPIC_NAME_STRATEGY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.TOPIC_RECORD_NAME_STRATEGY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.VALUE_STRATEGY_PROPERTY;

/**
 * @author Andrei_Tytsik
 */
public class AvroRecordValueTabulatorTest {

    @Test
    public void testNullIsTabulated() throws Exception {
        AvroRecordValueTabulator tabulator = new AvroRecordValueTabulator(null);

        Map<String, Object> tabular = tabulator.toTabularValue(createConsumerRecord(null));
        Assert.assertNull(tabular);
    }

    @Test
    public void testPrimitiveObjectIsTabulated() throws Exception {
        AvroRecordValueTabulator tabulator = new AvroRecordValueTabulator(null);

        Object[] values = new Object[] {"stringvalue", new Object(), 1L, 1, 1f, 1d, false};
        for (Object value : values) {
            Map<String, Object> tabular = tabulator.toTabularValue(createConsumerRecord(value));
            Assert.assertNotNull(tabular);
            Assert.assertEquals(1, tabular.size());
            Assert.assertTrue(tabular.containsKey(value.getClass().getSimpleName()));
            Assert.assertEquals(value, tabular.get(value.getClass().getSimpleName()));
        }
    }

    @Test
    public void testRecordIsTabulated() throws Exception {
        AvroRecordValueTabulator tabulator = new AvroRecordValueTabulator(null);

        GenericRecord record = createEmptyTestRecord();

        record.put("a", "a");
        record.put("b", "b");
        record.put("c", "c");
        record.put("d", Arrays.asList("1","2","3"));

        GenericRecord subRecord = new GenericData.Record(record.getSchema().getField("e").schema());
        subRecord.put("f", "f");
        subRecord.put("g", "g");
        subRecord.put("h", "h");
        record.put("e", subRecord);

        Map<String, Object> tabular = tabulator.toTabularValue(createConsumerRecord(record));
        Assert.assertNotNull(tabular);
        Assert.assertEquals(7, tabular.size());
        Assert.assertEquals("a", tabular.get("a"));
        Assert.assertEquals("b", tabular.get("b"));
        Assert.assertEquals("c", tabular.get("c"));
        Assert.assertEquals(Arrays.asList("1","2","3"), tabular.get("d"));
        Assert.assertEquals("f", tabular.get("e.f"));
        Assert.assertEquals("g", tabular.get("e.g"));
        Assert.assertEquals("h", tabular.get("e.h"));
    }

    @Test
    public void testSchemaTopicNameStrategy() {
        Config config = new Config(ImmutableList.of(
                new ConfigEntry(KEY_STRATEGY_PROPERTY, TOPIC_NAME_STRATEGY),
                new ConfigEntry(VALUE_STRATEGY_PROPERTY, TOPIC_NAME_STRATEGY)));
        AvroRecordValueTabulator tabulator = new AvroRecordValueTabulator(config);
        GenericRecord record = createEmptyTestRecord();
        RegistrySchema schema = tabulator.getSchema(createConsumerRecord(record));
        Assert.assertEquals(schema.getSchemaKey(), "topic-key");
        Assert.assertEquals(schema.getSchemaValue(), "topic-value");
    }

    @Test
    public void testSchemaRecordNameStrategy() {
        Config config = new Config(ImmutableList.of(
                new ConfigEntry(KEY_STRATEGY_PROPERTY, RECORD_NAME_STRATEGY),
                new ConfigEntry(VALUE_STRATEGY_PROPERTY, RECORD_NAME_STRATEGY)));
        AvroRecordValueTabulator tabulator = new AvroRecordValueTabulator(config);
        GenericRecord record = createEmptyTestRecord();
        RegistrySchema schema = tabulator.getSchema(createConsumerRecord(record));
        Assert.assertEquals(schema.getSchemaKey(), "TestRecord");
        Assert.assertEquals(schema.getSchemaValue(), "TestRecord");
    }

    @Test
    public void testSchemaTopicRecordNameStrategy() {
        Config config = new Config(ImmutableList.of(
                new ConfigEntry(KEY_STRATEGY_PROPERTY, TOPIC_RECORD_NAME_STRATEGY),
                new ConfigEntry(VALUE_STRATEGY_PROPERTY, TOPIC_RECORD_NAME_STRATEGY)));
        AvroRecordValueTabulator tabulator = new AvroRecordValueTabulator(config);
        GenericRecord record = createEmptyTestRecord();
        RegistrySchema schema = tabulator.getSchema(createConsumerRecord(record));
        Assert.assertEquals(schema.getSchemaKey(), "topic-TestRecord");
        Assert.assertEquals(schema.getSchemaValue(), "topic-TestRecord");
    }


    private ConsumerRecord<?, Object> createConsumerRecord(Object value) {
        return new ConsumerRecord<>("topic", 0, 0, null, value);
    }

    private GenericRecord createEmptyTestRecord() {
        Schema schema = new Schema.Parser().parse(
                "{\"type\":\"record\", \"name\": \"TestRecord\", \"fields\":[" +
                    "{\"name\": \"a\", \"type\": \"string\"}," +
                    "{\"name\": \"b\", \"type\": \"string\"}," +
                    "{\"name\": \"c\", \"type\": \"string\"}," +
                    "{\"name\": \"d\", \"type\": {\"type\": \"array\", \"items\": \"string\"}}," +
                    "{\"name\": \"e\", \"type\": {\"type\": \"record\", \"name\": \"TestSubRecord\", \"fields\":[" +
                        "{\"name\": \"f\", \"type\": \"string\"}," +
                        "{\"name\": \"g\", \"type\": \"string\"}," +
                        "{\"name\": \"h\", \"type\": \"string\"}" +
                    "]}}" +
                "]}");
        return new GenericData.Record(schema);
    }

}
