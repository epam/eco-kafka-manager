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
public class BrokerSearchCriteriaTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        BrokerSearchCriteria origin = BrokerSearchCriteria.builder().
                brokerId(42).
                rack("default").
                description("description").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        BrokerSearchCriteria deserialized = mapper.readValue(
                json,
                BrokerSearchCriteria.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("brokerId", "42");
        json.put("rack", "default");
        json.put("description", "description");

        BrokerSearchCriteria query = BrokerSearchCriteria.fromJson(json);
        Assert.assertNotNull(query);
        Assert.assertEquals(Integer.valueOf(42), query.getBrokerId());
        Assert.assertEquals("default", query.getRack());
        Assert.assertEquals("description", query.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"brokerId\": 42" +
                ", \"rack\": \"default\"" +
                ", \"description\": \"description\"" +
                "}";

        BrokerSearchCriteria query = BrokerSearchCriteria.fromJson(json);
        Assert.assertNotNull(query);
        Assert.assertEquals(Integer.valueOf(42), query.getBrokerId());
        Assert.assertEquals("default", query.getRack());
        Assert.assertEquals("description", query.getDescription());
    }

}
