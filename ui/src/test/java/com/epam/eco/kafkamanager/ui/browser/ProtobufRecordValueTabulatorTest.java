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

import java.util.Collections;
import java.util.Map;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.Assert;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;

import com.epam.eco.kafkamanager.ui.browser.protobuf.Schema;
import com.epam.eco.kafkamanager.ui.topics.browser.ProtobufRecordValueTabulator;
import com.epam.eco.kafkamanager.ui.topics.browser.RecordSchema;

import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.KEY_STRATEGY_PROPERTY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.RECORD_NAME_STRATEGY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.TOPIC_NAME_STRATEGY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.TOPIC_RECORD_NAME_STRATEGY;
import static com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils.VALUE_STRATEGY_PROPERTY;

/**
 * @author Vershkov_Mikhail
 */
public class ProtobufRecordValueTabulatorTest {

    private static final String OPERATION_ID = "QWER-TYUI-OPAS-DFGH";
    private static final Integer DOC_ID = 12345;
    private static final String METADATA_BT = "2022-08-22T10:00:00.0000000";
    private static final String METADATA_TT = "2022-08-22T12:00:00.0000000";

    @Test
    public void testNullIsTabulated() {
        ProtobufRecordValueTabulator tabulator = new ProtobufRecordValueTabulator(new Config(Collections.emptyList()));
        Map<String, Object> tabular = tabulator.toTabularValue(createConsumerRecord(null));
        Assert.assertNull(tabular);
    }

    @Test
    public void testRecordIsTabulated() {
        ProtobufRecordValueTabulator tabulator = new ProtobufRecordValueTabulator(new Config(Collections.emptyList()));

        DynamicMessage dynamicMessage = createDynamicMessage();

        Map<String, Object> tabular = tabulator.toTabularValue(createConsumerRecord(dynamicMessage));
        Assert.assertNotNull(tabular);
        Assert.assertEquals(4, tabular.size());
        Assert.assertEquals(OPERATION_ID, tabular.get("operationId"));
        Assert.assertEquals(DOC_ID, tabular.get("docId"));
        Assert.assertEquals(METADATA_BT, tabular.get("metadataBt"));
        Assert.assertEquals(METADATA_TT, tabular.get("metadataTt"));
    }

    @Test
    public void testSchemaTopicNameStrategy() {
        Config config = new Config(ImmutableList.of(
                new ConfigEntry(KEY_STRATEGY_PROPERTY, TOPIC_NAME_STRATEGY),
                new ConfigEntry(VALUE_STRATEGY_PROPERTY, TOPIC_NAME_STRATEGY)));
        ProtobufRecordValueTabulator tabulator = new ProtobufRecordValueTabulator(config);
        DynamicMessage dynamicMessage = createDynamicMessage();
        RecordSchema schema = tabulator.getSchema(createConsumerRecord(dynamicMessage));
        Assert.assertEquals(schema.getSchemaKey(), "testTopic-key");
        Assert.assertEquals(schema.getSchemaValue(), "testTopic-value");
    }

    @Test
    public void testSchemaRecordNameStrategy() {
        Config config = new Config(ImmutableList.of(
                new ConfigEntry(KEY_STRATEGY_PROPERTY, RECORD_NAME_STRATEGY),
                new ConfigEntry(VALUE_STRATEGY_PROPERTY, RECORD_NAME_STRATEGY)));
        ProtobufRecordValueTabulator tabulator = new ProtobufRecordValueTabulator(config);
        DynamicMessage dynamicMessage = createDynamicMessage();
        RecordSchema schema = tabulator.getSchema(createConsumerRecord(dynamicMessage));
        Assert.assertEquals(schema.getSchemaKey(), "kafkaManager.test.ProtobufRecord");
        Assert.assertEquals(schema.getSchemaValue(), "kafkaManager.test.ProtobufRecord");
    }

    @Test
    public void testSchemaTopicRecordNameStrategy() {
        Config config = new Config(ImmutableList.of(
                new ConfigEntry(KEY_STRATEGY_PROPERTY, TOPIC_RECORD_NAME_STRATEGY),
                new ConfigEntry(VALUE_STRATEGY_PROPERTY, TOPIC_RECORD_NAME_STRATEGY)));
        ProtobufRecordValueTabulator tabulator = new ProtobufRecordValueTabulator(config);
        DynamicMessage dynamicMessage = createDynamicMessage();
        RecordSchema schema = tabulator.getSchema(createConsumerRecord(dynamicMessage));
        Assert.assertEquals(schema.getSchemaKey(), "testTopic-kafkaManager.test.ProtobufRecord");
        Assert.assertEquals(schema.getSchemaValue(), "testTopic-kafkaManager.test.ProtobufRecord");
    }

    private DynamicMessage createDynamicMessage() {

        String messageTypeName = "ProtobufRecord";
        Descriptors.FileDescriptor fileDescriptor = Schema.getDescriptor();
        Descriptors.Descriptor descriptor = fileDescriptor.findMessageTypeByName(messageTypeName);

        return DynamicMessage.newBuilder(descriptor)
                .setField(descriptor.findFieldByName("operationId"), OPERATION_ID)
                .setField(descriptor.findFieldByName("docId"), DOC_ID)
                .setField(descriptor.findFieldByName("metadataBt"), METADATA_BT)
                .setField(descriptor.findFieldByName("metadataTt"), METADATA_TT)
                .build();
    }

    private ConsumerRecord<String, Object> createConsumerRecord(DynamicMessage dynamicMessage) {
        return new ConsumerRecord<>("testTopic", 1, 0, "test-key", dynamicMessage);
    }

}
