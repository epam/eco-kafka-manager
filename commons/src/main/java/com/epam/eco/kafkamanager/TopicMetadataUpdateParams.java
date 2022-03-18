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
public class TopicMetadataUpdateParams {

    private final String topicName;
    private final String description;
    private final Map<String, Object> attributes;

    public TopicMetadataUpdateParams(
            @JsonProperty("topicName") String topicName,
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes) {
        Validate.notBlank(topicName, "Topic name is blank");

        this.topicName = topicName;
        this.description = description;
        this.attributes =
                attributes != null ?
                Collections.unmodifiableMap(new HashMap<>(attributes)) :
                Collections.emptyMap();
    }

    public String getTopicName() {
        return topicName;
    }
    public String getDescription() {
        return description;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicMetadataUpdateParams that = (TopicMetadataUpdateParams) obj;
        return
                Objects.equals(this.topicName, that.topicName) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, description, attributes);
    }

    @Override
    public String toString() {
        return
                "{topicName: " + topicName +
                ", description: " + description +
                ", attributes: " + attributes +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder((TopicMetadataUpdateParams)null);
    }

    public static Builder builder(TopicMetadataUpdateParams origin) {
        return new Builder(origin);
    }

    public static Builder builder(Metadata origin) {
        return new Builder(origin);
    }

    public static TopicMetadataUpdateParams fromJson(Map<String, ?> map) {
        Validate.notNull(map, "JSON map is null");

        return MapperUtils.convert(map, TopicMetadataUpdateParams.class);
    }

    public static TopicMetadataUpdateParams fromJson(String json) {
        Validate.notNull(json, "JSON is null");

        return MapperUtils.jsonToBean(json, TopicMetadataUpdateParams.class);
    }

    public static class Builder {

        private String topicName;
        private String description;
        private Map<String, Object> attributes = new HashMap<>();

        private Builder(TopicMetadataUpdateParams origin) {
            if (origin == null) {
                return;
            }

            this.topicName = origin.topicName;
            this.description = origin.description;
            this.attributes.putAll(origin.attributes);
        }

        private Builder(Metadata origin) {
            if (origin == null) {
                return;
            }

            this.description = origin.getDescription();
            this.attributes.putAll(origin.getAttributes());
        }

        public Builder topicName(String topicName) {
            this.topicName = topicName;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.clear();
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttribute(String key, Object value) {
            this.attributes.put(key, value);
            return this;
        }

        public Builder removeAttribute(String key) {
            this.attributes.remove(key);
            return this;
        }

        public TopicMetadataUpdateParams build() {
            return new TopicMetadataUpdateParams(topicName, description, attributes);
        }

    }

}
