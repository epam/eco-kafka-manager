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

import com.fasterxml.jackson.annotation.JsonProperty;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class BrokerSearchCriteria implements SearchCriteria<BrokerInfo> {

    private final Integer brokerId;
    private final String rack;
    private final String description;

    public BrokerSearchCriteria(
            @JsonProperty("brokerId") Integer brokerId,
            @JsonProperty("rack") String rack,
            @JsonProperty("description") String description) {
        this.brokerId = brokerId;
        this.rack = rack;
        this.description = description;
    }

    public Integer getBrokerId() {
        return brokerId;
    }
    public String getRack() {
        return rack;
    }
    public String getDescription() {
        return description;
    }

    @Override
    public boolean matches(BrokerInfo obj) {
        Validate.notNull(obj, "Broker Info is null");

        return
                (brokerId == null || Objects.equals(obj.getId(), brokerId)) &&
                (StringUtils.isBlank(rack) || StringUtils.containsIgnoreCase(obj.getRack(), rack)) &&
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
        BrokerSearchCriteria that = (BrokerSearchCriteria) obj;
        return
                Objects.equals(this.brokerId, that.brokerId) &&
                Objects.equals(this.rack, that.rack) &&
                Objects.equals(this.description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(brokerId, rack, description);
    }

    @Override
    public String toString() {
        return
                "{brokerId: " + brokerId +
                ", rack: " + rack +
                ", description: " + description +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(BrokerSearchCriteria origin) {
        return new Builder(origin);
    }

    public static BrokerSearchCriteria fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, BrokerSearchCriteria.class);
    }

    public static BrokerSearchCriteria fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, BrokerSearchCriteria.class);
    }

    public static class Builder {

        private Integer brokerId;
        private String rack;
        private String description;

        private Builder(BrokerSearchCriteria origin) {
            if (origin == null) {
                return;
            }

            this.brokerId = origin.brokerId;
            this.rack = origin.rack;
            this.description = origin.description;
        }

        public Builder brokerId(Integer brokerId) {
            this.brokerId = brokerId;
            return this;
        }

        public Builder rack(String rack) {
            this.rack = rack;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public BrokerSearchCriteria build() {
            return new BrokerSearchCriteria(brokerId, rack, description);
        }

    }

}
