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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.common.ConsumerGroupState;


/**
 * @author Andrei_Tytsik
 */
class ClientGroupMetadata implements GroupMetadataAdapter {

    private final ConsumerGroupDescription metadata;
    private final List<MemberMetadataAdapter> members;

    public ClientGroupMetadata(ConsumerGroupDescription metadata) {
        Validate.notNull(metadata, "Group description is null");

        this.metadata = metadata;
        this.members = metadata.members().stream().
                map(ClientMemberMetadata::new).
                collect(Collectors.toList());
    }

    @Override
    public String getGroupId() {
        return metadata.groupId();
    }

    @Override
    public Integer getCoordinator() {
        return !metadata.coordinator().isEmpty() ? metadata.coordinator().id() : null;
    }

    @Override
    public ConsumerGroupState getState() {
        return metadata.state();
    }

    @Override
    public String getProtocolType() {
        return null;
    }

    @Override
    public String getPartitionAssignor() {
        return metadata.partitionAssignor();
    }

    @Override
    public Collection<MemberMetadataAdapter> getMembers() {
        return members;
    }

    public static ClientGroupMetadata ofNullable(ConsumerGroupDescription metadata) {
        return metadata != null ? new ClientGroupMetadata(metadata) : null;
    }

}