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

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupMetadataDeleteParams {

    private final String groupName;

    public ConsumerGroupMetadataDeleteParams(
            @JsonProperty("groupName") String groupName) {
        Validate.notBlank(groupName, "Group Name is blank");

        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ConsumerGroupMetadataDeleteParams that = (ConsumerGroupMetadataDeleteParams) obj;
        return
                Objects.equals(this.groupName, that.groupName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupName);
    }

    @Override
    public String toString() {
        return
                "{groupName: " + groupName +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(ConsumerGroupMetadataDeleteParams origin) {
        return new Builder(origin);
    }

    public static ConsumerGroupMetadataDeleteParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, ConsumerGroupMetadataDeleteParams.class);
    }

    public static ConsumerGroupMetadataDeleteParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, ConsumerGroupMetadataDeleteParams.class);
    }

    public static class Builder {

        private String groupName;

        private Builder(ConsumerGroupMetadataDeleteParams origin) {
            if (origin == null) {
                return;
            }

            this.groupName = origin.groupName;
        }

        public Builder groupName(String groupName) {
            this.groupName = groupName;
            return this;
        }


        public ConsumerGroupMetadataDeleteParams build() {
            return new ConsumerGroupMetadataDeleteParams(groupName);
        }

    }

}
