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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class BrokerConfigUpdateParams {

    private final int brokerId;
    private final Map<String, String> config;

    public BrokerConfigUpdateParams(
            @JsonProperty("brokerId") int brokerId,
            @JsonProperty("config") Map<String, String> config) {
        Validate.isTrue(brokerId >= 0, "Broker Id is invalid: %d", brokerId);

        this.brokerId = brokerId;
        this.config =
                config != null ?
                Collections.unmodifiableMap(new HashMap<>(config)) :
                Collections.emptyMap();
    }

    public int getBrokerId() {
        return brokerId;
    }
    public Map<String, String> getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BrokerConfigUpdateParams that = (BrokerConfigUpdateParams) obj;
        return
                Objects.equals(this.brokerId, that.brokerId) &&
                Objects.equals(this.config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerId, config);
    }

    @Override
    public String toString() {
        return
                "{brokerId: " + brokerId +
                ", config: " + config +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(BrokerConfigUpdateParams origin) {
        return new Builder(origin);
    }

    public static BrokerConfigUpdateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, BrokerConfigUpdateParams.class);
    }

    public static BrokerConfigUpdateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, BrokerConfigUpdateParams.class);
    }

    public static class Builder {

        private int brokerId = -1;
        private final Map<String, String> config = new HashMap<>();

        private Builder(BrokerConfigUpdateParams origin) {
            if (origin == null) {
                return;
            }

            this.brokerId = origin.brokerId;
            this.config.putAll(origin.config);
        }

        public Builder brokerId(int brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public Builder appendConfigEntry(String key, String value) {
            this.config.put(key, value);
            return this;
        }

        public Builder config(Map<String, String> config) {
            this.config.clear();
            if (config != null) {
                this.config.putAll(config);
            }
            return this;
        }

        public BrokerConfigUpdateParams build() {
            return new BrokerConfigUpdateParams(brokerId, config);
        }
    }

}
