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

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class BrokerInfo implements MetadataAware, Comparable<BrokerInfo> {

    private final int id;
    private final List<EndPointInfo> endPoints;
    private final String rack;
    private final Map<String, ConfigValue> config;
    private final Metadata metadata;

    public BrokerInfo(
            @JsonProperty("id") int id,
            @JsonProperty("endPoints") List<EndPointInfo> endPoints,
            @JsonProperty("rack") String rack,
            @JsonProperty("config") Map<String, ConfigValue> config,
            @JsonProperty("metadata") Metadata metadata) {
        Validate.isTrue(id >= 0, "Id is invalid");
        Validate.notEmpty(endPoints, "Collection of endPoints is null or empty");
        Validate.noNullElements(endPoints, "Collection of endPoints contains null elements");
        Validate.notEmpty(config, "Config map is null or empty");
        Validate.noNullElements(config.keySet(), "Collection of config keys contains null elements");
        Validate.noNullElements(config.values(), "Collection of config values contains null elements");

        this.id = id;
        this.endPoints = endPoints.stream().
                sorted().
                collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                Collections::unmodifiableList));
        this.rack = rack;
        this.config = Collections.unmodifiableMap(new TreeMap<>(config));
        this.metadata = metadata;
    }

    public int getId() {
        return id;
    }
    public List<EndPointInfo> getEndPoints() {
        return endPoints;
    }
    public String getRack() {
        return rack;
    }
    public Map<String, ConfigValue> getConfig() {
        return config;
    }
    @Override
    public Optional<Metadata> getMetadata() {
        return Optional.ofNullable(metadata);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, endPoints, rack, config, metadata);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        BrokerInfo that = (BrokerInfo)obj;
        return
                Objects.equals(this.id, that.id) &&
                Objects.equals(this.endPoints, that.endPoints) &&
                Objects.equals(this.rack, that.rack) &&
                Objects.equals(this.config, that.config) &&
                Objects.equals(this.metadata, that.metadata);
    }

    @Override
    public String toString() {
        return
                "{id: " + id +
                ", endPoints: " + endPoints +
                ", rack: " + rack +
                ", metadata: " + metadata +
                "}";
    }

    @Override
    public int compareTo(BrokerInfo that) {
        return ObjectUtils.compare(this.id, that.id);
    }

    public Builder toBuilder() {
        return new Builder(this);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private int id;
        private List<EndPointInfo> endPoints = new ArrayList<>();
        private String rack;
        private Map<String, ConfigValue> config = new HashMap<>();
        private Metadata metadata;

        public Builder() {
            this(null);
        }

        public Builder(BrokerInfo origin) {
            if (origin == null) {
                return;
            }

            this.id = origin.id;
            this.endPoints.addAll(origin.endPoints);
            this.rack = origin.rack;
            this.config.putAll(origin.getConfig());
            this.metadata = origin.metadata;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }
        public Builder addEndPoint(EndPointInfo endPoint) {
            this.endPoints.add(endPoint);
            return this;
        }
        public Builder endPoints(List<EndPointInfo> endPoints) {
            this.endPoints.clear();
            if (endPoints != null) {
                this.endPoints.addAll(endPoints);
            }
            return this;
        }
        public Builder rack(String rack) {
            this.rack = rack;
            return this;
        }
        public Builder addConfig(ConfigValue config) {
            this.config.put(config.getName(), config);
            return this;
        }
        public Builder config(Map<String, ConfigValue> config) {
            this.config.clear();
            if (config != null) {
                this.config.putAll(config);
            }
            return this;
        }
        public Builder metadata(Metadata metadata) {
            this.metadata = metadata;
            return this;
        }

        public BrokerInfo build() {
            return new BrokerInfo(id, endPoints, rack, config, metadata);
        }

    }

}
