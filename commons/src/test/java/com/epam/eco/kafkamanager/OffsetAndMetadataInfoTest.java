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
package com.epam.eco.kafkamanager;

import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
class OffsetAndMetadataInfoTest {

    @Test
    void testSerializedToJsonAndBack() throws Exception {
        OffsetAndMetadataInfo origin = OffsetAndMetadataInfo.builder().
                topicPartition(new TopicPartition("topic", 0)).
                offset(42).
                metadata("metadata").
                commitDate(System.currentTimeMillis()).
                expireDate(System.currentTimeMillis()).
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assertions.assertNotNull(json);

        OffsetAndMetadataInfo deserialized = mapper.readValue(
                json,
                OffsetAndMetadataInfo.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
    }

    @Test
    void testFixNegativeOffsetToZero() throws Exception {
        long now = System.currentTimeMillis();
        OffsetAndMetadataInfo origin = OffsetAndMetadataInfo.builder().
                topicPartition(new TopicPartition("topic", 0)).
                offset(-1).
                metadata("metadata").
                commitDate(now).
                expireDate(now).
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assertions.assertNotNull(json);

        OffsetAndMetadataInfo deserialized = mapper.readValue(
                json,
                OffsetAndMetadataInfo.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
        Assertions.assertEquals(0L, deserialized.getOffset());
    }

}
