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
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
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
public class ResourcePermissionFilter {

    // resource pattern
    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;

    // permission filter. null == ANY
    private final String principalFilter;
    private final KafkaPrincipal principalObjectFilter;
    private final AclPermissionType permissionTypeFilter;
    private final AclOperation operationFilter;
    private final String hostFilter;

    @JsonCreator
    public ResourcePermissionFilter(
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType,
            @JsonProperty("principalFilter") String principalFilter,
            @JsonProperty("permissionTypeFilter") AclPermissionType permissionTypeFilter,
            @JsonProperty("operationFilter") AclOperation operationFilter,
            @JsonProperty("hostFilter") String hostFilter) {
        this(
                resourceType,
                resourceName,
                patternType,
                principalFilter,
                principalFilter != null ? SecurityUtils.parseKafkaPrincipal(principalFilter) : null,
                permissionTypeFilter,
                operationFilter,
                hostFilter);
    }

    public ResourcePermissionFilter(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal principalFilter,
            AclPermissionType permissionTypeFilter,
            AclOperation operationFilter,
            String hostFilter) {
        this(
                resourceType,
                resourceName,
                patternType,
                principalFilter != null ? principalFilter.toString() : null,
                principalFilter,
                permissionTypeFilter,
                operationFilter,
                hostFilter);
    }

    private ResourcePermissionFilter(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            String principalFilter,
            KafkaPrincipal principalObjectFilter,
            AclPermissionType permissionTypeFilter,
            AclOperation operationFilter,
            String hostFilter) {
        Validate.notNull(resourceType, "Resource Type can't be null");
        Validate.isTrue(resourceType != ResourceType.ANY, "Resource Type can't be %s", resourceType);
        Validate.notBlank(resourceName, "Resource Name can't be blank");
        Validate.notNull(patternType, "Pattern Type can't be null");
        Validate.isTrue(
                patternType != PatternType.ANY && patternType != PatternType.MATCH,
                "Pattern Type can't be %s", patternType);
        Validate.notNull(permissionTypeFilter, "Permission Type filter can't be null");
        Validate.notNull(operationFilter, "Operation filter can't be null");

        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType;
        this.principalFilter = principalFilter;
        this.principalObjectFilter = principalObjectFilter;
        this.permissionTypeFilter = permissionTypeFilter;
        this.operationFilter = operationFilter;
        this.hostFilter = hostFilter;
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
    public String getPrincipalFilter() {
        return principalFilter;
    }
    @JsonIgnore
    public KafkaPrincipal getPrincipalObjectFilter() {
        return principalObjectFilter;
    }
    public AclPermissionType getPermissionTypeFilter() {
        return permissionTypeFilter;
    }
    public AclOperation getOperationFilter() {
        return operationFilter;
    }
    public String getHostFilter() {
        return hostFilter;
    }

    public ResourcePermissionFilter toMatchAnyFilter() {
        return toBuilder().
                principalFilter((KafkaPrincipal)null).
                permissionTypeFilter(AclPermissionType.ANY).
                operationFilter(AclOperation.ANY).
                hostFilter(null).
                build();
    }

    public ResourcePattern toResourcePattern() {
        return new ResourcePattern(
                resourceType,
                resourceName,
                patternType);
    }

    public ResourcePatternFilter toResourcePatternFilter() {
        return new ResourcePatternFilter(
                resourceType,
                resourceName,
                patternType);
    }

    public AccessControlEntryFilter toAccessControlEntryFilter() {
        return new AccessControlEntryFilter(
                principalFilter,
                hostFilter,
                operationFilter,
                permissionTypeFilter);
    }

    public AclBindingFilter toAclBindingFilter() {
        return new AclBindingFilter(toResourcePatternFilter(), toAccessControlEntryFilter());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ResourcePermissionFilter that = (ResourcePermissionFilter) obj;
        return
                Objects.equals(resourceType, that.resourceType) &&
                Objects.equals(resourceName, that.resourceName) &&
                Objects.equals(patternType, that.patternType) &&
                Objects.equals(principalFilter, that.principalFilter) &&
                Objects.equals(permissionTypeFilter, that.permissionTypeFilter) &&
                Objects.equals(operationFilter, that.operationFilter) &&
                Objects.equals(hostFilter, that.hostFilter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                resourceType,
                resourceName,
                patternType,
                principalFilter,
                permissionTypeFilter,
                operationFilter,
                hostFilter);
    }

    @Override
    public String toString() {
        return
                "{resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", patternType: " + patternType +
                ", principalFilter: " + principalFilter +
                ", permissionTypeFilter: " + permissionTypeFilter +
                ", operationFilter: " + operationFilter +
                ", hostFilter: " + hostFilter +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(ResourcePermissionFilter origin) {
        return new Builder(origin);
    }

    public static ResourcePermissionFilter fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, ResourcePermissionFilter.class);
    }

    public static ResourcePermissionFilter fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, ResourcePermissionFilter.class);
    }

    public static class Builder {

        private static final AclPermissionType DEFAULT_PERMISSION_TYPE_FILTER = AclPermissionType.ANY;
        private static final AclOperation DEFAULT_OPERATION_FILTER = AclOperation.ANY;

        private ResourceType resourceType;
        private String resourceName;
        private PatternType patternType;
        private String principalFilter;
        private KafkaPrincipal principalObjectFilter;
        private AclPermissionType permissionTypeFilter = DEFAULT_PERMISSION_TYPE_FILTER;
        private AclOperation operationFilter = DEFAULT_OPERATION_FILTER;
        private String hostFilter;

        private Builder(ResourcePermissionFilter origin) {
            if (origin == null) {
                return;
            }

            this.resourceType = origin.resourceType;
            this.resourceName = origin.resourceName;
            this.patternType = origin.patternType;
            this.principalFilter = origin.principalFilter;
            this.permissionTypeFilter = origin.permissionTypeFilter;
            this.operationFilter = origin.operationFilter;
            this.hostFilter = origin.hostFilter;
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

        public Builder principalFilter(String principalFilter) {
            this.principalFilter = principalFilter;
            this.principalObjectFilter =
                    principalFilter != null ? SecurityUtils.parseKafkaPrincipal(principalFilter) : null;
            return this;
        }

        public Builder principalFilter(KafkaPrincipal principalFilter) {
            this.principalObjectFilter = principalFilter;
            this.principalFilter = principalFilter != null ? principalFilter.toString() : null;
            return this;
        }

        public Builder permissionTypeFilterOrElseDefault(AclPermissionType permissionTypeFilter) {
            return permissionTypeFilter(permissionTypeFilter != null ? permissionTypeFilter : DEFAULT_PERMISSION_TYPE_FILTER);
        }

        public Builder permissionTypeFilter(AclPermissionType permissionTypeFilter) {
            this.permissionTypeFilter = permissionTypeFilter;
            return this;
        }

        public Builder operationFilterOrElseDefault(AclOperation operationFilter) {
            return operationFilter(operationFilter != null ? operationFilter : DEFAULT_OPERATION_FILTER);
        }

        public Builder operationFilter(AclOperation operationFilter) {
            this.operationFilter = operationFilter;
            return this;
        }

        public Builder hostFilter(String hostFilter) {
            this.hostFilter = hostFilter;
            return this;
        }

        public ResourcePermissionFilter build() {
            return new ResourcePermissionFilter(
                    resourceType,
                    resourceName,
                    patternType,
                    principalFilter,
                    principalObjectFilter,
                    permissionTypeFilter,
                    operationFilter,
                    hostFilter);
        }
    }

}
