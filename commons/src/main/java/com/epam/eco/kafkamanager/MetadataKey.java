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

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * @author Andrei_Tytsik
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "entityType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BrokerMetadataKey.class, name = "BROKER"),
        @JsonSubTypes.Type(value = ConsumerGroupMetadataKey.class, name = "CONSUMER_GROUP"),
        @JsonSubTypes.Type(value = PermissionMetadataKey.class, name = "PERMISSION"),
        @JsonSubTypes.Type(value = TopicMetadataKey.class, name = "TOPIC")
})
public abstract class MetadataKey {

    protected final EntityType entityType;

    public MetadataKey(EntityType entityType) {
        Validate.notNull(entityType, "Entity type can't be null");

        this.entityType = entityType;
    }

    public EntityType getEntityType() {
        return entityType;
    }

    @JsonIgnore
    public abstract Object getEntityId();

    @Override
    public int hashCode() {
        return Objects.hash(entityType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        MetadataKey that = (MetadataKey)obj;
        return
                Objects.equals(this.entityType, that.entityType);
    }

}
