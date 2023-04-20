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

import org.apache.kafka.common.ConsumerGroupState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupSearchCriteriaTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        ConsumerGroupSearchCriteria origin = ConsumerGroupSearchCriteria.builder().
                groupName("group1").
                state(ConsumerGroupState.STABLE).
                storageType(StorageType.KAFKA).
                description("description").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assertions.assertNotNull(json);

        ConsumerGroupSearchCriteria deserialized = mapper.readValue(
                json,
                ConsumerGroupSearchCriteria.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("groupName", "group1");
        json.put("state", "EMPTY");
        json.put("storageType", "KAFKA");
        json.put("description", "description");

        ConsumerGroupSearchCriteria criteria = ConsumerGroupSearchCriteria.fromJson(json);
        Assertions.assertNotNull(criteria);
        Assertions.assertEquals("group1", criteria.getGroupName());
        Assertions.assertEquals(ConsumerGroupState.EMPTY, criteria.getState());
        Assertions.assertEquals(StorageType.KAFKA, criteria.getStorageType());
        Assertions.assertEquals("description", criteria.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"groupName\": \"group1\"" +
                ", \"state\": \"COMPLETING_REBALANCE\"" +
                ", \"storageType\": \"KAFKA\"" +
                ", \"description\": \"description\"" +
                "}";

        ConsumerGroupSearchCriteria criteria = ConsumerGroupSearchCriteria.fromJson(json);
        Assertions.assertNotNull(criteria);
        Assertions.assertEquals("group1", criteria.getGroupName());
        Assertions.assertEquals(ConsumerGroupState.COMPLETING_REBALANCE, criteria.getState());
        Assertions.assertEquals(StorageType.KAFKA, criteria.getStorageType());
        Assertions.assertEquals("description", criteria.getDescription());
    }

}
