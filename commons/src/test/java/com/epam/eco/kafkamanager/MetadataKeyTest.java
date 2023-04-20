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

import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class MetadataKeyTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        MetadataKey origin1 = BrokerMetadataKey.with(42);
        MetadataKey origin2 = ConsumerGroupMetadataKey.with("groupName");
        MetadataKey origin3 = PermissionMetadataKey.with(
                "user:user",
                ResourceType.TOPIC,
                "topicName",
                PatternType.LITERAL);
        MetadataKey origin4 = TopicMetadataKey.with("topicName");

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json1 = mapper.writeValueAsString(origin1);
        Assertions.assertNotNull(json1);

        String json2 = mapper.writeValueAsString(origin2);
        Assertions.assertNotNull(json2);

        String json3 = mapper.writeValueAsString(origin3);
        Assertions.assertNotNull(json3);

        String json4 = mapper.writeValueAsString(origin4);
        Assertions.assertNotNull(json4);

        MetadataKey deserialized1 = mapper.readValue(json1, MetadataKey.class);
        Assertions.assertNotNull(deserialized1);
        Assertions.assertEquals(origin1, deserialized1);

        MetadataKey deserialized2 = mapper.readValue(json2, MetadataKey.class);
        Assertions.assertNotNull(deserialized2);
        Assertions.assertEquals(origin2, deserialized2);

        MetadataKey deserialized3 = mapper.readValue(json3, MetadataKey.class);
        Assertions.assertNotNull(deserialized3);
        Assertions.assertEquals(origin3, deserialized3);

        MetadataKey deserialized4 = mapper.readValue(json4, MetadataKey.class);
        Assertions.assertNotNull(deserialized4);
        Assertions.assertEquals(origin4, deserialized4);
    }

}
