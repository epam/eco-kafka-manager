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
public class PermissionSearchCriteriaTest {

    @Test
    public void testSerializedToJsonAndBack() throws Exception {
        PermissionSearchCriteria origin = PermissionSearchCriteria.builder().
                kafkaPrincipal("user1").
                resourceType(ResourceType.GROUP).
                resourceName("gorup1").
                permissionType(AclPermissionType.ALLOW).
                operation(AclOperation.READ).
                host("host").
                description("description").
                build();

        ObjectMapper mapper = TestObjectMapperSingleton.getObjectMapper();

        String json = mapper.writeValueAsString(origin);
        Assert.assertNotNull(json);

        PermissionSearchCriteria deserialized = mapper.readValue(
                json,
                PermissionSearchCriteria.class);
        Assert.assertNotNull(deserialized);
        Assert.assertEquals(origin, deserialized);
    }

    @Test
    public void testDeserializedFromJson1() throws Exception {
        Map<String, Object> json = new HashMap<>();
        json.put("kafkaPrincipal", "user1");
        json.put("resourceType", "GROUP");
        json.put("resourceName", "group1");
        json.put("permissionType", "ALLOW");
        json.put("operation", "READ");
        json.put("host", "host");
        json.put("description", "description");

        PermissionSearchCriteria criteria = PermissionSearchCriteria.fromJson(json);
        Assert.assertNotNull(criteria);
        Assert.assertEquals("user1", criteria.getKafkaPrincipal());
        Assert.assertEquals(ResourceType.GROUP, criteria.getResourceType());
        Assert.assertEquals("group1", criteria.getResourceName());
        Assert.assertEquals(AclPermissionType.ALLOW, criteria.getPermissionType());
        Assert.assertEquals(AclOperation.READ, criteria.getOperation());
        Assert.assertEquals("host", criteria.getHost());
        Assert.assertEquals("description", criteria.getDescription());
    }

    @Test
    public void testDeserializedFromJson2() throws Exception {
        String json =
                "{" +
                "\"kafkaPrincipal\": \"user1\"" +
                ", \"resourceType\": \"GROUP\"" +
                ", \"resourceName\": \"group1\"" +
                ", \"permissionType\": \"ALLOW\"" +
                ", \"operation\": \"READ\"" +
                ", \"host\": \"host\"" +
                ", \"description\": \"description\"" +
                "}";

        PermissionSearchCriteria criteria = PermissionSearchCriteria.fromJson(json);
        Assert.assertNotNull(criteria);
        Assert.assertEquals("user1", criteria.getKafkaPrincipal());
        Assert.assertEquals(ResourceType.GROUP, criteria.getResourceType());
        Assert.assertEquals("group1", criteria.getResourceName());
        Assert.assertEquals(AclPermissionType.ALLOW, criteria.getPermissionType());
        Assert.assertEquals(AclOperation.READ, criteria.getOperation());
        Assert.assertEquals("host", criteria.getHost());
        Assert.assertEquals("description", criteria.getDescription());
    }

}
