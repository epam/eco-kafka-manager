/*
 * Copyright 2020 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.epam.eco.kafkamanager;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class TopicMetadataUpdateParamsTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        TopicMetadataUpdateParams origin = TopicMetadataUpdateParams.builder().
                topicName("topicName").
                description("description").
                appendAttribute("attr1", "value1").
                appendAttribute("attr2", "value2").
                appendAttribute("attr3", "value3").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        TopicMetadataUpdateParams deserialized = mapper.readValue(
                json,
                TopicMetadataUpdateParams.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("topicName", "topic1");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        json.put("attributes", attributes);
        json.put("description", "description");

        TopicMetadataUpdateParams params = TopicMetadataUpdateParams.fromJson(json);
        Assert.assertNotNull(params);
        Assert.assertEquals("topic1", params.getTopicName());
        Assert.assertEquals(attributes, params.getAttributes());
        Assert.assertEquals("description", params.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"topicName\": \"topic1\"" +
                ", \"attributes\": {\"attr1\":\"value1\",\"attr2\":\"value2\",\"attr3\":\"value3\"}" +
                ", \"description\": \"description\"" +
                "}";

        TopicMetadataUpdateParams params = TopicMetadataUpdateParams.fromJson(json);
        Assert.assertNotNull(params);
        Assert.assertEquals("topic1", params.getTopicName());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        Assert.assertEquals(attributes, params.getAttributes());
        Assert.assertEquals("description", params.getDescription());
    }

}
