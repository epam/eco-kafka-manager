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
package com.epam.eco.kafkamanager;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * @author Andrei_Tytsik
 */
public interface KafkaManager {

    int getBrokerCount();
    boolean brokerExists(int brokerId);
    BrokerInfo getBroker(int brokerId);
    List<BrokerInfo> getAllBrokers();
    List<BrokerInfo> getBrokers(BrokerSearchCriteria criteria);
    Page<BrokerInfo> getBrokerPage(Pageable pageable);
    Page<BrokerInfo> getBrokerPage(BrokerSearchCriteria criteria, Pageable pageable);
    BrokerInfo updateBroker(BrokerMetadataUpdateParams params);
    BrokerInfo updateBroker(BrokerMetadataDeleteParams params);
    BrokerInfo updateBroker(BrokerConfigUpdateParams params);

    int getTopicCount();
    boolean topicExists(String topicName);
    TopicInfo getTopic(String topicName);
    List<TopicInfo> getAllTopics();
    List<TopicInfo> getTopics(TopicSearchCriteria criteria);
    Page<TopicInfo> getTopicPage(Pageable pageable);
    Page<TopicInfo> getTopicPage(TopicSearchCriteria criteria, Pageable pageable);
    TopicInfo createTopic(TopicCreateParams params);
    TopicInfo updateTopic(TopicConfigUpdateParams params);
    TopicInfo updateTopic(TopicPartitionsCreateParams params);
    TopicInfo updateTopic(TopicMetadataUpdateParams params);
    TopicInfo updateTopic(TopicMetadataDeleteParams params);
    void deleteTopic(String topicName);
    TopicRecordCounterTaskExecutor getTopicRecordCounterTaskExecutor();
    @Deprecated
    TopicOffsetFetcherTaskExecutor getTopicOffsetFetcherTaskExecutor();
    TopicOffsetRangeFetcherTaskExecutor getTopicOffsetRangeFetcherTaskExecutor();
    TopicOffsetForTimeFetcherTaskExecutor getTopicOffsetForTimeFetcherTaskExecutor();
    TopicPurgerTaskExecutor getTopicPurgerTaskExecutor();
    <K, V> TopicRecordFetcherTaskExecutor<K, V> getTopicRecordFetcherTaskExecutor();

    int getConsumerGroupCount();
    boolean consumerGroupExists(String groupName);
    ConsumerGroupInfo getConsumerGroup(String groupName);
    List<ConsumerGroupInfo> getAllConsumerGroups();
    List<ConsumerGroupInfo> getConsumerGroups(ConsumerGroupSearchCriteria criteria);
    Page<ConsumerGroupInfo> getConsumerGroupPage(Pageable pageable);
    Page<ConsumerGroupInfo> getConsumerGroupPage(ConsumerGroupSearchCriteria criteria, Pageable pageable);
    List<ConsumerGroupInfo> getConsumerGroupsForTopic(String topicName);
    ConsumerGroupInfo updateConsumerGroup(ConsumerGroupDeleteTopicParams params);
    ConsumerGroupInfo updateConsumerGroup(ConsumerGroupMetadataUpdateParams params);
    ConsumerGroupInfo updateConsumerGroup(ConsumerGroupMetadataDeleteParams params);
    void deleteConsumerGroup(String groupName);
    ConsumerGroupOffsetResetterTaskExecutor getConsumerGroupOffsetResetterTaskExecutor();
    ConsumerGroupTopicOffsetFetcherTaskExecutor getConsumerGroupTopicOffsetFetcherTaskExecutor();

    int getPermissionCount();
    List<PermissionInfo> getAllPermissions();
    List<PermissionInfo> getPermissions(PermissionSearchCriteria criteria);
    Page<PermissionInfo> getPermissionPage(Pageable pageable);
    Page<PermissionInfo> getPermissionPage(PermissionSearchCriteria criteria, Pageable pageable);
    List<PermissionInfo> getPermissionsOfResource(ResourcePermissionFilter filter);
    void createPermission(PermissionCreateParams params);
    void updatePermission(PermissionMetadataUpdateParams params);
    void updatePermission(PermissionMetadataDeleteParams params);
    void deletePermissions(ResourcePermissionsDeleteParams params);
    void deletePermissions(PrincipalPermissionsDeleteParams params);

    int getTransactionCount();
    boolean transactionExists(String transactionalId);
    TransactionInfo getTransaction(String transactionalId);
    List<TransactionInfo> getAllTransactions();
    List<TransactionInfo> getTransactions(TransactionSearchCriteria criteria);
    Page<TransactionInfo> getTransactionPage(Pageable pageable);
    Page<TransactionInfo> getTransactionPage(TransactionSearchCriteria criteria, Pageable pageable);
    List<TransactionInfo> getTransactionsForTopic(String topicName);

}
