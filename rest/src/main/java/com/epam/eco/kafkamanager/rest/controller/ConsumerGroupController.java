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
package com.epam.eco.kafkamanager.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.epam.eco.kafkamanager.ConsumerGroupDeleteTopicParams;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataDeleteParams;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataUpdateParams;
import com.epam.eco.kafkamanager.ConsumerGroupSearchQuery;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.core.utils.PageUtils;
import com.epam.eco.kafkamanager.rest.request.MetadataRequest;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/consumer-groups")
public class ConsumerGroupController {

    @Autowired
    private KafkaManager kafkaManager;

    @GetMapping
    public Page<ConsumerGroupInfo> getConsumerGroupPage(
            @RequestParam(value = "groupName", required = false) String groupName,
            @RequestParam(value = "storageType", required = false) ConsumerGroupInfo.StorageType storageType,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Pageable pageable = PageUtils.buildPageableWithDefaultsIfNull(page, pageSize);
        ConsumerGroupSearchQuery query = ConsumerGroupSearchQuery.builder()
                .groupName(groupName)
                .storageType(storageType)
                .description(description)
                .build();
        return kafkaManager.getConsumerGroupPage(query, pageable);
    }

    @GetMapping("/{groupName}")
    public ConsumerGroupInfo getConsumerGroup(@PathVariable("groupName") String groupName) {
        return kafkaManager.getConsumerGroup(groupName);
    }

    @PutMapping("/{groupName}/metadata")
    public ConsumerGroupInfo putConsumerGroupMetadata(
            @PathVariable("groupName") String groupName,
            @RequestBody MetadataRequest request) {
        ConsumerGroupMetadataUpdateParams params = ConsumerGroupMetadataUpdateParams.builder()
                .groupName(groupName)
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        return kafkaManager.updateConsumerGroup(params);
    }

    @DeleteMapping("/{groupName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConsumerGroup(@PathVariable("groupName") String groupName) {
        kafkaManager.deleteConsumerGroup(groupName);
    }

    @DeleteMapping("/{groupName}/metadata")
    public ConsumerGroupInfo deleteConsumerGroupMetadata(@PathVariable("groupName") String groupName) {
        ConsumerGroupMetadataDeleteParams params = ConsumerGroupMetadataDeleteParams.builder()
                .groupName(groupName)
                .build();
        return kafkaManager.updateConsumerGroup(params);
    }

    @DeleteMapping("/{groupName}/topics/{topicName}")
    public ConsumerGroupInfo deleteConsumerGroupTopic(
            @PathVariable("groupName") String groupName,
            @PathVariable("topicName") String topicName) {
        ConsumerGroupDeleteTopicParams params = ConsumerGroupDeleteTopicParams.builder()
                .groupName(groupName)
                .topicName(topicName)
                .build();
        return kafkaManager.updateConsumerGroup(params);
    }

}
