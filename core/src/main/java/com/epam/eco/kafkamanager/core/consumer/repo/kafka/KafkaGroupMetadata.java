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

import org.apache.commons.lang3.Validate;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.GroupMetadata;
import kafka.coordinator.group.OffsetKey;

/**
 * @author Andrei_Tytsik
 */
class KafkaGroupMetadata {

    private final String name;
    private GroupMetadata groupMetadata;
    private final Map<OffsetKey, OffsetAndMetadata> offsetsAndMetadata = new HashMap<>();

    public KafkaGroupMetadata(String name) {
        Validate.notBlank(name, "Name can't be blank");

        this.name = name;
    }

    public String getName() {
        return name;
    }

    public GroupMetadata getGroupMetadata() {
        return groupMetadata;
    }

    public Map<OffsetKey, OffsetAndMetadata> getOffsetsAndMetadata() {
        return offsetsAndMetadata;
    }

    public void updateGroupMetadata(GroupMetadata metadata) {
        this.groupMetadata = metadata;
    }

    public void updateOffsetsAndMetadata(Map<OffsetKey, OffsetAndMetadata> offsetsAndMetadata) {
        this.offsetsAndMetadata.putAll(offsetsAndMetadata);
    }

    public void updateOffsetAndMetadata(OffsetKey key, OffsetAndMetadata value) {
        Validate.notNull(key, "Key is null");

        if (value != null) {
            offsetsAndMetadata.put(key, value);
        } else {
            offsetsAndMetadata.remove(key);
        }
    }

    public boolean hasMetadata() {
        return groupMetadata != null || !offsetsAndMetadata.isEmpty();
    }

    public KafkaGroupMetadata copyOf() {
        KafkaGroupMetadata copy = new KafkaGroupMetadata(this.name);
        copy.updateGroupMetadata(this.groupMetadata);
        copy.updateOffsetsAndMetadata(this.offsetsAndMetadata);
        return copy;
    }

}
