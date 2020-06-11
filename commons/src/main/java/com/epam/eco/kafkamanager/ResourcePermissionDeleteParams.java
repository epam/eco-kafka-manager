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

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Naira_Tamrazyan
 */
public class ResourcePermissionDeleteParams {

    private final ResourceType resourceType;
    private final String resourceName;
    private final String principal;
    private final KafkaPrincipal principalObject;

    @JsonCreator
    public ResourcePermissionDeleteParams(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("principal") String principal) {
        this(
                resourceType,
                resourceName,
                principal,
                SecurityUtils.parseKafkaPrincipal(principal));
    }

    public ResourcePermissionDeleteParams(
            ResourceType resourceType,
            String resourceName,
            KafkaPrincipal principal) {
        this(
                resourceType,
                resourceName,
                principal != null ? principal.toString() : null,
                principal);
    }

    private ResourcePermissionDeleteParams(
            ResourceType resourceType,
            String resourceName,
            String principal,
            KafkaPrincipal principalObject) {
        Validate.notNull(resourceType, "Resource Type is null");
        Validate.notBlank(resourceName, "Resource Name is blank");
        Validate.notBlank(principal, "Principal is blank");
        Validate.notNull(principalObject, "Principal Object is null");

        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.principal = principal;
        this.principalObject = principalObject;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourcePermissionDeleteParams that = (ResourcePermissionDeleteParams) o;
        return resourceType == that.resourceType &&
                Objects.equals(resourceName, that.resourceName) &&
                Objects.equals(principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceType,
                resourceName,
                principal);
    }

    @Override
    public String toString() {
        return
                "{resourceType: " + resourceType +
                        ", resourceName: " + resourceName +
                        ", principal: " + principal +
                        "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(ResourcePermissionDeleteParams origin) {
        return new Builder(origin);
    }

    public static PermissionDeleteParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, PermissionDeleteParams.class);
    }

    public static PermissionDeleteParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, PermissionDeleteParams.class);
    }

    public static class Builder {

        private ResourceType resourceType;
        private String resourceName;
        private String principal;
        private KafkaPrincipal principalObject;

        private Builder(ResourcePermissionDeleteParams origin) {
            if (origin == null) {
                return;
            }

            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.principal = origin.principal;
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

        public ResourcePermissionDeleteParams build() {
            return new ResourcePermissionDeleteParams(
                    resourceType,
                    resourceName,
                    principal,
                    principalObject);
        }
    }

}
