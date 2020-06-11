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
import java.util.Collections;
import java.util.List;

import org.apache.kafka.clients.consumer.internals.ConsumerProtocol;
import org.apache.kafka.common.TopicPartition;

/**
 * @author Andrei_Tytsik
 */
class ConsumerProtocolUtils {

    public static List<TopicPartition> deserializeAssignment(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return Collections.emptyList();
        }

        return ConsumerProtocol.deserializeAssignment(ByteBuffer.wrap(bytes)).partitions();
    }

    public static List<TopicPartition> deserializeAssignment(ByteBuffer bytes) {
        if (bytes == null || bytes.remaining() <= 0) {
            return Collections.emptyList();
        }

        return ConsumerProtocol.deserializeAssignment(bytes.duplicate()).partitions();
    }

}
