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

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicConfigUpdateParams;
import com.epam.eco.kafkamanager.TopicCreateParams;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataDeleteParams;
import com.epam.eco.kafkamanager.TopicMetadataUpdateParams;
import com.epam.eco.kafkamanager.TopicPartitionsCreateParams;
import com.epam.eco.kafkamanager.TopicSearchCriteria;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.core.utils.PageUtils;
import com.epam.eco.kafkamanager.rest.request.MetadataRequest;
import com.epam.eco.kafkamanager.rest.request.TopicConfigRequest;
import com.epam.eco.kafkamanager.rest.request.TopicPartitionsRequest;
import com.epam.eco.kafkamanager.rest.request.TopicRequest;

/**
 * @author Raman_Babich
 */
@RestController
@RequestMapping("/api/topics")
public class TopicController {

    @Autowired
    private KafkaManager kafkaManager;

    @GetMapping
    public Page<TopicInfo> getTopicsPage(
            @RequestParam(value = "topicName", required = false) String topicName,
            @RequestParam(value = "minPartitionCount", required = false) Integer minPartitionCount,
            @RequestParam(value = "maxPartitionCount", required = false) Integer maxPartitionCount,
            @RequestParam(value = "minReplicationFactor", required = false) Integer minReplicationFactor,
            @RequestParam(value = "maxReplicationFactor", required = false) Integer maxReplicationFactor,
            @RequestParam(value = "minConsumerCount", required = false) Integer minConsumerCount,
            @RequestParam(value = "maxConsumerCount", required = false) Integer maxConsumerCount,
            @RequestParam(value = "replicationState", required = false) TopicSearchCriteria.ReplicationState replicationState,
            @RequestParam(value = "configString", required = false) String configString,
            @RequestParam(value = "configMap", required = false) Map<String, String> configMap,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "pageSize", required = false) Integer pageSize) {
        Pageable pageable = PageUtils.buildPageableWithDefaultsIfNull(page, pageSize);
        TopicSearchCriteria query = TopicSearchCriteria.builder()
                .topicName(topicName)
                .minPartitionCount(minPartitionCount)
                .maxPartitionCount(maxPartitionCount)
                .minReplicationFactor(minReplicationFactor)
                .maxReplicationFactor(maxReplicationFactor)
                .minConsumerCount(minConsumerCount)
                .maxConsumerCount(maxConsumerCount)
                .replicationState(replicationState)
                .configString(configString)
                .configMap(configMap)
                .description(description)
                .build();
        return kafkaManager.getTopicPage(query, pageable);
    }

    @GetMapping("/{topicName}")
    public TopicInfo getTopic(@PathVariable("topicName") String topicName) {
        return kafkaManager.getTopic(topicName);
    }

    @PostMapping
    public TopicInfo postTopic(@RequestBody TopicRequest request) {
        TopicCreateParams query = TopicCreateParams.builder()
                .topicName(request.getTopicName())
                .partitionCount(request.getPartitionCount())
                .replicationFactor(request.getReplicationFactor())
                .config(request.getConfig())
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        return kafkaManager.createTopic(query);
    }

    @DeleteMapping("/{topicName}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTopic(@PathVariable("topicName") String topicName) {
        kafkaManager.deleteTopic(topicName);
    }

    @PutMapping("/{topicName}/configs")
    public TopicInfo putTopicConfigs(
            @PathVariable("topicName") String topicName,
            @RequestBody TopicConfigRequest request) {
        TopicConfigUpdateParams query = TopicConfigUpdateParams.builder()
                .topicName(topicName)
                .config(request.getConfig())
                .build();
        return kafkaManager.updateTopic(query);
    }

    @PutMapping("/{topicName}/partitions")
    public TopicInfo putTopicPartitions(
            @PathVariable("topicName") String topicName,
            @RequestBody TopicPartitionsRequest request) {
        TopicPartitionsCreateParams query = TopicPartitionsCreateParams.builder()
                .topicName(topicName)
                .newPartitionCount(request.getNewPartitionCount())
                .build();
        return kafkaManager.updateTopic(query);
    }

    @PutMapping("/{topicName}/metadata")
    public TopicInfo putTopicMetadata(
            @PathVariable("topicName") String topicName,
            @RequestBody MetadataRequest request) {
        TopicMetadataUpdateParams query = TopicMetadataUpdateParams.builder()
                .topicName(topicName)
                .description(request.getDescription())
                .attributes(request.getAttributes())
                .build();
        return kafkaManager.updateTopic(query);
    }

    @DeleteMapping("/{topicName}/metadata")
    public TopicInfo deleteTopicMetadata(@PathVariable("topicName") String topicName) {
        TopicMetadataDeleteParams query = TopicMetadataDeleteParams.builder()
                .topicName(topicName)
                .build();
        return kafkaManager.updateTopic(query);
    }

    @GetMapping("/{topicName}/consumer-groups")
    public List<ConsumerGroupInfo> getTopicConsumerGroups(@PathVariable("topicName") String topicName) {
        return kafkaManager.getConsumerGroupsForTopic(topicName);
    }

    @GetMapping("/{topicName}/transactions")
    public List<TransactionInfo> getTopicTransactions(@PathVariable("topicName") String topicName) {
        return kafkaManager.getTransactionsForTopic(topicName);
    }

}
