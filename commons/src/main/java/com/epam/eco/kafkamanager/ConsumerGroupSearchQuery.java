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

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupSearchQuery implements SearchQuery<ConsumerGroupInfo> {

    private final String groupName;
    private final StorageType storageType;
    private final String description;

    public ConsumerGroupSearchQuery(
            @JsonProperty("groupName") String groupName,
            @JsonProperty("storageType") StorageType storageType,
            @JsonProperty("description") String description) {
        this.groupName = groupName;
        this.storageType = storageType;
        this.description = description;
    }

    public String getGroupName() {
        return groupName;
    }
    public StorageType getStorageType() {
        return storageType;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public boolean matches(ConsumerGroupInfo obj) {
        Validate.notNull(obj, "Consumer Group Info is null");

        return
                (StringUtils.isBlank(groupName) || StringUtils.containsIgnoreCase(obj.getName(), groupName)) &&
                (storageType == null || Objects.equals(obj.getStorageType(), storageType)) &&
                (
                        StringUtils.isBlank(description) ||
                        StringUtils.containsIgnoreCase(
                                obj.getMetadata().map(Metadata::getDescription).orElse(null), description));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConsumerGroupSearchQuery that = (ConsumerGroupSearchQuery) obj;
        return
                Objects.equals(this.groupName, that.groupName) &&
                Objects.equals(this.storageType, that.storageType) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, storageType, description);
    }

    @Override
    public String toString() {
        return
                "{groupName: " + groupName +
                ", storageType: " + storageType +
                ", description: " + description +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(ConsumerGroupSearchQuery origin) {
        return new Builder(origin);
    }

    public static ConsumerGroupSearchQuery fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, ConsumerGroupSearchQuery.class);
    }

    public static ConsumerGroupSearchQuery fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, ConsumerGroupSearchQuery.class);
    }

    public static class Builder {

        private String groupName;
        private StorageType storageType;
        private String description;

        private Builder(ConsumerGroupSearchQuery origin) {
            if (origin == null) {
                return;
            }

            this.groupName = origin.groupName;
            this.storageType = origin.storageType;
            this.description = origin.description;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder storageType(StorageType storageType) {
            this.storageType = storageType;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public ConsumerGroupSearchQuery build() {
            return new ConsumerGroupSearchQuery(groupName, storageType, description);
        }

    }

}
