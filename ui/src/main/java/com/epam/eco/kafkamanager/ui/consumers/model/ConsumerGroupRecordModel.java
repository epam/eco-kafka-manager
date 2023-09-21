/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.consumers.model;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonGetter;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.Metadata;

/**
 * @author Mikhail_Vershkov
 */

public class ConsumerGroupRecordModel implements Serializable {
    private final String name;
    private final String storage;
    private final String state;
    private final List<String> topics;
    private final List<String> members;
    private final List<String> offsets;
    private final String description;

    public ConsumerGroupRecordModel(ConsumerGroupInfo consumerGroupInfo) {
        this.name = consumerGroupInfo.getName();
        this.storage = StringUtils.capitalize(consumerGroupInfo.getStorageType().name().toLowerCase());
        this.state = consumerGroupInfo.getState().toString();
        this.topics = consumerGroupInfo.getTopicNames();
        this.members = getMembersAsHtml(consumerGroupInfo);
        this.offsets = getOffsetsAsHtml(consumerGroupInfo);
        this.description = getDescription(consumerGroupInfo);
    }

    public List<String> getMembersAsHtml(ConsumerGroupInfo groupInfo) {
        return groupInfo.getMembers().stream()
                .map(m -> String.format("%s(<b>%s</b>)", m.getClientId(), m.getClientHost() != null ? m.getClientHost() : ""))
                .collect(Collectors.toList());
    }

    public List<String> getOffsetsAsHtml(ConsumerGroupInfo groupInfo) {
        return groupInfo.getOffsets().entrySet().stream()
                .map(e -> String.format("%s = <b>%s</b>", e.getKey(), e.getValue() != null ? e.getValue() : "N/A"))
                .collect(Collectors.toList());
    }

    public String getDescription(ConsumerGroupInfo groupInfo) {
        return groupInfo.getMetadata().map(Metadata::getDescription).orElse(null);
    }

    @JsonGetter("name")
    public String getName() {
        return name;
    }

    @JsonGetter("storage")
    public String getStorage() {
        return storage;
    }

    @JsonGetter("state")
    public String getState() {
        return state;
    }

    @JsonGetter("topics")
    public List<String> getTopics() {
        return topics;
    }

    @JsonGetter("members")
    public List<String> getMembers() {
        return members;
    }

    @JsonGetter("offsets")
    public List<String> getOffsets() {
        return offsets;
    }

    @JsonGetter("description")
    public String getDescription() {
        return description;
    }
}
