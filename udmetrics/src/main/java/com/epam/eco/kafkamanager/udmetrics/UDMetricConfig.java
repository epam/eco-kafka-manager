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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class UDMetricConfig implements Comparable<UDMetricConfig> {

    private final String name;
    private final UDMetricType type;
    private final String resourceName;
    private final Map<String, Object> config;

    private final int hashCode;
    private final String toString;

    @JsonCreator
    public UDMetricConfig(
            @JsonProperty("name") String name,
            @JsonProperty("type") UDMetricType type,
            @JsonProperty("resourceName") String resourceName,
            @JsonProperty("config") Map<String, Object> config) {
        Validate.notBlank(name, "Name is blank");
        Validate.notNull(type, "Type is null");
        Validate.notBlank(resourceName, "Resource name is blank");

        this.name = name;
        this.type = type;
        this.resourceName = resourceName;
        this.config =
                config != null ?
                Collections.unmodifiableMap(new LinkedHashMap<>(config)) :
                Collections.emptyMap();

        hashCode = calculateHashCode();
        toString = calculateToString();
    }

    public String getName() {
        return name;
    }
    public UDMetricType getType() {
        return type;
    }
    public String getResourceName() {
        return resourceName;
    }
    public Map<String, Object> getConfig() {
        return config;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        UDMetricConfig that = (UDMetricConfig)obj;
        return
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.config, that.config);
    }

    @Override
    public String toString() {
        return toString;
    }

    @Override
    public int compareTo(UDMetricConfig that) {
        int result = ObjectUtils.compare(this.type, that.type);
        if (result == 0) {
            result = ObjectUtils.compare(this.resourceName, that.resourceName);
        }
        return result;
    }

    private int calculateHashCode() {
        return Objects.hash(name, type, resourceName, config);
    }

    private String calculateToString() {
        return
                "{name: " + name +
                ", type: " + type +
                ", resourceName: " + resourceName +
                ", config: " + config +
                "}";
    }

    public static UDMetricConfig with(
            String name,
            UDMetricType type,
            String resourceName,
            Map<String, Object> config) {
        return new UDMetricConfig(name, type, resourceName, config);
    }

}
