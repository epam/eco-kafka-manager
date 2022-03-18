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
import java.util.Optional;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class PermissionInfo implements MetadataAware, Comparable<PermissionInfo> {

    private final KafkaPrincipal kafkaPrincipal;
    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;
    private final AclPermissionType permissionType;
    private final AclOperation operation;
    private final String host;
    private final Metadata metadata;

    public PermissionInfo(
            @JsonProperty("kafkaPrincipal") KafkaPrincipal kafkaPrincipal,
            @JsonProperty("resourceType") ResourceType resourceType,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("patternType") PatternType patternType,
            @JsonProperty("permissionType") AclPermissionType permissionType,
            @JsonProperty("operation") AclOperation operation,
            @JsonProperty("host") String host,
            @JsonProperty("metadata") Metadata metadata) {
        Validate.notNull(kafkaPrincipal, "Kafka principal can't be null");
        Validate.notNull(resourceType, "Resource type can't be null");
        Validate.notBlank(resourceName, "Resource name can't be blank");
        Validate.notNull(patternType, "Pattern type can't be null");
        Validate.notNull(permissionType, "Permission type can't be null");
        Validate.notNull(operation, "Operation can't be null");
        Validate.notBlank(host, "Host can't be blank");

        this.kafkaPrincipal = kafkaPrincipal;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType;
        this.permissionType = permissionType;
        this.operation = operation;
        this.host = host;
        this.metadata = metadata;
    }

    public KafkaPrincipal getKafkaPrincipal() {
        return kafkaPrincipal;
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
    public Optional<Metadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                kafkaPrincipal,
                resourceType,
                resourceName,
                patternType,
                permissionType,
                operation,
                host,
                metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        PermissionInfo that = (PermissionInfo)obj;
        return
                Objects.equals(this.kafkaPrincipal, that.kafkaPrincipal) &&
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.patternType, that.patternType) &&
                Objects.equals(this.permissionType, that.permissionType) &&
                Objects.equals(this.operation, that.operation) &&
                Objects.equals(this.host, that.host) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public String toString() {
        return
                "{kafkaPrincipal: " + kafkaPrincipal +
                ", resourceType: " + resourceType +
                ", resourceName: " + resourceName +
                ", patternType: " + patternType +
                ", permissionType: " + permissionType +
                ", operation: " + operation +
                ", host: " + host +
                ", metadata: " + metadata +
                "}";
    }

    @Override
    public int compareTo(PermissionInfo that) {
        int result = ObjectUtils.compare(this.resourceType, that.resourceType);
        if (result == 0) {
            result = ObjectUtils.compare(this.resourceName, that.resourceName);
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.patternType, that.patternType);
        }
        if (result == 0) {
            result = ObjectUtils.compare(
                    this.kafkaPrincipal.getPrincipalType(),
                    that.kafkaPrincipal.getPrincipalType());
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.kafkaPrincipal.getName(), that.kafkaPrincipal.getName());
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.permissionType, that.permissionType);
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.operation, that.operation);
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.host, that.host);
        }
        return result;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private KafkaPrincipal kafkaPrincipal;
        private ResourceType resourceType;
        private String resourceName;
        private PatternType patternType;
        private AclPermissionType permissionType;
        private AclOperation operation;
        private String host;
        private Metadata metadata;

        public Builder kafkaPrincipal(KafkaPrincipal principal) {
            this.kafkaPrincipal = principal;
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
        public Builder patternType(PatternType patternType) {
            this.patternType = patternType;
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
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public PermissionInfo build() {
            return new PermissionInfo(
                    kafkaPrincipal,
                    resourceType,
                    resourceName,
                    patternType,
                    permissionType,
                    operation,
                    host,
                    metadata);
        }

    }

}
