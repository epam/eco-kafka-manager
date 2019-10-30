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

import org.apache.commons.lang3.Validate;

import kafka.common.OffsetAndMetadata;
import kafka.coordinator.group.OffsetKey;

/**
 * @author Andrei_Tytsik
 */
class KafkaOffsetMetadataRecord implements KafkaMetadataRecord<OffsetKey, OffsetAndMetadata> {

    private final OffsetKey key;
    private final OffsetAndMetadata value;

    public KafkaOffsetMetadataRecord(OffsetKey key, OffsetAndMetadata value) {
        Validate.notNull(key, "Key is null");

        this.key = key;
        this.value = value;
    }

    @Override
    public String getGroupName() {
        return key.key().group();
    }

    @Override
    public OffsetKey getKey() {
        return key;
    }

    @Override
    public OffsetAndMetadata getValue() {
        return value;
    }

    @Override
    public String toString() {
        return String.format("{key: %s, value: %s}", key, value);
    }

}
