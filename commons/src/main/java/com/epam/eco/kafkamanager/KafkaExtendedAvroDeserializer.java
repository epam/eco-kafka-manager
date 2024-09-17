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

import com.epam.eco.kafkamanager.logicaltype.LogicalTypeSchemaConverter;

import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient;
import io.confluent.kafka.serializers.KafkaAvroDeserializer;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaExtendedAvroDeserializer extends KafkaAvroDeserializer {

    private static final int START_SCHEMA_ID_POSITION = 1;
    private static final int END_SCHEMA_ID_POSITION = 5;
    private boolean isKey;

    public KafkaExtendedAvroDeserializer() {}

    public KafkaExtendedAvroDeserializer(SchemaRegistryClient schemaRegistryClient) {
        super(schemaRegistryClient);
    }

    public KafkaExtendedAvroDeserializer(SchemaRegistryClient client, Map<String, ?> props) {
        super(client, props);
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        super.configure(configs, isKey);
        this.isKey = isKey;
    }

    @Override
    public Object deserialize(String topic, byte[] bytes) {
        return getDeserializedResult(topic, bytes);
    }

    @Override
    public Object deserialize(String topic, byte[] bytes, Schema readerSchema) {
        return getDeserializedResult(topic,bytes);
    }

    private Object getDeserializedResult(String topic, byte[] bytes) {
        Object obj = deserialize(topic, isKey, bytes, null);
        if(obj instanceof GenericRecord) {
            return new GenericRecordWrapper((GenericRecord) obj,
                                            getSchemaId(bytes),
                                            LogicalTypeSchemaConverter.convert((GenericRecord) obj));
        } else {
            return obj;
        }
    }

    protected int getSchemaId(byte[] bytes) {
        Validate.isTrue(bytes[0]==0x0,"Deserialization exception: not avro record!");
        Validate.isTrue(bytes.length>END_SCHEMA_ID_POSITION,"Serialized message too short! (bytes length<="+END_SCHEMA_ID_POSITION+")");
        return ByteBuffer.wrap(Arrays.copyOfRange(bytes, START_SCHEMA_ID_POSITION, END_SCHEMA_ID_POSITION)).getInt();
    }

    protected static class GenericRecordWrapper {
        private final GenericRecord value;
        private final long schemaId;
        private final Map<String,Object> valuesAsMap;

        private GenericRecordWrapper(GenericRecord value,
                                     long schemaId,
                                     Map<String, Object> valuesAsMap
        ) {
            this.value = value;
            this.schemaId = schemaId;
            this.valuesAsMap = valuesAsMap;
        }

        public GenericRecord getValue() {
            return value;
        }

        public long getSchemaId() {
            return schemaId;
        }

        public Map<String, Object> getValuesAsMap() {
            return valuesAsMap;
        }
    }

}