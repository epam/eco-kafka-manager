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
package com.epam.eco.kafkamanager.rest.request;

import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raman_Babich
 */
public class MetadataRequest {

    private final String description;
    private final Map<String, Object> attributes;

    public MetadataRequest(
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        this.description = description;
        this.attributes = attributes;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetadataRequest that = (MetadataRequest) o;
        return Objects.equals(description, that.description) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, attributes);
    }

    @Override
    public String toString() {
        return "MetadataRequest{" +
                "description='" + description + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
