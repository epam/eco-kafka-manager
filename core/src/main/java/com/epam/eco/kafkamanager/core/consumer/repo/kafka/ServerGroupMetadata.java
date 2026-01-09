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
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.GroupState;
import org.apache.kafka.coordinator.group.generated.GroupMetadataValue;

import static org.apache.kafka.common.GroupState.UNKNOWN;

/**
 * @author Andrei_Tytsik
 */
class ServerGroupMetadata implements GroupMetadataAdapter {

    private final GroupMetadataValue metadata;
    private final List<MemberMetadataAdapter> members;

    public ServerGroupMetadata(GroupMetadataValue metadata) {
        Validate.notNull(metadata, "Group metadata is null");

        this.metadata = metadata;
        this.members = metadata.members().stream().
                map(ServerMemberMetadata::new).
                collect(Collectors.toList());
    }

    @Override
    public String getGroupId() {
        return null;
    }

    @Override
    public Integer getCoordinator() {
        return null;
    }

    @Override
    public GroupState getState() {
        return UNKNOWN;
    }

    @Override
    public String getProtocolType() {
        return metadata.protocolType();
    }

    @Override
    public String getPartitionAssignor() {
        return metadata.protocol();
    }

    @Override
    public List<MemberMetadataAdapter> getMembers() {
        return members;
    }

    public static ServerGroupMetadata ofNullable(GroupMetadataValue metadata) {
        return metadata != null ? new ServerGroupMetadata(metadata) : null;
    }

}
