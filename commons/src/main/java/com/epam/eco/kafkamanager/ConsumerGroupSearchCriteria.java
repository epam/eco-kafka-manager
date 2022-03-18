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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.ConsumerGroupState;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.ConsumerGroupInfo.StorageType;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupSearchCriteria implements SearchCriteria<ConsumerGroupInfo> {

    private final String groupName;
    private final ConsumerGroupState state;
    private final StorageType storageType;
    private final String description;

    public ConsumerGroupSearchCriteria(
            @JsonProperty("groupName") String groupName,
            @JsonProperty("state") ConsumerGroupState state,
            @JsonProperty("storageType") StorageType storageType,
            @JsonProperty("description") String description) {
        this.groupName = groupName;
        this.state = state;
        this.storageType = storageType;
        this.description = description;
    }

    public String getGroupName() {
        return groupName;
    }
    public ConsumerGroupState getState() {
        return state;
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
                (state == null || Objects.equals(obj.getState(), state)) &&
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
        ConsumerGroupSearchCriteria that = (ConsumerGroupSearchCriteria) obj;
        return
                Objects.equals(this.groupName, that.groupName) &&
                Objects.equals(this.state, that.state) &&
                Objects.equals(this.storageType, that.storageType) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName, state, storageType, description);
    }

    @Override
    public String toString() {
        return
                "{groupName: " + groupName +
                ", state: " + state +
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

    public static Builder builder(ConsumerGroupSearchCriteria origin) {
        return new Builder(origin);
    }

    public static ConsumerGroupSearchCriteria fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, ConsumerGroupSearchCriteria.class);
    }

    public static ConsumerGroupSearchCriteria fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, ConsumerGroupSearchCriteria.class);
    }

    public static class Builder {

        private String groupName;
        private ConsumerGroupState state;
        private StorageType storageType;
        private String description;

        private Builder(ConsumerGroupSearchCriteria origin) {
            if (origin == null) {
                return;
            }

            this.groupName = origin.groupName;
            this.state = origin.state;
            this.storageType = origin.storageType;
            this.description = origin.description;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }

        public Builder state(ConsumerGroupState state) {
            this.state = state;
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

        public ConsumerGroupSearchCriteria build() {
            return new ConsumerGroupSearchCriteria(groupName, state, storageType, description);
        }

    }

}
