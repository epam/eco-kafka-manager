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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.udmetrics.utils.TestObjectMapperSingleton;


/**
 * @author Andrei_Tytsik
 */
public class UDMetricConfigSearchCriteriaTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        UDMetricConfigSearchCriteria origin = UDMetricConfigSearchCriteria.builder().
                type(UDMetricType.TOPIC_OFFSET_INCREASE).
                resourceName("resource1").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        UDMetricConfigSearchCriteria deserialized = mapper.readValue(
                json,
                UDMetricConfigSearchCriteria.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("type", "CONSUMER_GROUP_LAG");
        json.put("resourceName", "resource3");

        UDMetricConfigSearchCriteria criteria = UDMetricConfigSearchCriteria.fromJson(json);
        Assert.assertNotNull(criteria);
        Assert.assertEquals(UDMetricType.CONSUMER_GROUP_LAG, criteria.getType());
        Assert.assertEquals("resource3", criteria.getResourceName());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"type\": \"CONSUMER_GROUP_LAG\"" +
                ", \"resourceName\": \"resource3\"" +
                "}";

        UDMetricConfigSearchCriteria criteria = UDMetricConfigSearchCriteria.fromJson(json);
        Assert.assertNotNull(criteria);
        Assert.assertEquals(UDMetricType.CONSUMER_GROUP_LAG, criteria.getType());
        Assert.assertEquals("resource3", criteria.getResourceName());
    }

}
