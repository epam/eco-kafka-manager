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

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.kafkamanager.utils.TestObjectMapperSingleton;

/**
 * @author Andrei_Tytsik
 */
public class PermissionCreateParamsTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        PermissionCreateParams origin = PermissionCreateParams.builder().
                resourceType(ResourceType.GROUP).
                resourceName("group").
                principal("user:user").
                permissionType(AclPermissionType.ALLOW).
                operation(AclOperation.CREATE).
                host("host").
                description("description").
                appendAttribute("attr1", "value1").
                appendAttribute("attr2", "value2").
                appendAttribute("attr3", "value3").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        PermissionCreateParams deserialized = mapper.readValue(
                json,
                PermissionCreateParams.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("resourceType", "GROUP");
        json.put("resourceName", "group1");
        json.put("principal", "user:user1");
        json.put("permissionType", AclPermissionType.ALLOW);
        json.put("operation", AclOperation.READ);
        json.put("host", "host");
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        json.put("attributes", attributes);
        json.put("description", "description");

        PermissionCreateParams query = PermissionCreateParams.fromJson(json);
        Assert.assertNotNull(query);
        Assert.assertEquals(ResourceType.GROUP, query.getResourceType());
        Assert.assertEquals("group1", query.getResourceName());
        Assert.assertEquals("user:user1", query.getPrincipal());
        Assert.assertEquals(AclPermissionType.ALLOW, query.getPermissionType());
        Assert.assertEquals(AclOperation.READ, query.getOperation());
        Assert.assertEquals("host", query.getHost());
        Assert.assertEquals(attributes, query.getAttributes());
        Assert.assertEquals("description", query.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"resourceType\": \"GROUP\"" +
                ", \"resourceName\": \"group1\"" +
                ", \"principal\": \"user:user1\"" +
                ", \"permissionType\": \"ALLOW\"" +
                ", \"operation\": \"READ\"" +
                ", \"host\": \"host\"" +
                ", \"attributes\": {\"attr1\":\"value1\",\"attr2\":\"value2\",\"attr3\":\"value3\"}" +
                ", \"description\": \"description\"" +
                "}";

        PermissionCreateParams query = PermissionCreateParams.fromJson(json);
        Assert.assertNotNull(query);
        Assert.assertEquals(ResourceType.GROUP, query.getResourceType());
        Assert.assertEquals("group1", query.getResourceName());
        Assert.assertEquals("user:user1", query.getPrincipal());
        Assert.assertEquals(AclPermissionType.ALLOW, query.getPermissionType());
        Assert.assertEquals(AclOperation.READ, query.getOperation());
        Assert.assertEquals("host", query.getHost());
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("attr1", "value1");
        attributes.put("attr2", "value2");
        attributes.put("attr3", "value3");
        Assert.assertEquals(attributes, query.getAttributes());
        Assert.assertEquals("description", query.getDescription());
    }

}
