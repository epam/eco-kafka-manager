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

import java.util.Collection;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.common.TopicPartition;


/**
 * @author Andrei_Tytsik
 */
public class ClientMemberMetadata implements MemberMetadataAdapter {

    private final MemberDescription metadata;

    public ClientMemberMetadata(MemberDescription metadata) {
        Validate.notNull(metadata, "Member description is null");

        this.metadata = metadata;
    }

    @Override
    public String getClientId() {
        return metadata.clientId();
    }

    @Override
    public String getMemberId() {
        return metadata.consumerId();
    }

    @Override
    public String getClientHost() {
        return metadata.host();
    }

    @Override
    public Integer getRebalanceTimeoutMs() {
        return null;
    }

    @Override
    public Integer getSessionTimeoutMs() {
        return null;
    }

    @Override
    public Long getLatestHeartbeatDate() {
        return null;
    }

    @Override
    public Collection<TopicPartition> getAssignment() {
        return metadata.assignment().topicPartitions();
    }

}
