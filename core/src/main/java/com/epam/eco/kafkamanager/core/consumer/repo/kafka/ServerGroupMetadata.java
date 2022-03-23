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
import org.apache.kafka.common.ConsumerGroupState;

import com.epam.eco.commons.kafka.ScalaConversions;

import kafka.coordinator.group.GroupMetadata;

/**
 * @author Andrei_Tytsik
 */
class ServerGroupMetadata implements GroupMetadataAdapter {

    private final GroupMetadata metadata;
    private final List<MemberMetadataAdapter> members;

    public ServerGroupMetadata(GroupMetadata metadata) {
        Validate.notNull(metadata, "Group metadata is null");

        this.metadata = metadata;
        this.members = ScalaConversions.asJavaList(metadata.allMemberMetadata()).stream().
                map(ServerMemberMetadata::new).
                collect(Collectors.toList());
    }

    @Override
    public String getGroupId() {
        return metadata.groupId();
    }

    @Override
    public Integer getCoordinator() {
        return null;
    }

    @Override
    public ConsumerGroupState getState() {
        return ScalaConversions.asJavaGroupState(metadata.currentState());
    }

    @Override
    public String getProtocolType() {
        return
                metadata.protocolType().isDefined() ?
                metadata.protocolType().get() :
                null;
    }

    @Override
    public String getPartitionAssignor() {
        return metadata.protocolName().getOrElse(() -> null);
    }

    @Override
    public List<MemberMetadataAdapter> getMembers() {
        return members;
    }

    public static ServerGroupMetadata ofNullable(GroupMetadata metadata) {
        return metadata != null ? new ServerGroupMetadata(metadata) : null;
    }

}
