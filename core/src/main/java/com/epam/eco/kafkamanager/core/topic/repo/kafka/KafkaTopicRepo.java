/*******************************************************************************
 *  Copyright 2025 EPAM Systems
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
package com.epam.eco.kafkamanager.core.topic.repo.kafka;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.admin.TopicListing;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.AlreadyExistsException;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.PartitionInfo;
import com.epam.eco.kafkamanager.SearchCriteria;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataKey;
import com.epam.eco.kafkamanager.TopicRepo;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import static com.epam.eco.commons.kafka.AdminClientUtils.configToMap;
import static com.epam.eco.kafkamanager.core.utils.WaitUtil.waitForCondition;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Implementation of TopicRepo that uses Kafka's AdminClient API to retrieve topic information
 * without relying on ZooKeeper. This implementation is compatible with Kafka's KRaft mode.
 *
 */
public class KafkaTopicRepo extends AbstractKeyValueRepo<String, TopicInfo,
        SearchCriteria<TopicInfo>> implements TopicRepo, CachedRepo<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;

    @Autowired
    private MetadataRepo metadataRepo;

    @Override
    public int size() {
        return adminOperations.listTopics().size();
    }

    @Override
    public boolean contains(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");
        return adminOperations.topicExists(topicName);
    }

    @Override
    public TopicInfo get(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        TopicInfo topicInfo = fetchTopicInfo(topicName);
        if (topicInfo == null) {
            throw new NotFoundException(String.format("Topic not found by name '%s'", topicName));
        }
        return topicInfo;
    }

    @Override
    public List<TopicInfo> values() {
        return fetchAllTopics();
    }

    @Override
    public List<TopicInfo> values(List<String> topicNames) {
        Validate.notNull(topicNames, "Topic names list is null");
        Validate.noNullElements(topicNames, "Topic names list has null elements");
        return fetchTopics(topicNames);
    }

    @Override
    public List<String> keys() {
        return adminOperations.listTopics().stream().map(TopicListing::name).toList();
    }

    @Override
    public TopicInfo create(
            String topicName,
            int partitionCount,
            int replicationFactor,
            Map<String, String> config
    ) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.isTrue(partitionCount > 0, "Partition count is invalid");
        Validate.isTrue(replicationFactor > 0, "Replication factor is invalid");

        try {
            if (contains(topicName)) {
                throw new AlreadyExistsException(String.format("Topic '%s' already exists",
                        topicName));
            }

            adminOperations.createTopic(topicName, partitionCount, replicationFactor, config);

            waitForCondition(() -> adminOperations.topicExists(topicName),
                    String.format("Waiting for operation create topic: %s",
                            topicName));

            return get(topicName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create topic", e);
        }
    }

    @Override
    public TopicInfo updateConfig(
            String topicName,
            Map<String, String> configs
    ) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.notNull(configs, "Map of configs is null");

        if (!contains(topicName)) {
            throw new NotFoundException(String.format("Topic '%s' doesn't exist", topicName));
        }

        adminOperations.alterTopicConfigs(topicName, configs);
        return get(topicName);

    }

    @Override
    public TopicInfo createPartitions(
            String topicName,
            int newPartitionCount
    ) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.isTrue(newPartitionCount > 0, "Partition count is invalid");

        TopicInfo currentTopic = get(topicName);
        int currentPartitionCount = currentTopic.getPartitions().size();

        if (newPartitionCount <= currentPartitionCount) {
            throw new IllegalArgumentException(String.format(
                    "New partition count (%d) for topic '%s' is less or equal than current " +
                            "one (%d)",
                    newPartitionCount, topicName, currentPartitionCount));
        }

        adminOperations.createPartitions(topicName, newPartitionCount);
        return get(topicName);

    }

    @Override
    public void delete(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        if (!contains(topicName)) {
            throw new NotFoundException(String.format("Topic '%s' doesn't exist", topicName));
        }

        adminOperations.deleteTopic(topicName);
    }

    @Override
    public void evict(String topicName) {
        // No-op since we're not caching anymore
    }

    private List<TopicInfo> fetchAllTopics() {
        return fetchTopics(Collections.emptyList());
    }

    private List<TopicInfo> fetchTopics(List<String> topicNames) {
        List<TopicInfo> result = new ArrayList<>();
        try {
            LOGGER.debug("Fetching topics {}", topicNames.size());
            List<String> topics = topicNames.isEmpty() ?
                    adminOperations.listTopics().stream().map(TopicListing::name).toList() :
                    topicNames;
            if (topics.isEmpty()) {
                LOGGER.debug("No topics found");
                return result;
            }

            Map<String, TopicDescription> topicDescriptions =
                    adminOperations.describeTopics(topics);

            Map<String, Config> topicConfigs =
                    adminOperations.describeTopicConfigs(topics);
            for (String topicName : topics) {
                try {
                    TopicDescription topicDescription = topicDescriptions.get(topicName);
                    Config config = topicConfigs.get(topicName);

                    Map<String, String> nonDefaultConfigMap = configToMapNonDefaultValues(config);
                    result.add(buildTopicInfo(topicDescription, nonDefaultConfigMap));
                } catch (Exception e) {
                    LOGGER.error("Failed to fetch topic info for topic {}", topicName, e);
                }
            }

            LOGGER.debug("{} topics fetched", result.size());
        } catch (Exception e) {
            LOGGER.error("Failed to fetch topics", e);
        }
        Collections.sort(result);
        return result;
    }

    private Map<String, String> configToMapNonDefaultValues(Config config) {
        return config.entries()
                .stream()
                .filter(this::nonDefault)
                .collect(toMap(ConfigEntry::name, ConfigEntry::value));
    }

    private boolean nonDefault(ConfigEntry entry) {
        return !(
                entry.value() == null ||
                entry.isDefault() ||
                entry.source().equals(ConfigEntry.ConfigSource.STATIC_BROKER_CONFIG)
        );
    }

    private TopicInfo fetchTopicInfo(String topicName) {

        try {
            TopicDescription topicDescription = adminOperations
                    .describeTopics(Collections.singleton(topicName))
                    .get(topicName);

            if (topicDescription == null) {
                return null;
            }
            Config config = adminOperations
                    .describeTopicConfig(topicName);

            Map<String, String> configMap = configToMap(config, false, false);
            updateNullValues(configMap);

            return buildTopicInfo(topicDescription, configMap);
        } catch (Exception e) {
            LOGGER.error("Failed to fetch topic info for topic {}.", topicName, e);
            return null;
        }
    }

    private void updateNullValues(Map<String, String> configMap) {
        configMap.entrySet().forEach(entry -> {
            if (entry.getValue() == null) {
                entry.setValue(EMPTY);
            }
        });
    }

    private TopicInfo buildTopicInfo(
            TopicDescription topicDescription,
            Map<String, String> config
    ) {
        Map<TopicPartition, PartitionInfo> partitions = new HashMap<>();

        for (TopicPartitionInfo partitionInfo : topicDescription.partitions()) {
            TopicPartition topicPartition = new TopicPartition(
                    topicDescription.name(),
                    partitionInfo.partition());

            List<Integer> replicas = partitionInfo.replicas().stream()
                    .map(Node::id)
                    .collect(Collectors.toList());

            List<Integer> isr = partitionInfo.isr().stream()
                    .map(Node::id)
                    .collect(Collectors.toList());

            Integer leader = partitionInfo.leader() != null ?
                    partitionInfo.leader().id() : null;

            partitions.put(
                    topicPartition,
                    PartitionInfo.builder()
                            .id(topicPartition)
                            .replicas(replicas)
                            .leader(leader)
                            .isr(isr)
                            .build());
        }

        return TopicInfo.builder()
                .name(topicDescription.name())
                .partitions(partitions)
                .config(config)
                .metadata(metadataRepo.get(TopicMetadataKey.with(topicDescription.name())))
                .build();
    }

}
