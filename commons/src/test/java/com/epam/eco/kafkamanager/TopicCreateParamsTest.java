/*
 * Copyright 2019 EPAM Systems
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
public class TopicCreateParamsTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        TopicCreateParams origin = TopicCreateParams.builder().
                topicName("topicName").
                partitionCount(42).
                replicationFactor(10).
                appendConfigEntry("key1", "value1").
                appendConfigEntry("key2", "value2").
                appendConfigEntry("key3", "value3").
                description("description").
                appendAttribute("attr1", "value1").
                appendAttribute("attr2", "value2").
                appendAttribute("attr3", "value3").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        TopicCreateParams deserialized = mapper.readValue(
                json,
                TopicCreateParams.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("topicName", "topic1");
        json.put("partitionCount", 42);
        json.put("replicationFactor", 10);
        Map<String, Object> config = new HashMap<>();
        config.put("config1", "value1");
        config.put("config2", "value2");
        config.put("config3", "value3");
        json.put("config", config);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        json.put("attributes", attributes);
        json.put("description", "description");

        TopicCreateParams query = TopicCreateParams.fromJson(json);
        Assert.assertNotNull(query);
        Assert.assertEquals("topic1", query.getTopicName());
        Assert.assertEquals(42, query.getPartitionCount());
        Assert.assertEquals(10, query.getReplicationFactor());
        Assert.assertEquals(config, query.getConfig());
        Assert.assertEquals(attributes, query.getAttributes());
        Assert.assertEquals("description", query.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"topicName\": \"topic1\"" +
                ", \"partitionCount\": 42" +
                ", \"replicationFactor\": 10" +
                ", \"config\": {\"config1\":\"value1\",\"config2\":\"value2\",\"config3\":\"value3\"}" +
                ", \"attributes\": {\"attr1\":\"value1\",\"attr2\":\"value2\",\"attr3\":\"value3\"}" +
                ", \"description\": \"description\"" +
                "}";

        TopicCreateParams query = TopicCreateParams.fromJson(json);
        Assert.assertNotNull(query);
        Assert.assertEquals("topic1", query.getTopicName());
        Assert.assertEquals(42, query.getPartitionCount());
        Assert.assertEquals(10, query.getReplicationFactor());
        Map<String, Object> config = new HashMap<>();
        config.put("config1", "value1");
        config.put("config2", "value2");
        config.put("config3", "value3");
        Assert.assertEquals(config, query.getConfig());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        Assert.assertEquals(attributes, query.getAttributes());
        Assert.assertEquals("description", query.getDescription());
    }

}
