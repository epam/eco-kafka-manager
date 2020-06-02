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
package com.epam.eco.kafkamanager.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.ResourceType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.*;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=Config.class)
public class RestKafkaManagerIT {

    @Autowired
    private KafkaManager kafkaManager;

    @Test
    public void testBrokerOperations() {
        Assert.assertTrue("Number of brokers less or equals to 0", kafkaManager.getBrokerCount() > 0);

        List<BrokerInfo> brokers = kafkaManager.getAllBrokers();
        Assert.assertFalse("There is no brokers", brokers.isEmpty());

        BrokerInfo someBroker = kafkaManager.getBroker(brokers.get(0).getId());
        Assert.assertNotNull(someBroker);

        Assert.assertTrue("Broker does't exist", kafkaManager.brokerExists(brokers.get(0).getId()));

        Page<BrokerInfo> brokerPage = kafkaManager.getBrokerPage(
                BrokerSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assert.assertNotNull(brokerPage);
        Assert.assertTrue("There is no brokers", brokerPage.getContent().size() > 0);

        BrokerMetadataDeleteParams deleteParams = BrokerMetadataDeleteParams.builder()
                .brokerId(someBroker.getId())
                .build();
        kafkaManager.updateBroker(deleteParams);
        someBroker = kafkaManager.getBroker(brokers.get(0).getId());
        Assert.assertFalse("There is broker metadata after removing it", someBroker.getMetadata().isPresent());

        BrokerMetadataUpdateParams updateParams = BrokerMetadataUpdateParams.builder()
                .brokerId(someBroker.getId())
                .description("Description")
                .attributes(Collections.singletonMap("a", "b"))
                .build();
        kafkaManager.updateBroker(updateParams);
        someBroker = kafkaManager.getBroker(brokers.get(0).getId());
        Assert.assertTrue("There is no broker metadata after updating it", someBroker.getMetadata().isPresent());
    }

    @Test
    public void testTopicOperations() throws Exception {
        String topicName = "24_transactional_1";

        Assert.assertTrue("Topic doesn't exist", kafkaManager.topicExists(topicName));

        Map<TopicPartition, OffsetRange> offsetMap = kafkaManager.getTopicOffsetFetcherTaskExecutor().execute(topicName);
        Assert.assertNotNull(offsetMap);
        Assert.assertFalse("Offset map is empty", offsetMap.isEmpty());

        Map<Integer, Long> offsets = offsetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().partition(),
                        entry -> entry.getValue().getSmallest()));
        RecordFetchRequest recordFetchRequest = new RecordFetchRequest(
                RecordFetchRequest.DataFormat.STRING,
                RecordFetchRequest.DataFormat.STRING,
                offsets,
                10L,
                10000L);
        RecordFetchResult<String, String> recordFetchResult =
                kafkaManager.<String, String>getTopicRecordFetcherTaskExecutor()
                .execute(topicName, recordFetchRequest);
        Assert.assertNotNull(recordFetchResult);
        Assert.assertFalse("Fetched record list is empty", recordFetchResult.getRecords().isEmpty());

        Map<TopicPartition, OffsetTimeSeries> offsetTimeSeries = kafkaManager.getTopicOffsetFetcherTaskExecutor()
                .getOffsetTimeSeries(topicName);
        Assert.assertNotNull(offsetTimeSeries);
        Assert.assertFalse("Offset time series is empty", offsetTimeSeries.isEmpty());

        TaskResult<Long> topicRecordCount = kafkaManager.getTopicRecordCounterTaskExecutor()
                .executeDetailed(topicName);
        Assert.assertNotNull(topicRecordCount);
        Assert.assertTrue("Record count is 0", topicRecordCount.getValue() > 0);

        Future<TaskResult<Long>> topicRecordCountFuture = kafkaManager.getTopicRecordCounterTaskExecutor()
                .submitDetailed(topicName);
        topicRecordCount = topicRecordCountFuture.get();
        Assert.assertNotNull(topicRecordCount);
        Assert.assertTrue("Record count is 0", topicRecordCount.getValue() > 0);

        kafkaManager.getTopicPurgerTaskExecutor().execute(topicName);
        topicRecordCount = kafkaManager.getTopicRecordCounterTaskExecutor()
                .executeDetailed(topicName);
        Assert.assertNotNull(topicRecordCount);
        Assert.assertEquals(0, topicRecordCount.getValue().intValue());

        Assert.assertTrue("Number of topics less or equals to 0", kafkaManager.getTopicCount() > 0);

        List<TopicInfo> topics = kafkaManager.getAllTopics();
        Assert.assertFalse("There is no topics", topics.isEmpty());

        TopicInfo someTopic = kafkaManager.getTopic(topicName);
        Assert.assertNotNull(someTopic);

