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
import java.util.Set;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

import static java.util.Objects.isNull;

/**
 * @author Andrei_Tytsik
 */
public class PrincipalPermissionsDeleteParams {

    private final String principal;
    private final KafkaPrincipal principalObject;
    private final Set<ResourceExcludes> excludes;
    private final ResourceType resourceType;

    @JsonCreator
    public PrincipalPermissionsDeleteParams(
            @JsonProperty("principal") String principal) {
        this(
                principal,
                SecurityUtils.parseKafkaPrincipal(principal),
                null,
                null);
    }

    public PrincipalPermissionsDeleteParams(KafkaPrincipal principal) {
        this(principal != null ? principal.toString() : null, principal, null, null);
    }

    public PrincipalPermissionsDeleteParams(KafkaPrincipal principal,
                                            ResourceType resourceType,
                                            Set<ResourceExcludes> excludeds) {
        this(principal != null ? principal.toString() : null, principal, resourceType,  excludeds);
    }

    private PrincipalPermissionsDeleteParams(
            String principal,
            KafkaPrincipal principalObject,
            ResourceType resourceType,
            Set<ResourceExcludes> excludes) {
        Validate.notBlank(principal, "Principal is blank");
        Validate.notNull(principalObject, "Principal Object is null");

        this.principal = principal;
        this.principalObject = principalObject;
        this.resourceType = resourceType;
        this.excludes = excludes;
    }

    public String getPrincipal() {
        return principal;
    }
    @JsonIgnore
    public KafkaPrincipal getPrincipalObject() {
        return principalObject;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }

    public Set<ResourceExcludes> getExcludes() {
        return excludes;
    }

    public boolean containsInExcludes(String resourceName, ResourceType resourceType) {
        if(isNull(this.excludes)) {
            return false;
        }
        return this.excludes.contains(new ResourceExcludes(resourceName,resourceType));
    }

    public boolean matchResourceType(ResourceType type) {
        if(isNull(this.resourceType)) {
            return true;
        }
        return type==this.resourceType;

    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        PrincipalPermissionsDeleteParams that = (PrincipalPermissionsDeleteParams) obj;
        return
                Objects.equals(principal, that.principal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal);
    }

    @Override
    public String toString() {
        return
                "{principal: " + principal + "}";
    }

    public record ResourceExcludes(String resourceName, ResourceType resourceType) {}

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(PrincipalPermissionsDeleteParams origin) {
        return new Builder(origin);
    }

    public static PrincipalPermissionsDeleteParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, PrincipalPermissionsDeleteParams.class);
    }

    public static PrincipalPermissionsDeleteParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, PrincipalPermissionsDeleteParams.class);
    }

    public static class Builder {

        private String principal;
        private KafkaPrincipal principalObject;
        private ResourceType resourceType;
        private Set<ResourceExcludes> excludes;

        private Builder(PrincipalPermissionsDeleteParams origin) {
            if (origin == null) {
                return;
            }
            this.principal = origin.principal;
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

        public Builder excludes(Set<ResourceExcludes> excludes) {
            this.excludes = excludes;
            return this;
        }

        public Builder resourceType(ResourceType resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        public PrincipalPermissionsDeleteParams build() {
            return new PrincipalPermissionsDeleteParams(
                    principal,
                    principalObject,
                    resourceType,
                    excludes);
        }
    }

}
