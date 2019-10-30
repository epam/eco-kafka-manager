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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class ConsumerGroupMetadataKey extends MetadataKey {

    private final String groupName;

    public ConsumerGroupMetadataKey(
            @JsonProperty("groupName") String groupName) {
        super(EntityType.CONSUMER_GROUP);

        Validate.notBlank(groupName, "Group name can't be blank");

        this.groupName = groupName;
    }

    public String getGroupName() {
        return groupName;
    }

    @Override
    public Object getEntityId() {
        return groupName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), groupName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        ConsumerGroupMetadataKey that = (ConsumerGroupMetadataKey)obj;
        return
                Objects.equals(this.groupName, that.groupName);
    }

    @Override
    public String toString() {
        return
                "{entityType: " + entityType +
                ", groupName: " + groupName +
                "}";
    }

    public static final ConsumerGroupMetadataKey with(String groupName) {
        return new ConsumerGroupMetadataKey(groupName);
    }

}
