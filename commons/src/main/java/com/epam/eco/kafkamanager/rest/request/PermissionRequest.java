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

import java.util.Objects;

import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raman_Babich
 */
public class PermissionRequest {

    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;
    private final String principal;
    private final AclPermissionType permissionType;
    private final AclOperation operation;
    private final String host;

    public PermissionRequest(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType,
            @JsonProperty("principal") String principal,
            @JsonProperty("permissionType") AclPermissionType permissionType,
            @JsonProperty("operation") AclOperation operation,
            @JsonProperty("host") String host) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType;
        this.principal = principal;
        this.permissionType = permissionType;
        this.operation = operation;
        this.host = host;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
    public String getResourceName() {
        return resourceName;
    }
    public PatternType getPatternType() {
        return patternType;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PermissionRequest that = (PermissionRequest) o;
        return
                resourceType == that.resourceType &&
                Objects.equals(resourceName, that.resourceName) &&
                patternType == that.patternType &&
                Objects.equals(principal, that.principal) &&
                permissionType == that.permissionType &&
                operation == that.operation &&
                Objects.equals(host, that.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceType,
                resourceName,
                patternType,
                principal,
                permissionType,
                operation,
                host);
    }

    @Override
    public String toString() {
        return "PermissionRequest{" +
                "resourceType=" + resourceType +
                ", resourceName='" + resourceName + '\'' +
                ", patternType=" + patternType +
                ", principal='" + principal + '\'' +
                ", permissionType=" + permissionType +
                ", operation=" + operation +
                ", host='" + host +
                '}';
    }

}
