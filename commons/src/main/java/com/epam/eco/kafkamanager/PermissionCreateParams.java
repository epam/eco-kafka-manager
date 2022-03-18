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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
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
    private final PatternType patternType;
    private final String principal;
    private final KafkaPrincipal principalObject;
    private final AclPermissionType permissionType;
    private final AclOperation operation;
    private final String host;

    @JsonCreator
    public PermissionCreateParams(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType,
            @JsonProperty("principal") String principal,
            @JsonProperty("permissionType") AclPermissionType permissionType,
            @JsonProperty("operation") AclOperation operation,
            @JsonProperty("host") String host) {
        this(
                resourceType,
                resourceName,
                patternType,
                principal,
                SecurityUtils.parseKafkaPrincipal(principal),
                permissionType,
                operation,
                host);
    }

    public PermissionCreateParams(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal principal,
            AclPermissionType permissionType,
            AclOperation operation,
            String host) {
        this(
                resourceType,
                resourceName,
                patternType,
                principal != null ? principal.toString() : null,
                principal,
                permissionType,
                operation,
                host);
    }

    private PermissionCreateParams(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            String principal,
            KafkaPrincipal principalObject,
            AclPermissionType permissionType,
            AclOperation operation,
            String host) {
        Validate.notNull(resourceType, "Resource Type is null");
        Validate.notBlank(resourceName, "Resource Name is blank");
        Validate.notNull(patternType, "Pattern Type is null");
        Validate.notBlank(principal,  "Principal is blank");
        Validate.notNull(principalObject, "Principal Object is null");
        Validate.notNull(permissionType, "Permission Type is null");
        Validate.notNull(operation, "Operation is null");
        Validate.notBlank(host, "Host is null");

        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType;
        this.principal = principal;
        this.principalObject = principalObject;
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
                Objects.equals(this.patternType, that.patternType) &&
                Objects.equals(this.principal, that.principal) &&
                Objects.equals(this.permissionType, that.permissionType) &&
                Objects.equals(this.operation, that.operation) &&
                Objects.equals(this.host, that.host);
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
        return
                "{resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", patternType: " + patternType +
                ", principal: " + principal +
                ", permissionType: " + permissionType +
                ", operation: " + operation +
                ", host: " + host +
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
        private PatternType patternType;
        private String principal;
        private KafkaPrincipal principalObject;
        private AclPermissionType permissionType;
        private AclOperation operation;
        private String host;

        private Builder(PermissionCreateParams origin) {
            if (origin == null) {
                return;
            }

            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.patternType = origin.patternType;
            this.principal = origin.principal;
            this.permissionType = origin.permissionType;
            this.operation = origin.operation;
            this.host = origin.host;
        }

        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public Builder patternType(PatternType patternType) {
            this.patternType = patternType;
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

        public PermissionCreateParams build() {
            return new PermissionCreateParams(
                    resourceType,
                    resourceName,
                    patternType,
                    principal,
                    principalObject,
                    permissionType,
                    operation,
                    host);
        }

    }

}
