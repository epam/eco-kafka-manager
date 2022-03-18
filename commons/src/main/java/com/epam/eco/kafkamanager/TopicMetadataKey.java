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

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class TopicMetadataKey extends MetadataKey {

    private final String topicName;

    public TopicMetadataKey(
            @JsonProperty("topicName") String topicName) {
        super(EntityType.TOPIC);

        Validate.notBlank(topicName, "topicName is blank");

        this.topicName = topicName;
    }

    public String getTopicName() {
        return topicName;
    }

    @Override
    public Object getEntityId() {
        return topicName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), topicName);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        TopicMetadataKey that = (TopicMetadataKey)obj;
        return
                Objects.equals(this.topicName, that.topicName);
    }

    @Override
    public String toString() {
        return
                "{entityType: " + entityType +
                ", topicName: " + topicName +
                "}";
    }

    public static final TopicMetadataKey with(String topicName) {
        return new TopicMetadataKey(topicName);
    }

}
