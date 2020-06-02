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
package com.epam.eco.kafkamanager.rest.request;

import java.util.Map;
import java.util.Objects;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raman_Babich
 */
public class PermissionRequest {

    private final ResourceType resourceType;
    private final String resourceName;
    private final String principal;
    private final AclPermissionType permissionType;
    private final AclOperation operation;
    private final String host;
    private final String description;
    private final Map<String, Object> attributes;

    public PermissionRequest(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("principal") String principal,
            @JsonProperty("permissionType") AclPermissionType permissionType,
            @JsonProperty("operation") AclOperation operation,
            @JsonProperty("host") String host,
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.principal = principal;
        this.permissionType = permissionType;
        this.operation = operation;
        this.host = host;
        this.description = description;
        this.attributes = attributes;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getPrincipal() {
        return principal;
    }

    public AclPermissionType getPermissionType() {
        return permissionType;
    }

    public AclOperation getOperation() {
        return operation;
    }

    public String getHost() {
        return host;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionRequest that = (PermissionRequest) o;
        return resourceType == that.resourceType &&
                Objects.equals(resourceName, that.resourceName) &&
                Objects.equals(principal, that.principal) &&
                permissionType == that.permissionType &&
                operation == that.operation &&
                Objects.equals(host, that.host) &&
                Objects.equals(description, that.description) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, resourceName, principal, permissionType, operation, host, description, attributes);
    }

    @Override
    public String toString() {
        return "PermissionRequest{" +
                "resourceType=" + resourceType +
                ", resourceName='" + resourceName + '\'' +
                ", principal='" + principal + '\'' +
                ", permissionType=" + permissionType +
                ", operation=" + operation +
                ", host='" + host + '\'' +
                ", description='" + description + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
