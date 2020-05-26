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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class PermissionSearchCriteria implements SearchCriteria<PermissionInfo> {

    private final String kafkaPrincipal;
    private final ResourceType resourceType;
    private final String resourceName;
    private final AclPermissionType permissionType;
    private final AclOperation operation;
    private final String host;
    private final String description;

    public PermissionSearchCriteria(
            @JsonProperty("kafkaPrincipal") String kafkaPrincipal,
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("permissionType") AclPermissionType permissionType,
            @JsonProperty("operation") AclOperation operation,
            @JsonProperty("host") String host,
            @JsonProperty("description") String description) {
        this.kafkaPrincipal = kafkaPrincipal;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.permissionType = permissionType;
        this.operation = operation;
        this.host = host;
        this.description = description;
    }

    public String getKafkaPrincipal() {
        return kafkaPrincipal;
    }
    public ResourceType getResourceType() {
        return resourceType;
    }
    public String getResourceName() {
        return resourceName;
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

    @Override
    public boolean matches(PermissionInfo obj) {
        Validate.notNull(obj, "Permission Info is null");

        return
                (
                        StringUtils.isBlank(kafkaPrincipal) ||
                        StringUtils.containsIgnoreCase(obj.getKafkaPrincipal().toString(), kafkaPrincipal)) &&
                (resourceType == null || Objects.equals(obj.getResourceType(), resourceType)) &&
                (
                        StringUtils.isBlank(resourceName) ||
                        StringUtils.containsIgnoreCase(obj.getResourceName(), resourceName)) &&
                (permissionType == null || Objects.equals(obj.getPermissionType(), permissionType)) &&
                (operation == null || Objects.equals(obj.getOperation(), operation)) &&
                (StringUtils.isBlank(host) || StringUtils.containsIgnoreCase(obj.getHost(), host)) &&
                (StringUtils.isBlank(description) || StringUtils.containsIgnoreCase(
                        obj.getMetadata().map(Metadata::getDescription).orElse(null),
                        description));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PermissionSearchCriteria that = (PermissionSearchCriteria) obj;
        return
                Objects.equals(this.kafkaPrincipal, that.kafkaPrincipal) &&
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.permissionType, that.permissionType) &&
                Objects.equals(this.operation, that.operation) &&
                Objects.equals(this.host, that.host) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                kafkaPrincipal,
                resourceType,
                resourceName,
                permissionType,
                operation,
                host,
                description);
    }

    @Override
    public String toString() {
        return
                "{kafkaPrincipal: " + kafkaPrincipal +
                ", resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", permissionType: " + permissionType +
                ", operation: " + operation +
                ", host: " + host +
                ", description: " + description +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(PermissionSearchCriteria origin) {
        return new Builder(origin);
    }

    public static PermissionSearchCriteria fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, PermissionSearchCriteria.class);
    }

    public static PermissionSearchCriteria fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, PermissionSearchCriteria.class);
    }

    public static class Builder {

        private String kafkaPrincipal;
        private ResourceType resourceType;
        private String resourceName;
        private AclPermissionType permissionType;
        private AclOperation operation;
        private String host;
        private String description;

        private Builder(PermissionSearchCriteria origin) {
            if (origin == null) {
                return;
            }

            this.kafkaPrincipal = origin.kafkaPrincipal;
            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.permissionType = origin.permissionType;
            this.operation = origin.operation;
            this.host = origin.host;
            this.description = origin.description;
        }

        public Builder kafkaPrincipal(String kafkaPrincipal) {
            this.kafkaPrincipal = kafkaPrincipal;
            return this;
        }

        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public Builder permissionType(AclPermissionType permissionType) {
            this.permissionType = permissionType;
            return this;
        }

        public Builder operation(AclOperation operation) {
            this.operation = operation;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public PermissionSearchCriteria build() {
            return new PermissionSearchCriteria(
                    kafkaPrincipal,
                    resourceType,
                    resourceName,
                    permissionType,
                    operation,
                    host,
                    description);
        }

    }

}
