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

import java.util.Objects;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class PermissionMetadataKey extends MetadataKey {

    private final String principal;
    private final KafkaPrincipal principalObject;
    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;

    @JsonCreator
    public PermissionMetadataKey(
            @JsonProperty("principal") String principal,
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType) {
        this(
                principal,
                principal != null ? SecurityUtils.parseKafkaPrincipal(principal) : null,
                resourceType,
                resourceName,
                patternType);
    }

    public PermissionMetadataKey(
            KafkaPrincipal principal,
            ResourceType resourceType,
            String resourceName,
            PatternType patternType) {
        this(
                principal != null ? principal.toString() : null,
                principal,
                resourceType,
                resourceName,
                patternType);
    }

    private PermissionMetadataKey(
            String principal,
            KafkaPrincipal principalObject,
            ResourceType resourceType,
            String resourceName,
            PatternType patternType) {
        super(EntityType.PERMISSION);

        Validate.notBlank(principal, "Principal is blank");
        Validate.notNull(principalObject, "Principal Object is null");
        Validate.notNull(resourceType, "Resource Type is null");
        Validate.notBlank(resourceName, "Resource Name is blank");
        // TODO compatibility Validate.notNull(patternType, "Pattern Type is null");

        this.principal = principal;
        this.principalObject = principalObject;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType != null ? patternType : PatternType.LITERAL; // TODO compatibility
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
    public String getResourceName() {
        return resourceName;
    }
    public PatternType getPatternType() {
        return patternType;
    }

    @Override
    public Object getEntityId() {
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                super.hashCode(),
                principal,
                resourceType,
                resourceName,
                patternType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        PermissionMetadataKey that = (PermissionMetadataKey)obj;
        return
                Objects.equals(this.principal, that.principal) &&
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.patternType, that.patternType);
    }

    @Override
    public String toString() {
        return
                "{entityType: " + entityType +
                ", principal: " + principal +
                ", resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", patternType: " + patternType +
                "}";
    }

    public static final PermissionMetadataKey with(
            KafkaPrincipal principal,
            ResourceType resourceType,
            String resourceName,
            PatternType patternType) {
        return new PermissionMetadataKey(principal, resourceType, resourceName, patternType);
    }

    public static final PermissionMetadataKey with(
            String principal,
            ResourceType resourceType,
            String resourceName,
            PatternType patternType) {
        return new PermissionMetadataKey(principal, resourceType, resourceName, patternType);
    }

}
