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
package com.epam.eco.kafkamanager.udmetrics;

import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.SearchQuery;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class UDMetricSearchQuery implements SearchQuery<UDMetric> {

    public enum Status {
        OK, FAILED
    }

    private final UDMetricType type;
    private final String resourceName;
    private final Status status;

    public UDMetricSearchQuery(
            @JsonProperty("type") UDMetricType type,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("status") Status status) {
        this.type = type;
        this.resourceName = resourceName;
        this.status = status;
    }

    public UDMetricType getType() {
        return type;
    }
    public String getResourceName() {
        return resourceName;
    }
    public Status getStatus() {
        return status;
    }

    @Override
    public boolean matches(UDMetric obj) {
        Validate.notNull(obj, "UDM Metric is null");

        Boolean failed = null;
        if (Status.OK == status) {
            failed = Boolean.FALSE;
        } else if (Status.FAILED == status) {
            failed = Boolean.TRUE;
        }

        return
                (type == null || Objects.equals(obj.getType(), type)) &&
                (
                        StringUtils.isBlank(resourceName) ||
                        StringUtils.containsIgnoreCase(obj.getResourceName(), resourceName)) &&
                (failed == null || failed == obj.hasErrors());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        UDMetricSearchQuery that = (UDMetricSearchQuery) obj;
        return
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, resourceName, status);
    }

    @Override
    public String toString() {
        return
                "{type: " + type +
                ", resourceName: " + resourceName +
                ", status: " + status +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(UDMetricSearchQuery origin) {
        return new Builder(origin);
    }

    public static UDMetricSearchQuery fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, UDMetricSearchQuery.class);
    }

    public static UDMetricSearchQuery fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, UDMetricSearchQuery.class);
    }

    public static class Builder {

        private UDMetricType type;
        private String resourceName;
        private Status status;

        private Builder(UDMetricSearchQuery origin) {
            if (origin == null) {
                return;
            }

            this.type = origin.type;
            this.resourceName = origin.resourceName;
            this.status = origin.status;
        }

        public Builder type(UDMetricType type) {
            this.type = type;
            return this;
        }

        public Builder resourceName(String resourceName) {
            this.resourceName = resourceName;
            return this;
        }

        public Builder status(Status status) {
            this.status = status;
            return this;
        }

        public UDMetricSearchQuery build() {
            return new UDMetricSearchQuery(type, resourceName, status);
        }

    }

}
