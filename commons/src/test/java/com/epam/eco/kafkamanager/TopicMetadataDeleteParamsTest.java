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

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class TopicMetadataDeleteParamsTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        TopicMetadataDeleteParams origin = TopicMetadataDeleteParams.builder().
                topicName("topicName").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assertions.assertNotNull(json);

        TopicMetadataDeleteParams deserialized = mapper.readValue(
                json,
                TopicMetadataDeleteParams.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("topicName", "topic1");

        TopicMetadataDeleteParams params = TopicMetadataDeleteParams.fromJson(json);
        Assertions.assertNotNull(params);
        Assertions.assertEquals("topic1", params.getTopicName());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"topicName\": \"topic1\"" +
                "}";

        TopicMetadataDeleteParams params = TopicMetadataDeleteParams.fromJson(json);
        Assertions.assertNotNull(params);
        Assertions.assertEquals("topic1", params.getTopicName());
    }

}
