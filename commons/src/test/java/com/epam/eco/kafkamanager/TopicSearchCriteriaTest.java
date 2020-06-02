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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.TopicSearchCriteria.ReplicationState;
import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class TopicSearchCriteriaTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        TopicSearchCriteria origin = TopicSearchCriteria.builder().
                topicName("topicName").
                minPartitionCount(1).
                maxPartitionCount(10).
                minReplicationFactor(2).
                maxReplicationFactor(11).
                replicationStateFully().
                configMap(Collections.singletonMap("key1", "value1")).
                configString("key2:value2;key3:value3").
                description("description").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        TopicSearchCriteria deserialized = mapper.readValue(
                json,
                TopicSearchCriteria.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("topicName", "topic1");
        json.put("minPartitionCount", 1);
        json.put("maxPartitionCount", 10);
        json.put("minReplicationFactor", 1);
        json.put("maxReplicationFactor", 11);
        json.put("replicationState", ReplicationState.UNDER_REPLICATED);
        json.put("configMap", Collections.singletonMap("key1", "value1"));
        json.put("configString", "key2:value2;key3:value3");
        json.put("description", "description");

        TopicSearchCriteria criteria = TopicSearchCriteria.fromJson(json);
        Assert.assertNotNull(criteria);
        Assert.assertEquals("topic1", criteria.getTopicName());
        Assert.assertEquals(Integer.valueOf(1), criteria.getMinPartitionCount());
        Assert.assertEquals(Integer.valueOf(10), criteria.getMaxPartitionCount());
        Assert.assertEquals(Integer.valueOf(1), criteria.getMinReplicationFactor());
        Assert.assertEquals(Integer.valueOf(11), criteria.getMaxReplicationFactor());
        Assert.assertEquals(ReplicationState.UNDER_REPLICATED, criteria.getReplicationState());
        Map<String, Object> config = new HashMap<>();
        config.put("key1", "value1");
        Assert.assertEquals(config, criteria.getConfigMap());
        Assert.assertEquals("key2:value2;key3:value3", criteria.getConfigString());
        Assert.assertEquals("description", criteria.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"topicName\": \"topic1\"" +
                ", \"minPartitionCount\": 1" +
                ", \"maxPartitionCount\": 10" +
                ", \"minReplicationFactor\": 1" +
                ", \"maxReplicationFactor\": 11" +
                ", \"replicationState\": \"UNDER_REPLICATED\"" +
                ", \"configMap\": {\"key1\":\"value1\"}" +
                ", \"configString\": \"key2:value2;key3:value3\"" +
                ", \"description\": \"description\"" +
                "}";

        TopicSearchCriteria criteria = TopicSearchCriteria.fromJson(json);
        Assert.assertNotNull(criteria);
        Assert.assertEquals("topic1", criteria.getTopicName());
        Assert.assertEquals(Integer.valueOf(1), criteria.getMinPartitionCount());
        Assert.assertEquals(Integer.valueOf(10), criteria.getMaxPartitionCount());
        Assert.assertEquals(Integer.valueOf(1), criteria.getMinReplicationFactor());
        Assert.assertEquals(Integer.valueOf(11), criteria.getMaxReplicationFactor());
        Assert.assertEquals(ReplicationState.UNDER_REPLICATED, criteria.getReplicationState());
        Map<String, Object> config = new HashMap<>();
        config.put("key1", "value1");
        Assert.assertEquals(config, criteria.getConfigMap());
        Assert.assertEquals("key2:value2;key3:value3", criteria.getConfigString());
        Assert.assertEquals("description", criteria.getDescription());
    }

    @Test
    public void testConfigStringParsed() throws Exception {
        Map<String, String> config = TopicSearchCriteria.parseConfigString(":");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" :");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" : ");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" : ;   :  ");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" : ;  x :  ");
        Assert.assertNotNull(config);
        Assert.assertEquals(2, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals(null, config.get(null));
        Assert.assertTrue(config.containsKey("x"));
        Assert.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString("x:");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey("x"));
        Assert.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString(" x:");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey("x"));
        Assert.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString(" x :");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey("x"));
        Assert.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString(":y");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals("y", config.get(null));

        config = TopicSearchCriteria.parseConfigString(": y");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals("y", config.get(null));

        config = TopicSearchCriteria.parseConfigString(": y ");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey(null));
        Assert.assertEquals("y", config.get(null));

        config = TopicSearchCriteria.parseConfigString(" x    :  y    ");
        Assert.assertNotNull(config);
        Assert.assertEquals(1, config.size());
        Assert.assertTrue(config.containsKey("x"));
        Assert.assertEquals("y", config.get("x"));

        config = TopicSearchCriteria.parseConfigString(" x    :  y    ;     a:b;   c:   ");
        Assert.assertNotNull(config);
        Assert.assertEquals(3, config.size());
        Assert.assertTrue(config.containsKey("x"));
        Assert.assertEquals("y", config.get("x"));
        Assert.assertTrue(config.containsKey("a"));
        Assert.assertEquals("b", config.get("a"));
        Assert.assertTrue(config.containsKey("c"));
        Assert.assertEquals(null, config.get("c"));
    }

}