        Assert.assertTrue("Topic doesn't exist", kafkaManager.topicExists(topicName));

        Page<TopicInfo> topicPage = kafkaManager.getTopicPage(
                TopicSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assert.assertNotNull(topicPage);
        Assert.assertTrue("There is no topics", topicPage.getContent().size() > 0);

        TopicMetadataDeleteParams deleteParams = TopicMetadataDeleteParams.builder()
                .topicName(topicName)
                .build();
        kafkaManager.updateTopic(deleteParams);
        someTopic = kafkaManager.getTopic(topicName);
        Assert.assertFalse("There is topic metadata after removing it", someTopic.getMetadata().isPresent());

        TopicMetadataUpdateParams updateParams = TopicMetadataUpdateParams.builder()
                .topicName(topicName)
                .description("Description")
                .attributes(Collections.singletonMap("a", "b"))
                .build();
        kafkaManager.updateTopic(updateParams);
        someTopic = kafkaManager.getTopic(topicName);
        Assert.assertTrue("There is no topic metadata after updating it", someTopic.getMetadata().isPresent());

        List<ConsumerGroupInfo> topicConsumerGroups = kafkaManager.getConsumerGroupsForTopic(topicName);
        Assert.assertFalse("Topic consumer group list is empty", topicConsumerGroups.isEmpty());

        List<TransactionInfo> topicTransactions = kafkaManager.getTransactionsForTopic(topicName);
        Assert.assertFalse("Topic transaction list is empty", topicTransactions.isEmpty());

        String newTopicName = "rb-test";
        Assert.assertFalse("Topic exists", kafkaManager.topicExists(newTopicName));
        TopicCreateParams topicCreateParams = TopicCreateParams.builder()
                .topicName(newTopicName)
                .partitionCount(2)
                .replicationFactor(2)
                .appendAttribute("1", "1")
                .description("description")
                .build();
        kafkaManager.createTopic(topicCreateParams);
        Assert.assertTrue("Topic doesn't exist after creating it", kafkaManager.topicExists(newTopicName));

        TopicConfigUpdateParams topicConfigUpdateParams = TopicConfigUpdateParams.builder()
                .topicName(newTopicName)
                .config(Collections.singletonMap("cleanup.policy", "compact"))
                .build();
        kafkaManager.updateTopic(topicConfigUpdateParams);
        someTopic = kafkaManager.getTopic(newTopicName);
        Assert.assertEquals("compact", someTopic.getConfig().get("cleanup.policy"));

        TopicPartitionsCreateParams topicPartitionsCreateParams = TopicPartitionsCreateParams.builder()
                .topicName(newTopicName)
                .newPartitionCount(3)
                .build();
        kafkaManager.updateTopic(topicPartitionsCreateParams);
        someTopic = kafkaManager.getTopic(newTopicName);
        Assert.assertEquals(3, someTopic.getPartitionCount());

        kafkaManager.deleteTopic(newTopicName);
        Assert.assertFalse("Topic exists after deleting it", kafkaManager.topicExists(newTopicName));
    }

