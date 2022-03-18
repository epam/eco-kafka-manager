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

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class BrokerMetadataDeleteParams {

    private final int brokerId;

    public BrokerMetadataDeleteParams(
            @JsonProperty("brokerId") int brokerId) {
        Validate.isTrue(brokerId >= 0, "Broker Id is invalid");

        this.brokerId = brokerId;
    }

    public int getBrokerId() {
        return brokerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        BrokerMetadataDeleteParams that = (BrokerMetadataDeleteParams) obj;
        return
                Objects.equals(this.brokerId, that.brokerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerId);
    }

    @Override
    public String toString() {
        return
                "{brokerId: " + brokerId +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(BrokerMetadataDeleteParams origin) {
        return new Builder(origin);
    }

    public static BrokerMetadataDeleteParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, BrokerMetadataDeleteParams.class);
    }

    public static BrokerMetadataDeleteParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, BrokerMetadataDeleteParams.class);
    }

    public static class Builder {

        private int brokerId;

        private Builder(BrokerMetadataDeleteParams origin) {
            if (origin == null) {
                return;
            }

            this.brokerId = origin.brokerId;
        }

        public Builder brokerId(int brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public BrokerMetadataDeleteParams build() {
            return new BrokerMetadataDeleteParams(brokerId);
        }
    }

}
