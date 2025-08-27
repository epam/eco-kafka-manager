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

import java.util.Map;

import org.apache.kafka.clients.consumer.ConsumerRecord;

/**
 * @author Mikhail_Vershkov
 */

public class KafkaExtendedDeserializerUtils extends KafkaExtendedAvroDeserializer {

    public static Object extractGenericRecordOrValue(ConsumerRecord record) {
        return record.value() instanceof GenericRecordWrapper ?
               ((GenericRecordWrapper) record.value()).getValue() :
               record.value();
    }

    public static Map<String,Object> extractValuesAsMap(ConsumerRecord record) {
        return record.value() instanceof GenericRecordWrapper ?
               ((GenericRecordWrapper) record.value()).getValuesAsMap() :
               null;
    }

    public static long extractSchemaId(ConsumerRecord record) {
        return record.value() instanceof GenericRecordWrapper ?
               ((GenericRecordWrapper) record.value()).getSchemaId() : 0;
    }

}