    @Test
    public void testConsumerGroupOperations() {
        String consumerGroup = "group_24_transactional";

        Assert.assertTrue("Consumer group doesn't exist", kafkaManager.consumerGroupExists(consumerGroup));

        Map<TopicPartition, OffsetRange> consumerGroupOffsets = kafkaManager.getConsumerGroupTopicOffsetFetcherTaskExecutor()
                .execute(consumerGroup);
        Assert.assertNotNull(consumerGroupOffsets);
        Assert.assertFalse("Offset map is empty", consumerGroupOffsets.isEmpty());

        Map<TopicPartition, Long> newOffsets = consumerGroupOffsets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getSmallest()));
        kafkaManager.getConsumerGroupOffsetResetterTaskExecutor()
                .execute(consumerGroup, newOffsets);
        ConsumerGroupInfo someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        Assert.assertNotNull(someConsumerGroup);
        for (Map.Entry<TopicPartition, Long> entry : someConsumerGroup.getOffsets().entrySet()) {
            TopicPartition partition = entry.getKey();
            Long offset = entry.getValue();
            Assert.assertEquals(newOffsets.get(partition), offset);
        }

        Assert.assertTrue("Number of consumer groups less or equals to 0", kafkaManager.getConsumerGroupCount() > 0);

        List<ConsumerGroupInfo> consumerGroups = kafkaManager.getAllConsumerGroups();
        Assert.assertFalse("There is no consumer groups", consumerGroups.isEmpty());

        someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        Assert.assertNotNull(someConsumerGroup);

        Assert.assertTrue("Consumer group doesn't exist", kafkaManager.consumerGroupExists(consumerGroup));

        Page<ConsumerGroupInfo> topicPage = kafkaManager.getConsumerGroupPage(
                ConsumerGroupSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assert.assertNotNull(topicPage);
        Assert.assertTrue("There is no consumer groups", topicPage.getContent().size() > 0);

        ConsumerGroupMetadataDeleteParams deleteParams = ConsumerGroupMetadataDeleteParams.builder()
                .groupName(consumerGroup)
                .build();
        kafkaManager.updateConsumerGroup(deleteParams);
        someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        Assert.assertFalse("There is consumer group metadata after removing it", someConsumerGroup.getMetadata().isPresent());

        ConsumerGroupMetadataUpdateParams updateParams = ConsumerGroupMetadataUpdateParams.builder()
                .groupName(consumerGroup)
                .description("Description")
                .attributes(Collections.singletonMap("a", "b"))
                .build();
        kafkaManager.updateConsumerGroup(updateParams);
        someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        Assert.assertTrue("There is no consumer group metadata after updating it", someConsumerGroup.getMetadata().isPresent());

        kafkaManager.deleteConsumerGroup(consumerGroup);
        Assert.assertFalse("Consumer group exists after delete", kafkaManager.consumerGroupExists(consumerGroup));
    }

    @Test
    public void testPermissionOperations() {
        String topicName = "24_transactional_1";
        String principal = "User:Raman_Babich@epam.com";

        Assert.assertTrue("Number of permissions less or equals to 0", kafkaManager.getPermissionCount() > 0);

        List<PermissionInfo> permissionInfoList = kafkaManager.getAllPermissions();
        Assert.assertFalse("There is no permissions", permissionInfoList.isEmpty());

        Page<PermissionInfo> permissionPage = kafkaManager.getPermissionPage(
                PermissionSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assert.assertNotNull(permissionPage);
        Assert.assertTrue("There is no permissions", permissionPage.getContent().size() > 0);

        List<PermissionInfo> principalPermissions = kafkaManager.getPermissions(
                PermissionSearchCriteria.builder()
                        .kafkaPrincipal(principal)
                        .build());
        PermissionCreateParams permissionCreateParams = PermissionCreateParams.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(topicName)
                .principal(principal)
                .permissionType(AclPermissionType.ALLOW)
                .operation(AclOperation.DESCRIBE)
                .host("*")
                .description("description")
                .attributes(Collections.singletonMap("a", "a"))
                .build();
        kafkaManager.createPermission(permissionCreateParams);

        PermissionMetadataUpdateParams permissionMetadataUpdateParams = PermissionMetadataUpdateParams.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(topicName)
                .principal(principal)
                .description("new description")
                .attributes(Collections.singletonMap("1", "1"))
                .build();
        kafkaManager.updatePermission(permissionMetadataUpdateParams);

        PermissionMetadataDeleteParams permissionMetadataDeleteParams = PermissionMetadataDeleteParams.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(topicName)
                .principal(principal)
                .build();
        kafkaManager.updatePermission(permissionMetadataDeleteParams);
        List<PermissionInfo> newPrincipalPermissions = kafkaManager.getPermissions(
                PermissionSearchCriteria.builder()
                        .kafkaPrincipal(principal)
                        .build());
        Assert.assertEquals(principalPermissions.size() + 1, newPrincipalPermissions.size());

        PermissionDeleteParams permissionDeleteParams = PermissionDeleteParams.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(topicName)
                .principal(principal)
                .permissionType(AclPermissionType.ALLOW)
                .operation(AclOperation.DESCRIBE)
                .host("*")
                .build();
        kafkaManager.deletePermission(permissionDeleteParams);
        newPrincipalPermissions = kafkaManager.getPermissions(
                PermissionSearchCriteria.builder()
                        .kafkaPrincipal(principal)
                        .build());
        Assert.assertEquals(principalPermissions.size(), newPrincipalPermissions.size());
    }

    @Test
    public void testTransactionOperations() {
        Assert.assertTrue("Number of transactions less or equals to 0", kafkaManager.getTransactionCount() > 0);

        List<TransactionInfo> transactionInfoList = kafkaManager.getAllTransactions();
        Assert.assertFalse("There is no transactions", transactionInfoList.isEmpty());

        Assert.assertTrue("Transaction does't exist", kafkaManager.transactionExists(transactionInfoList.get(0).getTransactionalId()));

        TransactionInfo someTransaction = kafkaManager.getTransaction(transactionInfoList.get(0).getTransactionalId());
        Assert.assertNotNull(someTransaction);

        Page<TransactionInfo> transactionPage = kafkaManager.getTransactionPage(
                TransactionSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assert.assertNotNull(transactionPage);
        Assert.assertTrue("There is no transactions", transactionPage.getContent().size() > 0);
    }

}
