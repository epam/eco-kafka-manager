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

import org.apache.commons.lang3.Validate;
import org.apache.kafka.coordinator.group.generated.OffsetCommitValue;

/**
 * @author Andrei_Tytsik
 */
class ServerOffsetAndMetadata implements OffsetAndMetadataAdapter {

    private final OffsetCommitValue metadata;

    public ServerOffsetAndMetadata(OffsetCommitValue metadata) {
        Validate.notNull(metadata, "Offset metadata is null");

        this.metadata = metadata;
    }

    @Override
    public long getOffset() {
        return metadata.offset();
    }

    @Override
    public String getMetadata() {
        return metadata.metadata();
    }

    @Override
    public Long getCommitTimestamp() {
        return metadata.commitTimestamp();
    }

    @Override
    public Long getExpireTimestamp() {
        return metadata.expireTimestamp();
    }

    public static ServerOffsetAndMetadata ofNullable(OffsetCommitValue metadata) {
        return metadata != null ? new ServerOffsetAndMetadata(metadata) : null;
    }

}
