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

import java.util.Collections;
import java.util.HashMap;
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
public class PermissionMetadataUpdateParams {

    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;
    private final String principal;
    private final KafkaPrincipal principalObject;
    private final String description;
    private final Map<String, Object> attributes;

    @JsonCreator
    public PermissionMetadataUpdateParams(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType,
            @JsonProperty("principal") String principal,
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        this(
                resourceType,
                resourceName,
                patternType,
                principal,
                SecurityUtils.parseKafkaPrincipal(principal),
                description,
                attributes);
    }

    public PermissionMetadataUpdateParams(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal principal,
            String description,
            Map<String, Object> attributes) {
        this(
                resourceType,
                resourceName,
                patternType,
                principal != null ? principal.toString() : null,
                principal,
                description,
                attributes);
    }

    private PermissionMetadataUpdateParams(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            String principal,
            KafkaPrincipal principalObject,
            String description,
            Map<String, Object> attributes) {
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
        PermissionMetadataUpdateParams that = (PermissionMetadataUpdateParams) obj;
        return
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.patternType, that.patternType) &&
                Objects.equals(this.principal, that.principal) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceType,
                resourceName,
                patternType,
                principal,
                description,
                attributes);
    }

    @Override
    public String toString() {
        return
                "{resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", patternType: " + patternType +
                ", principal: " + principal +
                ", description: " + description +
                ", attributes: " + attributes +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder((PermissionMetadataUpdateParams)null);
    }

    public static Builder builder(PermissionMetadataUpdateParams origin) {
        return new Builder(origin);
    }

    public static Builder builder(Metadata origin) {
        return new Builder(origin);
    }

    public static PermissionMetadataUpdateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, PermissionMetadataUpdateParams.class);
    }

    public static PermissionMetadataUpdateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, PermissionMetadataUpdateParams.class);
    }

    public static class Builder {

        private ResourceType resourceType;
        private String resourceName;
        private PatternType patternType;
        private String principal;
        private KafkaPrincipal principalObject;
        private String description;
        private Map<String, Object> attributes = new HashMap<>();

        private Builder(PermissionMetadataUpdateParams origin) {
            if (origin == null) {
                return;
            }

            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.patternType = origin.patternType;
            this.principal = origin.principal;
            this.description = origin.description;
            this.attributes.putAll(origin.attributes);
        }

        private Builder(Metadata origin) {
            if (origin == null) {
                return;
            }

            this.description = origin.getDescription();
            this.attributes.putAll(origin.getAttributes());
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

        public Builder removeAttribute(String key) {
            this.attributes.remove(key);
            return this;
        }

        public PermissionMetadataUpdateParams build() {
            return new PermissionMetadataUpdateParams(
                    resourceType,
                    resourceName,
                    patternType,
                    principal,
                    principalObject,
                    description,
                    attributes);
        }

    }

}
