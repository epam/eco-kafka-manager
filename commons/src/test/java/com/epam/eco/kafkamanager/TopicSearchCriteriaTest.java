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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
        Assertions.assertNotNull(json);

        TopicSearchCriteria deserialized = mapper.readValue(
                json,
                TopicSearchCriteria.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
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
        Assertions.assertNotNull(criteria);
        Assertions.assertEquals("topic1", criteria.getTopicName());
        Assertions.assertEquals(Integer.valueOf(1), criteria.getMinPartitionCount());
        Assertions.assertEquals(Integer.valueOf(10), criteria.getMaxPartitionCount());
        Assertions.assertEquals(Integer.valueOf(1), criteria.getMinReplicationFactor());
        Assertions.assertEquals(Integer.valueOf(11), criteria.getMaxReplicationFactor());
        Assertions.assertEquals(ReplicationState.UNDER_REPLICATED, criteria.getReplicationState());
        Map<String, Object> config = new HashMap<>();
        config.put("key1", "value1");
        Assertions.assertEquals(config, criteria.getConfigMap());
        Assertions.assertEquals("key2:value2;key3:value3", criteria.getConfigString());
        Assertions.assertEquals("description", criteria.getDescription());
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
        Assertions.assertNotNull(criteria);
        Assertions.assertEquals("topic1", criteria.getTopicName());
        Assertions.assertEquals(Integer.valueOf(1), criteria.getMinPartitionCount());
        Assertions.assertEquals(Integer.valueOf(10), criteria.getMaxPartitionCount());
        Assertions.assertEquals(Integer.valueOf(1), criteria.getMinReplicationFactor());
        Assertions.assertEquals(Integer.valueOf(11), criteria.getMaxReplicationFactor());
        Assertions.assertEquals(ReplicationState.UNDER_REPLICATED, criteria.getReplicationState());
        Map<String, Object> config = new HashMap<>();
        config.put("key1", "value1");
        Assertions.assertEquals(config, criteria.getConfigMap());
        Assertions.assertEquals("key2:value2;key3:value3", criteria.getConfigString());
        Assertions.assertEquals("description", criteria.getDescription());
    }

    @Test
    public void testConfigStringParsed() throws Exception {
        Map<String, String> config = TopicSearchCriteria.parseConfigString(":");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" :");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" : ");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" : ;   :  ");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals(null, config.get(null));

        config = TopicSearchCriteria.parseConfigString(" : ;  x :  ");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(2, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals(null, config.get(null));
        Assertions.assertTrue(config.containsKey("x"));
        Assertions.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString("x:");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey("x"));
        Assertions.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString(" x:");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey("x"));
        Assertions.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString(" x :");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey("x"));
        Assertions.assertEquals(null, config.get("x"));

        config = TopicSearchCriteria.parseConfigString(":y");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals("y", config.get(null));

        config = TopicSearchCriteria.parseConfigString(": y");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals("y", config.get(null));

        config = TopicSearchCriteria.parseConfigString(": y ");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey(null));
        Assertions.assertEquals("y", config.get(null));

        config = TopicSearchCriteria.parseConfigString(" x    :  y    ");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(1, config.size());
        Assertions.assertTrue(config.containsKey("x"));
        Assertions.assertEquals("y", config.get("x"));

        config = TopicSearchCriteria.parseConfigString(" x    :  y    ;     a:b;   c:   ");
        Assertions.assertNotNull(config);
        Assertions.assertEquals(3, config.size());
        Assertions.assertTrue(config.containsKey("x"));
        Assertions.assertEquals("y", config.get("x"));
        Assertions.assertTrue(config.containsKey("a"));
        Assertions.assertEquals("b", config.get("a"));
        Assertions.assertTrue(config.containsKey("c"));
        Assertions.assertEquals(null, config.get("c"));
    }

}
