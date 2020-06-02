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
package com.epam.eco.kafkamanager.core.consumer.repo.kafka;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.utils.Time;

import com.epam.eco.commons.kafka.serde.KeyValueDecoder;

import kafka.coordinator.group.BaseKey;
import kafka.coordinator.group.GroupMetadataKey;
import kafka.coordinator.group.GroupMetadataManager;
import kafka.coordinator.group.OffsetKey;

/**
 * @author Andrei_Tytsik
 */
class ServerGroupMetadataDecoder implements KeyValueDecoder<BaseKey, Object> {

    @Override
    public BaseKey decodeKey(byte[] keyBytes) {
        Validate.notNull(keyBytes, "Key bytes array can't be null");

        return GroupMetadataManager.readMessageKey(ByteBuffer.wrap(keyBytes));
    }

    @Override
    public Object decodeValue(BaseKey key, byte[] valueBytes) {
        Validate.notNull(key, "Key can't be null");

        if (valueBytes == null) {
            return null;
        }

        if (key instanceof OffsetKey) {
            return GroupMetadataManager.readOffsetMessageValue(ByteBuffer.wrap(valueBytes));
        } else if (key instanceof GroupMetadataKey) {
            return GroupMetadataManager.readGroupMessageValue(
                    ((GroupMetadataKey)key).key(),
                    ByteBuffer.wrap(valueBytes),
                    Time.SYSTEM);
        } else {
            throw new IllegalArgumentException("Unsupported key=" + key);
        }
    }

}
