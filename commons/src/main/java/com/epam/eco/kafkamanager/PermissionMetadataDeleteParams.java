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
public class PermissionMetadataDeleteParams {

    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;
    private final String principal;
    private final KafkaPrincipal principalObject;

    @JsonCreator
    public PermissionMetadataDeleteParams(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType,
            @JsonProperty("principal") String principal) {
        this(
                resourceType,
                resourceName,
                patternType,
                principal,
                SecurityUtils.parseKafkaPrincipal(principal));
    }

    public PermissionMetadataDeleteParams(
            ResourceType resourceType,
            PatternType patternType,
            String resourceName,
            KafkaPrincipal principal) {
        this(
                resourceType,
                resourceName,
                patternType,
                principal != null ? principal.toString() : null,
                principal);
    }

    private PermissionMetadataDeleteParams(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            String principal,
            KafkaPrincipal principalObject) {
        Validate.notNull(resourceType, "Resource type can't be null");
        Validate.notBlank(resourceName, "Resource name can't be blank");
        Validate.notNull(patternType, "Pattern Type is null");
        Validate.notBlank(principal, "Principal can't be null");
        Validate.notNull(principalObject, "Principal object can't be null");

        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType;
        this.principal = principal;
        this.principalObject = principalObject;
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        PermissionMetadataDeleteParams that = (PermissionMetadataDeleteParams) obj;
        return
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.patternType, that.patternType) &&
                Objects.equals(this.principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceType,
                resourceName,
                patternType,
                principal);
    }

    @Override
    public String toString() {
        return
                "{resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", patternType: " + patternType +
                ", principal: " + principal +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(PermissionMetadataDeleteParams origin) {
        return new Builder(origin);
    }

    public static PermissionMetadataDeleteParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, PermissionMetadataDeleteParams.class);
    }

    public static PermissionMetadataDeleteParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, PermissionMetadataDeleteParams.class);
    }

    public static class Builder {

        private ResourceType resourceType;
        private String resourceName;
        private PatternType patternType;
        private String principal;
        private KafkaPrincipal principalObject;

        private Builder(PermissionMetadataDeleteParams origin) {
            if (origin == null) {
                return;
            }

            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.patternType = origin.patternType;
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

        public PermissionMetadataDeleteParams build() {
            return new PermissionMetadataDeleteParams(
                    resourceType,
                    resourceName,
                    patternType,
                    principal,
                    principalObject);
        }

    }

}
