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

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class ConfigValue {

    private final String name;
    private final String value;
    private final boolean isDefault;
    private final boolean isSensitive;
    private final boolean isReadOnly;

    public ConfigValue(String name, String value) {
        this(name, value, false, false, false);
    }

    public ConfigValue(String name, Object value) {
        this(name, value, false, false, false);
    }

    public ConfigValue(
            String name,
            Object value,
            boolean isDefault,
            boolean isSensitive,
            boolean isReadOnly) {
        this(name, Objects.toString(value, null), isDefault, isSensitive, isReadOnly);
    }

    @JsonCreator
    public ConfigValue(
            @JsonProperty("name") String name,
            @JsonProperty("value") String value,
            @JsonProperty("default") boolean isDefault,
            @JsonProperty("sensitive") boolean isSensitive,
            @JsonProperty("readOnly") boolean isReadOnly) {
        Validate.notBlank(name, "Name is blank");

        this.name = name;
        this.value = value;
        this.isDefault = isDefault;
        this.isSensitive = isSensitive;
        this.isReadOnly = isReadOnly;
    }

    public String getName() {
        return name;
    }
    public String getValue() {
        return value;
    }
    public boolean isDefault() {
        return isDefault;
    }
    public boolean isSensitive() {
        return isSensitive;
    }
    public boolean isReadOnly() {
        return isReadOnly;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, isDefault, isSensitive, isReadOnly);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        ConfigValue that = (ConfigValue)obj;
        return
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.value, that.value) &&
                Objects.equals(this.isDefault, that.isDefault) &&
                Objects.equals(this.isSensitive, that.isSensitive) &&
                Objects.equals(this.isReadOnly, that.isReadOnly);
    }

    @Override
    public String toString() {
        return
                "{name: " + name +
                ", value: " + value +
                ", isDefault: " + isDefault +
                ", isSensitive: " + isSensitive +
                ", isReadOnly: " + isReadOnly +
                "}";
    }

}
