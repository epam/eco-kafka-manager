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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class PermissionCreateParams {

    private final ResourceType resourceType;
    private final String resourceName;
    private final String principal;
    private final KafkaPrincipal principalObject;
    private final AclPermissionType permissionType;
    private final AclOperation operation;
    private final String host;
    private final String description;
    private final Map<String, Object> attributes;

    @JsonCreator
    public PermissionCreateParams(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("principal") String principal,
            @JsonProperty("permissionType") AclPermissionType permissionType,
            @JsonProperty("operation") AclOperation operation,
            @JsonProperty("host") String host,
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        this(
                resourceType,
                resourceName,
                principal,
                SecurityUtils.parseKafkaPrincipal(principal),
                permissionType,
                operation,
                host,
                description,
                attributes);
    }

    public PermissionCreateParams(
            ResourceType resourceType,
            String resourceName,
            KafkaPrincipal principal,
            AclPermissionType permissionType,
            AclOperation operation,
            String host,
            String description,
            Map<String, Object> attributes) {
        this(
                resourceType,
                resourceName,
                principal != null ? principal.toString() : null,
                principal,
                permissionType,
                operation,
                host,
                description,
                attributes);
    }

    private PermissionCreateParams(
            ResourceType resourceType,
            String resourceName,
            String principal,
            KafkaPrincipal principalObject,
            AclPermissionType permissionType,
            AclOperation operation,
            String host,
            String description,
            Map<String, Object> attributes) {
        Validate.notNull(resourceType, "Resource Type is null");
        Validate.notBlank(resourceName, "Resource Name is blank");
        Validate.notBlank(principal,  "Principal is blank");
        Validate.notNull(principalObject, "Principal Object is null");
        Validate.notNull(permissionType, "Permission Type is null");
        Validate.notNull(operation, "Operation is null");
        Validate.notBlank(host, "Host is null");

        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.principal = principal;
        this.principalObject = principalObject;
        this.permissionType = permissionType;
        this.operation = operation;
        this.host = host;
        this.description = description;
        this.attributes =
                attributes != null ?
                Collections.unmodifiableMap(new HashMap<>(attributes)) :
                Collections.emptyMap();
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
    @JsonIgnore
    public KafkaPrincipal getPrincipalObject() {
        return principalObject;
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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PermissionCreateParams that = (PermissionCreateParams) obj;
        return
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.principal, that.principal) &&
                Objects.equals(this.permissionType, that.permissionType) &&
                Objects.equals(this.operation, that.operation) &&
                Objects.equals(this.host, that.host) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceType,
                resourceName,
                principal,
                permissionType,
                operation,
                host,
                description,
                attributes);
    }

    @Override
    public String toString() {
        return
                "{resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", principal: " + principal +
                ", permissionType: " + permissionType +
                ", operation: " + operation +
                ", host: " + host +
                ", description: " + description +
                ", attributes: " + attributes +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(PermissionCreateParams origin) {
        return new Builder(origin);
    }

    public static PermissionCreateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, PermissionCreateParams.class);
    }

    public static PermissionCreateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, PermissionCreateParams.class);
    }

    public static class Builder {

        private ResourceType resourceType;
        private String resourceName;
        private String principal;
        private KafkaPrincipal principalObject;
        private AclPermissionType permissionType;
        private AclOperation operation;
        private String host;
        private String description;
        private Map<String, Object> attributes = new HashMap<>();

        private Builder(PermissionCreateParams origin) {
            if (origin == null) {
                return;
            }

            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.principal = origin.principal;
            this.permissionType = origin.permissionType;
            this.operation = origin.operation;
            this.host = origin.host;
            this.description = origin.description;
            this.attributes.putAll(origin.attributes);
        }

        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public Builder principal(String principal) {
            this.principal = principal;
            this.principalObject = principal != null ? SecurityUtils.parseKafkaPrincipal(principal) : null;
            return this;
        }

        public Builder principal(KafkaPrincipal principal) {
            this.principalObject = principal;
            this.principal = principal != null ? principal.toString() : null;
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

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.clear();
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public PermissionCreateParams build() {
            return new PermissionCreateParams(
                    resourceType,
                    resourceName,
                    principal,
                    principalObject,
                    permissionType,
                    operation,
                    host,
                    description,
                    attributes);
        }

    }

}
