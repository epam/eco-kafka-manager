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

import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class Metadata {

    private final String description;
    private final Map<String, Object> attributes;
    private final Date updatedAt;
    private final String updatedBy;

    public Metadata(
            @JsonProperty("description") String description,
            @JsonProperty("attributes") Map<String, Object> attributes,
            @JsonProperty("updatedAt") Date updatedAt,
            @JsonProperty("updatedBy") String updatedBy) {
        this.description = description;
        this.attributes =
                attributes != null ?
                Collections.unmodifiableMap(new HashMap<>(attributes)) :
                Collections.emptyMap();
        this.updatedAt = updatedAt != null ? (Date) updatedAt.clone() : null;
        this.updatedBy = updatedBy;
    }

    public String getDescription() {
        return description;
    }
    public Map<String, Object> getAttributes() {
        return attributes;
    }
    public Object getAttribute(String name) {
        return attributes.get(name);
    }
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String name, Class<T> type) {
        return (T)attributes.get(name);
    }
    public Date getUpdatedAt() {
        return updatedAt != null ? (Date) updatedAt.clone() : null;
    }
    public String getUpdatedBy() {
        return updatedBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, attributes, updatedAt, updatedBy);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        Metadata that = (Metadata)obj;
        return
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.attributes, that.attributes) &&
                Objects.equals(this.updatedAt, that.updatedAt) &&
                Objects.equals(this.updatedBy, that.updatedBy);
    }

    @Override
    public String toString() {
        return
                "{description: " + description +
                ", attributes: " + attributes +
                ", updatedAt: " + updatedAt +
                ", updatedBy: " + updatedBy +
                "}";
    }

    public Builder toBuilder() {
        return builder(this);
    }

    public static Builder builder() {
        return builder(null);
    }

    public static Builder builder(Metadata origin) {
        return new Builder(origin);
    }

    public static final class Builder {

        private String description;
        private Map<String, Object> attributes = new HashMap<>();
        private Date updatedAt;
        private String updatedBy;

        private Builder() {
            this(null);
        }

        private Builder(Metadata origin) {
            if (origin == null) {
                return;
            }

            this.description = origin.description;
            this.attributes.putAll(origin.attributes);
            this.updatedAt = origin.updatedAt;
            this.updatedBy = origin.updatedBy;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder attribute(String key, Object value) {
            return attributes(Collections.singletonMap(key, value));
        }

        public Builder attributes(Map<String, Object> attributes) {
            this.attributes.clear();
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder appendAttribute(String key, Object value) {
            return appendAttributes(Collections.singletonMap(key, value));
        }

        public Builder appendAttributes(Map<String, Object> attributes) {
            if (attributes != null) {
                this.attributes.putAll(attributes);
            }
            return this;
        }

        public Builder subtractAttribute(String key) {
            return subtractAttributes(Collections.singleton(key));
        }

        public Builder subtractAttributes(Collection<String> keys) {
            if (keys != null) {
                attributes.keySet().removeAll(keys);
            }
            return this;
        }

        public Builder updatedAtNow() {
            return updatedAt(new Date());
        }

        public Builder updatedAt(Date updatedAt) {
            this.updatedAt = updatedAt != null ? (Date) updatedAt.clone() : null;
            return this;
        }

        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        public Builder updatedBy(Principal updatedBy) {
            this.updatedBy = updatedBy != null ? updatedBy.getName() : null;
            return this;
        }

        public Metadata build() {
            return new Metadata(description, attributes, updatedAt, updatedBy);
        }

    }

}
