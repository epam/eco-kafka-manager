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
package com.epam.eco.kafkamanager.core.consumer.repo.kafka;

import java.nio.ByteBuffer;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.protocol.ApiMessage;
import org.apache.kafka.common.protocol.MessageUtil;
import org.apache.kafka.coordinator.common.runtime.CoordinatorRecord;
import org.apache.kafka.coordinator.group.GroupCoordinatorRecordSerde;

import com.epam.eco.commons.kafka.serde.KeyValueDecoder;


/**
 * @author Andrei_Tytsik
 */
class ServerGroupMetadataDecoder implements KeyValueDecoder<ApiMessage, Object> {

    private final GroupCoordinatorRecordSerde groupCoordinatorRecordSerde =
            new GroupCoordinatorRecordSerde();

    @Override
    public ApiMessage decodeKey(byte[] keyBytes) {
        Validate.notNull(keyBytes, "Key bytes array can't be null");
        CoordinatorRecord record =
                groupCoordinatorRecordSerde.deserialize(ByteBuffer.wrap(keyBytes), null);
        return record.key();
    }

    @Override
    public Object decodeValue(
            ApiMessage key,
            byte[] valueBytes
    ) {
        Validate.notNull(key, "Key can't be null");

        if (valueBytes == null) {
            return null;
        }
        CoordinatorRecord record =
                groupCoordinatorRecordSerde.deserialize(ByteBuffer.wrap(MessageUtil.toCoordinatorTypePrefixedBytes(key)),
                        ByteBuffer.wrap(valueBytes));
        return record.value().message();
    }

}
