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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataDeleteParams;
import com.epam.eco.kafkamanager.BrokerMetadataUpdateParams;
import com.epam.eco.kafkamanager.BrokerSearchCriteria;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataDeleteParams;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataUpdateParams;
import com.epam.eco.kafkamanager.ConsumerGroupSearchCriteria;
import com.epam.eco.kafkamanager.FetchMode;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.OffsetTimeSeries;
import com.epam.eco.kafkamanager.PermissionCreateParams;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataDeleteParams;
import com.epam.eco.kafkamanager.PermissionMetadataUpdateParams;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;
import com.epam.eco.kafkamanager.ResourcePermissionsDeleteParams;
import com.epam.eco.kafkamanager.TopicConfigUpdateParams;
import com.epam.eco.kafkamanager.TopicCreateParams;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataDeleteParams;
import com.epam.eco.kafkamanager.TopicMetadataUpdateParams;
import com.epam.eco.kafkamanager.TopicPartitionsCreateParams;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicSearchCriteria;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.TransactionSearchCriteria;
import com.epam.eco.kafkamanager.exec.TaskResult;

/**
 * @author Andrei_Tytsik
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=Config.class)
public class RestKafkaManagerIT {

    @Autowired
    private KafkaManager kafkaManager;

    @Test
    public void testBrokerOperations() {
        Assertions.assertTrue(kafkaManager.getBrokerCount() > 0, "Number of brokers less or equals to 0");

        List<BrokerInfo> brokers = kafkaManager.getAllBrokers();
        Assertions.assertFalse(brokers.isEmpty(), "There is no brokers");

        BrokerInfo someBroker = kafkaManager.getBroker(brokers.get(0).getId());
        Assertions.assertNotNull(someBroker);

        Assertions.assertTrue(kafkaManager.brokerExists(brokers.get(0).getId()), "Broker does't exist");

        Page<BrokerInfo> brokerPage = kafkaManager.getBrokerPage(
                BrokerSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assertions.assertNotNull(brokerPage);
        Assertions.assertTrue(brokerPage.getContent().size() > 0,"There is no brokers");

        BrokerMetadataDeleteParams deleteParams = BrokerMetadataDeleteParams.builder()
                .brokerId(someBroker.getId())
                .build();
        kafkaManager.updateBroker(deleteParams);
        someBroker = kafkaManager.getBroker(brokers.get(0).getId());
        Assertions.assertFalse(someBroker.getMetadata().isPresent(),"There is broker metadata after removing it");

        BrokerMetadataUpdateParams updateParams = BrokerMetadataUpdateParams.builder()
                .brokerId(someBroker.getId())
                .description("Description")
                .attributes(Collections.singletonMap("a", "b"))
                .build();
        kafkaManager.updateBroker(updateParams);
        someBroker = kafkaManager.getBroker(brokers.get(0).getId());
        Assertions.assertTrue(someBroker.getMetadata().isPresent(), "There is no broker metadata after updating it");
    }

    @Test
    public void testTopicOperations() throws Exception {
        String topicName = "24_transactional_1";

        assertTrue("Topic doesn't exist", kafkaManager.topicExists(topicName));

        Map<TopicPartition, OffsetRange> offsetMap = kafkaManager.getTopicOffsetRangeFetcherTaskExecutor().execute(topicName);
        Assertions.assertNotNull(offsetMap);
        assertFalse("Offset map is empty", offsetMap.isEmpty());

        Map<Integer, OffsetRange> offsets = offsetMap.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().partition(),
                        Map.Entry::getValue));
        TopicRecordFetchParams recordFetchParams = new TopicRecordFetchParams(
                TopicRecordFetchParams.DataFormat.STRING,
                TopicRecordFetchParams.DataFormat.STRING,
                offsets,
                10L,
                10000L,
                FetchMode.FETCH_FORWARD,
                0,
                false,
                60L);
        RecordFetchResult<String, String> recordFetchResult =
                kafkaManager.<String, String>getTopicRecordFetcherTaskExecutor()
                .execute(topicName, recordFetchParams);
        Assertions.assertNotNull(recordFetchResult);
        assertFalse("Fetched record list is empty", recordFetchResult.getRecords().isEmpty());

        Map<TopicPartition, OffsetTimeSeries> offsetTimeSeries = kafkaManager.getTopicOffsetRangeFetcherTaskExecutor()
                .getOffsetTimeSeries(topicName);
        Assertions.assertNotNull(offsetTimeSeries);
        assertFalse("Offset time series is empty", offsetTimeSeries.isEmpty());

        TaskResult<Long> topicRecordCount = kafkaManager.getTopicRecordCounterTaskExecutor()
                .executeDetailed(topicName);
        Assertions.assertNotNull(topicRecordCount);
        assertTrue("Record count is 0", topicRecordCount.getValue() > 0);

        Future<TaskResult<Long>> topicRecordCountFuture = kafkaManager.getTopicRecordCounterTaskExecutor()
                .submitDetailed(topicName);
        topicRecordCount = topicRecordCountFuture.get();
        Assertions.assertNotNull(topicRecordCount);
        assertTrue("Record count is 0", topicRecordCount.getValue() > 0);

        kafkaManager.getTopicPurgerTaskExecutor().execute(topicName);
        topicRecordCount = kafkaManager.getTopicRecordCounterTaskExecutor()
                .executeDetailed(topicName);
        Assertions.assertNotNull(topicRecordCount);
        Assertions.assertEquals(0, topicRecordCount.getValue().intValue());

        assertTrue("Number of topics less or equals to 0", kafkaManager.getTopicCount() > 0);

        List<TopicInfo> topics = kafkaManager.getAllTopics();
        assertFalse("There is no topics", topics.isEmpty());

        TopicInfo someTopic = kafkaManager.getTopic(topicName);
        Assertions.assertNotNull(someTopic);

        assertTrue("Topic doesn't exist", kafkaManager.topicExists(topicName));

        Page<TopicInfo> topicPage = kafkaManager.getTopicPage(
                TopicSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assertions.assertNotNull(topicPage);
        assertTrue("There is no topics", topicPage.getContent().size() > 0);

        TopicMetadataDeleteParams deleteParams = TopicMetadataDeleteParams.builder()
                .topicName(topicName)
                .build();
        kafkaManager.updateTopic(deleteParams);
        someTopic = kafkaManager.getTopic(topicName);
        assertFalse("There is topic metadata after removing it", someTopic.getMetadata().isPresent());

        TopicMetadataUpdateParams updateParams = TopicMetadataUpdateParams.builder()
                .topicName(topicName)
                .description("Description")
                .attributes(Collections.singletonMap("a", "b"))
                .build();
        kafkaManager.updateTopic(updateParams);
        someTopic = kafkaManager.getTopic(topicName);
        assertTrue("There is no topic metadata after updating it", someTopic.getMetadata().isPresent());

        List<ConsumerGroupInfo> topicConsumerGroups = kafkaManager.getConsumerGroupsForTopic(topicName);
        assertFalse("Topic consumer group list is empty", topicConsumerGroups.isEmpty());

        List<TransactionInfo> topicTransactions = kafkaManager.getTransactionsForTopic(topicName);
        assertFalse("Topic transaction list is empty", topicTransactions.isEmpty());

        String newTopicName = "rb-test";
        assertFalse("Topic exists", kafkaManager.topicExists(newTopicName));
        TopicCreateParams topicCreateParams = TopicCreateParams.builder()
                .topicName(newTopicName)
                .partitionCount(2)
                .replicationFactor(2)
                .appendAttribute("1", "1")
                .description("description")
                .build();
        kafkaManager.createTopic(topicCreateParams);
        assertTrue("Topic doesn't exist after creating it", kafkaManager.topicExists(newTopicName));

        TopicConfigUpdateParams topicConfigUpdateParams = TopicConfigUpdateParams.builder()
                .topicName(newTopicName)
                .config(Collections.singletonMap("cleanup.policy", "compact"))
                .build();
        kafkaManager.updateTopic(topicConfigUpdateParams);
        someTopic = kafkaManager.getTopic(newTopicName);
        Assertions.assertEquals("compact", someTopic.getConfig().get("cleanup.policy"));

        TopicPartitionsCreateParams topicPartitionsCreateParams = TopicPartitionsCreateParams.builder()
                .topicName(newTopicName)
                .newPartitionCount(3)
                .build();
        kafkaManager.updateTopic(topicPartitionsCreateParams);
        someTopic = kafkaManager.getTopic(newTopicName);
        Assertions.assertEquals(3, someTopic.getPartitionCount());

        kafkaManager.deleteTopic(newTopicName);
        assertFalse("Topic exists after deleting it", kafkaManager.topicExists(newTopicName));
    }

    @Test
    public void testConsumerGroupOperations() {
        String consumerGroup = "group_24_transactional";

        assertTrue("Consumer group doesn't exist", kafkaManager.consumerGroupExists(consumerGroup));

        Map<TopicPartition, OffsetRange> consumerGroupOffsets = kafkaManager.getConsumerGroupTopicOffsetFetcherTaskExecutor()
                .execute(consumerGroup);
        Assertions.assertNotNull(consumerGroupOffsets);
        assertFalse("Offset map is empty", consumerGroupOffsets.isEmpty());

        Map<TopicPartition, Long> newOffsets = consumerGroupOffsets.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().getSmallest()));
        kafkaManager.getConsumerGroupOffsetResetterTaskExecutor()
                .execute(consumerGroup, newOffsets);
        ConsumerGroupInfo someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        Assertions.assertNotNull(someConsumerGroup);
        for (Map.Entry<TopicPartition, Long> entry : someConsumerGroup.getOffsets().entrySet()) {
            TopicPartition partition = entry.getKey();
            Long offset = entry.getValue();
            Assertions.assertEquals(newOffsets.get(partition), offset);
        }

        assertTrue("Number of consumer groups less or equals to 0", kafkaManager.getConsumerGroupCount() > 0);

        List<ConsumerGroupInfo> consumerGroups = kafkaManager.getAllConsumerGroups();
        assertFalse("There is no consumer groups", consumerGroups.isEmpty());

        someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        Assertions.assertNotNull(someConsumerGroup);

        assertTrue("Consumer group doesn't exist", kafkaManager.consumerGroupExists(consumerGroup));

        Page<ConsumerGroupInfo> topicPage = kafkaManager.getConsumerGroupPage(
                ConsumerGroupSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assertions.assertNotNull(topicPage);
        assertTrue("There is no consumer groups", topicPage.getContent().size() > 0);

        ConsumerGroupMetadataDeleteParams deleteParams = ConsumerGroupMetadataDeleteParams.builder()
                .groupName(consumerGroup)
                .build();
        kafkaManager.updateConsumerGroup(deleteParams);
        someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        assertFalse("There is consumer group metadata after removing it", someConsumerGroup.getMetadata().isPresent());

        ConsumerGroupMetadataUpdateParams updateParams = ConsumerGroupMetadataUpdateParams.builder()
                .groupName(consumerGroup)
                .description("Description")
                .attributes(Collections.singletonMap("a", "b"))
                .build();
        kafkaManager.updateConsumerGroup(updateParams);
        someConsumerGroup = kafkaManager.getConsumerGroup(consumerGroup);
        assertTrue("There is no consumer group metadata after updating it", someConsumerGroup.getMetadata().isPresent());

        kafkaManager.deleteConsumerGroup(consumerGroup);
        assertFalse("Consumer group exists after delete", kafkaManager.consumerGroupExists(consumerGroup));
    }

    @Test
    public void testPermissionOperations() {
        String topicName = "24_transactional_1";
        String principal = "User:Raman_Babich@epam.com";

        assertTrue("Number of permissions less or equals to 0", kafkaManager.getPermissionCount() > 0);

        List<PermissionInfo> permissionInfoList = kafkaManager.getAllPermissions();
        assertFalse("There is no permissions", permissionInfoList.isEmpty());

        Page<PermissionInfo> permissionPage = kafkaManager.getPermissionPage(
                PermissionSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assertions.assertNotNull(permissionPage);
        assertTrue("There is no permissions", permissionPage.getContent().size() > 0);

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
        Assertions.assertEquals(principalPermissions.size() + 1, newPrincipalPermissions.size());

        ResourcePermissionFilter permissionDeleteFilter = ResourcePermissionFilter.builder()
                .resourceType(ResourceType.TOPIC)
                .resourceName(topicName)
                .principalFilter(principal)
                .permissionTypeFilter(AclPermissionType.ALLOW)
                .operationFilter(AclOperation.DESCRIBE)
                .hostFilter("*")
                .build();
        kafkaManager.deletePermissions(new ResourcePermissionsDeleteParams(permissionDeleteFilter));
        newPrincipalPermissions = kafkaManager.getPermissions(
                PermissionSearchCriteria.builder()
                        .kafkaPrincipal(principal)
                        .build());
        Assertions.assertEquals(principalPermissions.size(), newPrincipalPermissions.size());
    }

    @Test
    public void testTransactionOperations() {
        assertTrue("Number of transactions less or equals to 0", kafkaManager.getTransactionCount() > 0);

        List<TransactionInfo> transactionInfoList = kafkaManager.getAllTransactions();
        assertFalse("There is no transactions", transactionInfoList.isEmpty());

        assertTrue("Transaction does't exist", kafkaManager.transactionExists(transactionInfoList.get(0).getTransactionalId()));

        TransactionInfo someTransaction = kafkaManager.getTransaction(transactionInfoList.get(0).getTransactionalId());
        Assertions.assertNotNull(someTransaction);

        Page<TransactionInfo> transactionPage = kafkaManager.getTransactionPage(
                TransactionSearchCriteria.builder().build(),
                PageRequest.of(0, 10));
        Assertions.assertNotNull(transactionPage);
        assertTrue("There is no transactions", transactionPage.getContent().size() > 0);
    }

    private void assertTrue(String errorText, boolean result) {
        Assertions.assertTrue(result,errorText);
    }
    private void assertFalse(String errorText, boolean result) {
        Assertions.assertFalse(result,errorText);
    }

}
