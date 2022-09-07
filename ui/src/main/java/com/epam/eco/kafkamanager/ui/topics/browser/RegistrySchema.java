package com.epam.eco.kafkamanager.ui.topics.browser;
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

import org.apache.avro.generic.GenericContainer;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.google.protobuf.DynamicMessage;

import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.ui.utils.SchemaSubjectUtils;

/**
 * @author Mikhail_Vershkov
 */
public class RegistrySchema {

    private final String schemaName;
    private final String schemaKey;
    private final String schemaValue;
    private final String schemaAsString;

    private final TopicRecordFetchParams.DataFormat schemaType;

    public RegistrySchema(ConsumerRecord<?, ?> record,
                          String schemaName,
                          Config kafkaTopicConfig,
                          TopicRecordFetchParams.DataFormat schemaType ) {
        this.schemaType = schemaType;
        this.schemaName = schemaName;
        if(schemaType == TopicRecordFetchParams.DataFormat.AVRO || schemaType == TopicRecordFetchParams.DataFormat.PROTOCOL_BUFFERS) {
            this.schemaKey = SchemaSubjectUtils.getSchemaSubjectKey(record.topic(), schemaName, kafkaTopicConfig);
            this.schemaValue = SchemaSubjectUtils.getSchemaSubjectValue(record.topic(), schemaName, kafkaTopicConfig);
        } else {
            this.schemaKey = "";
            this.schemaValue = "";

        }

        switch (schemaType) {
            case AVRO: {
                this.schemaAsString = ((GenericContainer) record.value()).getSchema().toString(true);
                break;
            }
            case PROTOCOL_BUFFERS: {
                this.schemaAsString = ((DynamicMessage) record.value()).getDescriptorForType().getFile().toProto().toString();
                break;
            }
            default: {
                this.schemaAsString = "";
                break;
            }
        }

    }

    public TopicRecordFetchParams.DataFormat getSchemaType() {
        return schemaType;
    }

    public String getSchemaAsString() {
        return schemaAsString;
    }

    public String getSchemaValue() {
        return schemaValue;
    }

    public String getSchemaKey() {
        return schemaKey;
    }

    public String getSchemaName() {
        return schemaName;
    }

}
