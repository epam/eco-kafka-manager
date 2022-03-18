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

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.TopicPartition;

import kafka.coordinator.group.MemberMetadata;

/**
 * @author Andrei_Tytsik
 */
class ServerMemberMetadata implements MemberMetadataAdapter {

    private final MemberMetadata metadata;
    private final List<TopicPartition> assignment;

    public ServerMemberMetadata(MemberMetadata metadata) {
        Validate.notNull(metadata, "Member metadata is null");

        this.metadata = metadata;
        this.assignment = ConsumerProtocolUtils.deserializeAssignment(metadata.assignment());
    }

    @Override
    public String getClientId() {
        return metadata.clientId();
    }

    @Override
    public String getMemberId() {
        return metadata.memberId();
    }

    @Override
    public String getClientHost() {
        return metadata.clientHost();
    }

    @Override
    public Integer getRebalanceTimeoutMs() {
        return metadata.rebalanceTimeoutMs();
    }

    @Override
    public Integer getSessionTimeoutMs() {
        return metadata.sessionTimeoutMs();
    }

    @Override
    public Long getLatestHeartbeatDate() {
        return metadata.latestHeartbeat();
    }

    @Override
    public List<TopicPartition> getAssignment() {
        return assignment;
    }

}
