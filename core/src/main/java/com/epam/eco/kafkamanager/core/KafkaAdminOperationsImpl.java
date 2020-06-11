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
package com.epam.eco.kafkamanager.core;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.commons.kafka.AdminClientUtils;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.core.autoconfigure.KafkaManagerProperties;

import kafka.server.KafkaConfig;

/**
 * @author Andrei_Tytsik
 */
public class KafkaAdminOperationsImpl implements KafkaAdminOperations {

    private static final int VERIFY_MAX_ATTEMPTS = 5;
    private static final int VERIFY_BACKOFF = 200;

    @Autowired
    private KafkaManagerProperties properties;

    private AdminClient adminClient;

    @PostConstruct
    private void init() {
        adminClient = AdminClient.create(properties.getCommonAdminClientConfig());
    }

    @PreDestroy
    private void destroy() {
        adminClient.close();
    }

    @Override
    public Config describeBrokerConfig(int brokerId) {
        return describeBrokerConfigs(Collections.singletonList(brokerId)).get(brokerId);
    }

    @Override
    public Map<Integer, Config> describeBrokerConfigs(Collection<Integer> brokerIds) {
        return AdminClientUtils.describeBrokerConfigs(adminClient, brokerIds);
    }

    @Override
    public Config describeTopicConfig(String topicName) {
        return describeTopicConfigs(Collections.singleton(topicName)).get(topicName);
    }

    @Override
    public Map<String, Config> describeTopicConfigs(Collection<String> topicNames) {
        return AdminClientUtils.describeTopicConfigs(adminClient, topicNames);
    }

    @Override
    public void createAcl(AclBinding aclBinding) {
        AdminClientUtils.createAcl(adminClient, aclBinding);
    }

    @Override
    public void deleteAcl(AclBindingFilter aclBindingFilter) {
        AdminClientUtils.deleteAcl(adminClient, aclBindingFilter);
    }

    @Override
    public void createTopic(
            String topicName,
            int partitionCount,
            int replicationFactor,
            Map<String, String> config) {
        AdminClientUtils.createTopic(
                adminClient,
                topicName,
                partitionCount,
                replicationFactor,
                config);
    }

    @Override
    public void createPartitions(String topicName, int newPartitionCount) {
        AdminClientUtils.createPartitions(adminClient, topicName, newPartitionCount);
    }

    @Override
    public void deleteTopic(String topicName) {
        AdminClientUtils.deleteTopic(adminClient, topicName);
    }

    @Override
    public void deleteAllRecords(String topicName) {
        AdminClientUtils.deleteAllRecords(adminClient, topicName);
    }

    @Deprecated
    @Override
    public void alterTopicConfig(String topicName, Map<String, String> configs) {
        AdminClientUtils.alterTopicConfig(adminClient, topicName, configs);
    }

    @Override
    public void alterTopicConfigs(String topicName, Map<String, String> configs) {
        AdminClientUtils.alterTopicConfigs(adminClient, topicName, configs);
    }

    @Override
    public boolean verifyTopicConfigsAltered(String topicName, Map<String, String> configs) {
        Validate.notBlank(topicName, "Topic name is blank");
        Validate.notEmpty(configs, "Map of configs is null or empty");

        int attempt = 0;
        while (attempt++ < VERIFY_MAX_ATTEMPTS) {
            Map<String, String> actualConfigs = AdminClientUtils.describeTopicConfigAsMap(adminClient, topicName);
            if (actualConfigs.entrySet().containsAll(configs.entrySet())) {
                return true;
            }

            try {
                Thread.sleep(VERIFY_BACKOFF);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(ie);
            }
        }

        return false;
    }

    @Override
    public void alterBrokerConfigs(int brokerId, Map<String, String> configs) {
        AdminClientUtils.alterBrokerConfigs(adminClient, brokerId, configs);
    }

    @Override
    public int getDefaultReplicationFactor() {
        return Integer.parseInt(
                AdminClientUtils.describeAnyBrokerConfigEntry(
                        adminClient,
                        KafkaConfig.DefaultReplicationFactorProp()).value());
    }

    @Override
    public Map<String, ConsumerGroupDescription> describeAllConsumerGroups() {
        return AdminClientUtils.describeAllConsumerGroups(adminClient);
    }

    @Override
    public Map<String, Map<TopicPartition, OffsetAndMetadata>> listAllConsumerGroupOffsets() {
        return AdminClientUtils.listAllConsumerGroupOffsets(adminClient);
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        AdminClientUtils.deleteConsumerGroup(adminClient, groupName);
    }

    @Override
    public String getZkConnect() {
        String zkConnect = properties.getZkConnect();
        if (StringUtils.isBlank(zkConnect)) {
            zkConnect = AdminClientUtils.describeAnyBrokerConfigEntry(
                    adminClient,
                    KafkaConfig.ZkConnectProp()).value();
        }
        return zkConnect;
    }

}
