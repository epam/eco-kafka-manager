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

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class ResourcePermissionFilterTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        ResourcePermissionFilter origin = ResourcePermissionFilter.builder().
                resourceType(ResourceType.GROUP).
                resourceName("group").
                patternType(PatternType.LITERAL).
                principalFilter("user:user").
                permissionTypeFilter(AclPermissionType.ALLOW).
                operationFilter(AclOperation.CREATE).
                hostFilter("host").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assertions.assertNotNull(json);

        ResourcePermissionFilter deserialized = mapper.readValue(
                json,
                ResourcePermissionFilter.class);
        Assertions.assertNotNull(deserialized);
        Assertions.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("resourceType", "GROUP");
        json.put("resourceName", "group1");
        json.put("patternType", "LITERAL");
        json.put("principalFilter", "user:user1");
        json.put("permissionTypeFilter", AclPermissionType.ALLOW);
        json.put("operationFilter", AclOperation.READ);
        json.put("hostFilter", "host");

        ResourcePermissionFilter params = ResourcePermissionFilter.fromJson(json);
        Assertions.assertNotNull(params);
        Assertions.assertEquals(ResourceType.GROUP, params.getResourceType());
        Assertions.assertEquals("group1", params.getResourceName());
        Assertions.assertEquals(PatternType.LITERAL, params.getPatternType());
        Assertions.assertEquals("user:user1", params.getPrincipalFilter());
        Assertions.assertEquals(AclPermissionType.ALLOW, params.getPermissionTypeFilter());
        Assertions.assertEquals(AclOperation.READ, params.getOperationFilter());
        Assertions.assertEquals("host", params.getHostFilter());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"resourceType\": \"GROUP\"" +
                ", \"resourceName\": \"group1\"" +
                ", \"patternType\": \"LITERAL\"" +
                ", \"principalFilter\": \"user:user1\"" +
                ", \"permissionTypeFilter\": \"ALLOW\"" +
                ", \"operationFilter\": \"READ\"" +
                ", \"hostFilter\": \"host\"" +
                "}";

        ResourcePermissionFilter params = ResourcePermissionFilter.fromJson(json);
        Assertions.assertNotNull(params);
        Assertions.assertEquals(ResourceType.GROUP, params.getResourceType());
        Assertions.assertEquals("group1", params.getResourceName());
        Assertions.assertEquals(PatternType.LITERAL, params.getPatternType());
        Assertions.assertEquals("user:user1", params.getPrincipalFilter());
        Assertions.assertEquals(AclPermissionType.ALLOW, params.getPermissionTypeFilter());
        Assertions.assertEquals(AclOperation.READ, params.getOperationFilter());
        Assertions.assertEquals("host", params.getHostFilter());
    }

}
