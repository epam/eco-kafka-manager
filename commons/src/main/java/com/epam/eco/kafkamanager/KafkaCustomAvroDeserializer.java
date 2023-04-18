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
package com.epam.eco.kafkamanager;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Map;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaCustomAvroDeserializer extends KafkaAvroDeserializer {

    private static final int START_SCHEMA_ID_POSITION = 1;
    private static final int END_SCHEMA_ID_POSITION = 5;
    private boolean isKey;

    public KafkaCustomAvroDeserializer() {}

    public KafkaCustomAvroDeserializer(SchemaRegistryClient schemaRegistryClient) {
        schemaRegistry = schemaRegistryClient;
    }

    public KafkaCustomAvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
        schemaRegistry = client;
        configure(this.deserializerConfig(props));
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.isKey = isKey;
        super.configure(new KafkaAvroDeserializerConfig(configs));
    }

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        return new GenericRecordWrapper((GenericRecord) deserialize(topic, isKey, bytes, null),
                                            getSchemaId(bytes));
    }

    @Override
    public Object deserialize(String topic, byte[] bytes, Schema readerSchema) {
        return new GenericRecordWrapper((GenericRecord) deserialize(topic, isKey, bytes, readerSchema),
                                            getSchemaId(bytes));
    }

    private int getSchemaId(byte[] bytes) {
        Validate.isTrue(bytes[0]==0x0,"Deserialization exception: not avro record!");
        return ByteBuffer.wrap(Arrays.copyOfRange(bytes, START_SCHEMA_ID_POSITION, END_SCHEMA_ID_POSITION)).getInt();
    }

    private static class GenericRecordWrapper {
        private final GenericRecord genericRecord;
        private final long schemaId;

        public GenericRecordWrapper(GenericRecord genericRecord, long schemaId) {
            this.genericRecord = genericRecord;
            this.schemaId = schemaId;
        }

        public GenericRecord getGenericRecord() {
            return genericRecord;
        }

        public long getSchemaId() {
            return schemaId;
        }

    }

    public static Object extractGenericRecord(ConsumerRecord record) {
        return record.value() instanceof GenericRecordWrapper ?
                               ((GenericRecordWrapper) record.value()).getGenericRecord() :
                               record.value();
    }
    public static long extractSchemaId(ConsumerRecord record) {
        return record.value() instanceof GenericRecordWrapper ?
               ((GenericRecordWrapper) record.value()).getSchemaId() : 0;
    }

}

