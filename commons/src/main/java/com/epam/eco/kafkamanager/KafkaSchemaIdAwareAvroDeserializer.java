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

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaSchemaIdAwareAvroDeserializer extends KafkaAvroDeserializer {

    private static final int START_SCHEMA_ID_POSITION = 1;
    private static final int END_SCHEMA_ID_POSITION = 5;
    private boolean isKey;

    public KafkaSchemaIdAwareAvroDeserializer() {}

    public KafkaSchemaIdAwareAvroDeserializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    public KafkaSchemaIdAwareAvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
        super(client,props);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        super.configure(configs,isKey);
        this.isKey = isKey;
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

    protected int getSchemaId(byte[] bytes) {
        Validate.isTrue(bytes[0]==0x0,"Deserialization exception: not avro record!");
        Validate.isTrue(bytes.length>END_SCHEMA_ID_POSITION,"Serialized message too short! (bytes<="+END_SCHEMA_ID_POSITION+")");
        return ByteBuffer.wrap(Arrays.copyOfRange(bytes, START_SCHEMA_ID_POSITION, END_SCHEMA_ID_POSITION)).getInt();
    }

    protected static class GenericRecordWrapper {
        private final GenericRecord genericRecord;
        private final long schemaId;

        private GenericRecordWrapper(GenericRecord genericRecord, long schemaId) {
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

}

