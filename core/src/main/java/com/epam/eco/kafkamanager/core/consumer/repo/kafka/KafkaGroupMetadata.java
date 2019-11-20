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
package com.epam.eco.kafkamanager.core.consumer.repo.kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

/**
 * @author Andrei_Tytsik
 */
class KafkaGroupMetadata {

    private final String name;
    private final AtomicReference<GroupMetadataAdapter> groupMetadata = new AtomicReference<>();
    private final Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata = new HashMap<>();

    public KafkaGroupMetadata(String name) {
        Validate.notBlank(name, "Name can't be blank");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GroupMetadataAdapter getGroupMetadata() {
        return groupMetadata.get();
    }

    public Map<TopicPartition, OffsetAndMetadataAdapter> getOffsetsMetadata() {
        return offsetsMetadata;
    }

    public void setGroupMetadata(GroupMetadataAdapter groupMetadata) {
        this.groupMetadata.set(groupMetadata);
    }

    public void setOffsetsMetadata(Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata) {
        this.offsetsMetadata.clear();

        if (!MapUtils.isEmpty(offsetsMetadata)) {
            this.offsetsMetadata.putAll(offsetsMetadata);
        }
    }

    public void updateOffsetsMetadata(Map<TopicPartition, OffsetAndMetadataAdapter> offsetsMetadata) {
        if (MapUtils.isEmpty(offsetsMetadata)) {
            return;
        }

        offsetsMetadata.forEach(this::updateOffsetMetadata);
    }

    public void updateOffsetMetadata(
            TopicPartition topicPartition,
            OffsetAndMetadataAdapter offsetAndMetadata) {
        Validate.notNull(topicPartition, "TopicPartition is null");

        if (offsetAndMetadata != null) {
            offsetsMetadata.put(topicPartition, offsetAndMetadata);
        } else {
            offsetsMetadata.remove(topicPartition);
        }
    }

    public boolean isValid() {
        return groupMetadata.get() != null;
    }

    public KafkaGroupMetadata copyOf() {
        KafkaGroupMetadata copy = new KafkaGroupMetadata(getName());
        copy.setGroupMetadata(getGroupMetadata());
        copy.setOffsetsMetadata(getOffsetsMetadata());
        return copy;
    }

}
