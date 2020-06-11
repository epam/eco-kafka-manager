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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.SearchCriteria;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class UDMetricConfigSearchCriteria implements SearchCriteria<UDMetricConfig> {

    private final UDMetricType type;
    private final String resourceName;

    public UDMetricConfigSearchCriteria(
            @JsonProperty("type") UDMetricType type,
            @JsonProperty("resourceName") String resourceName) {
        this.type = type;
        this.resourceName = resourceName;
    }

    public UDMetricType getType() {
        return type;
    }
    public String getResourceName() {
        return resourceName;
    }

    @Override
    public boolean matches(UDMetricConfig obj) {
        Validate.notNull(obj, "UDM Config is null");

        return
                (type == null || Objects.equals(obj.getType(), type)) &&
                (StringUtils.isBlank(resourceName) || StringUtils.containsIgnoreCase(obj.getResourceName(), resourceName));
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UDMetricConfigSearchCriteria that = (UDMetricConfigSearchCriteria) obj;
        return
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.resourceName, that.resourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resourceName);
    }

    @Override
    public String toString() {
        return
                "{type: " + type +
                ", resourceName: " + resourceName +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(UDMetricConfigSearchCriteria origin) {
        return new Builder(origin);
    }

    public static UDMetricConfigSearchCriteria fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, UDMetricConfigSearchCriteria.class);
    }

    public static UDMetricConfigSearchCriteria fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, UDMetricConfigSearchCriteria.class);
    }

    public static class Builder {

        private UDMetricType type;
        private String resourceName;

        private Builder(UDMetricConfigSearchCriteria origin) {
            if (origin == null) {
                return;
            }

            this.type = origin.type;
            this.resourceName = origin.resourceName;
        }

        public Builder type(UDMetricType type) {
            this.type = type;
            return this;
        }

        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public UDMetricConfigSearchCriteria build() {
            return new UDMetricConfigSearchCriteria(type, resourceName);
        }

    }

}
