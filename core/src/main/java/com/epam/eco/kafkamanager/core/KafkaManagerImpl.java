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
package com.epam.eco.kafkamanager.core;

import java.security.Principal;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.epam.eco.commons.kafka.ScalaConversions;
import com.epam.eco.kafkamanager.BrokerConfigUpdateParams;
import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataDeleteParams;
import com.epam.eco.kafkamanager.BrokerMetadataKey;
import com.epam.eco.kafkamanager.BrokerMetadataUpdateParams;
import com.epam.eco.kafkamanager.BrokerRepo;
import com.epam.eco.kafkamanager.BrokerSearchQuery;
import com.epam.eco.kafkamanager.ConsumerGroupDeleteTopicParams;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataDeleteParams;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataKey;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataUpdateParams;
import com.epam.eco.kafkamanager.ConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.ConsumerGroupRepo;
import com.epam.eco.kafkamanager.ConsumerGroupSearchQuery;
import com.epam.eco.kafkamanager.ConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.PermissionCreateParams;
import com.epam.eco.kafkamanager.PermissionDeleteParams;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataDeleteParams;
import com.epam.eco.kafkamanager.PermissionMetadataKey;
import com.epam.eco.kafkamanager.PermissionMetadataUpdateParams;
import com.epam.eco.kafkamanager.PermissionRepo;
import com.epam.eco.kafkamanager.PermissionSearchQuery;
import com.epam.eco.kafkamanager.ResourcePermissionDeleteParams;
import com.epam.eco.kafkamanager.SecurityContextAdapter;
import com.epam.eco.kafkamanager.TopicConfigUpdateParams;
import com.epam.eco.kafkamanager.TopicCreateParams;
import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.TopicMetadataDeleteParams;
import com.epam.eco.kafkamanager.TopicMetadataKey;
import com.epam.eco.kafkamanager.TopicMetadataUpdateParams;
import com.epam.eco.kafkamanager.TopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicPartitionsCreateParams;
import com.epam.eco.kafkamanager.TopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordCounterTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicRepo;
import com.epam.eco.kafkamanager.TopicSearchQuery;
import com.epam.eco.kafkamanager.TransactionInfo;
import com.epam.eco.kafkamanager.TransactionRepo;
import com.epam.eco.kafkamanager.TransactionSearchQuery;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import kafka.security.auth.Resource;

/**
 * @author Andrei_Tytsik
 */
public class KafkaManagerImpl implements KafkaManager {

    @Autowired
    private SecurityContextAdapter securityContext;
    @Autowired
    private BrokerRepo brokerRepo;
    @Autowired
    private TopicRepo topicRepo;
    @Autowired
    private ConsumerGroupRepo consumerGroupRepo;
    @Autowired
    private PermissionRepo permissionRepo;
    @Autowired
    private MetadataRepo metadataRepo;
    @Autowired
    private TransactionRepo transactionRepo;
    @Autowired
    private TopicRecordCounterTaskExecutor topicRecordCounterTaskExecutor;
    @Autowired
    private TopicOffsetFetcherTaskExecutor topicOffsetFetcherTaskExecutor;
    @Autowired
    private TopicPurgerTaskExecutor topicPurgerTaskExecutor;
    @Autowired
    private ConsumerGroupOffsetResetterTaskExecutor consumerGroupOffsetResetterTaskExecutor;
    @Autowired
    private ConsumerGroupTopicOffsetFetcherTaskExecutor consumerGroupTopicOffsetFetcherTaskExecutor;
    @Autowired
    private TopicRecordFetcherTaskExecutor<?, ?> topicRecordFetcherTaskExecutor;

    @Override
    public int getBrokerCount() {
        return brokerRepo.size();
    }

    @Override
    public boolean brokerExists(int brokerId) {
        return brokerRepo.contains(brokerId);
    }

    @Override
    public BrokerInfo getBroker(int brokerId) {
        return brokerRepo.get(brokerId);
    }

    @Override
    public List<BrokerInfo> getAllBrokers() {
        return brokerRepo.values();
    }

    @Override
    public List<BrokerInfo> getBrokers(BrokerSearchQuery query) {
        return brokerRepo.values(query);
    }

    @Override
    public Page<BrokerInfo> getBrokerPage(Pageable pageable) {
        return getBrokerPage(null, pageable);
    }

    @Override
    public Page<BrokerInfo> getBrokerPage(BrokerSearchQuery query, Pageable pageable) {
        return brokerRepo.page(query, pageable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrokerInfo updateBroker(BrokerMetadataUpdateParams params) {
        Validate.notNull(params, "BrokerMetadataUpdateParams object is null");

        Principal principal =  securityContext.getPrincipal();

        brokerRepo.get(params.getBrokerId()); // sanity check

        metadataRepo.createOrReplace(
                BrokerMetadataKey.with(params.getBrokerId()),
                Metadata.builder().
                    description(params.getDescription()).
                    attributes(params.getAttributes()).
                    updatedAtNow().
                    updatedBy(principal).
                    build());

        if (brokerRepo instanceof CachedRepo) {
            ((CachedRepo<Integer>)brokerRepo).evict(params.getBrokerId());
        }

        return brokerRepo.get(params.getBrokerId());
    }

    @SuppressWarnings("unchecked")
    @Override
    public BrokerInfo updateBroker(BrokerMetadataDeleteParams params) {
        Validate.notNull(params, "BrokerMetadataDeleteParams object is null");

        brokerRepo.get(params.getBrokerId()); // sanity check

        metadataRepo.delete(BrokerMetadataKey.with(params.getBrokerId()));

        if (brokerRepo instanceof CachedRepo) {
            ((CachedRepo<Integer>)brokerRepo).evict(params.getBrokerId());
        }

        return brokerRepo.get(params.getBrokerId());
    }

    @Override
    public BrokerInfo updateBroker(BrokerConfigUpdateParams params) {
        Validate.notNull(params, "BrokerConfigUpdateParams object is null");

        return brokerRepo.updateConfig(params.getBrokerId(), params.getConfig());
    }

    @Override
    public int getTopicCount() {
        return topicRepo.size();
    }

    @Override
    public boolean topicExists(String topicName) {
        return topicRepo.contains(topicName);
    }

    @Override
    public TopicInfo getTopic(String topicName) {
        return topicRepo.get(topicName);
    }

    @Override
    public List<TopicInfo> getAllTopics() {
        return topicRepo.values();
    }

    @Override
    public List<TopicInfo> getTopics(TopicSearchQuery query) {
        return topicRepo.values(query);
    }

    @Override
    public Page<TopicInfo> getTopicPage(Pageable pageable) {
        return getTopicPage(null, pageable);
    }

    @Override
    public Page<TopicInfo> getTopicPage(TopicSearchQuery query, Pageable pageable) {
        return topicRepo.page(query, pageable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TopicInfo createTopic(TopicCreateParams params) {
        Validate.notNull(params, "TopicCreateParams object is null");

        Principal principal =  securityContext.getPrincipal();

        topicRepo.create(
                params.getTopicName(),
                params.getPartitionCount(),
                params.getReplicationFactor(),
                params.getConfig());

        metadataRepo.createOrReplace(
                TopicMetadataKey.with(params.getTopicName()),
                Metadata.builder().
                    description(params.getDescription()).
                    attributes(params.getAttributes()).
                    updatedAtNow().
                    updatedBy(principal).
                    build());

        if (topicRepo instanceof CachedRepo) {
            ((CachedRepo<String>)topicRepo).evict(params.getTopicName());
        }

        return topicRepo.get(params.getTopicName());
    }

    @Override
    public TopicInfo updateTopic(TopicConfigUpdateParams params) {
        Validate.notNull(params, "TopicConfigUpdateParams object is null");

        return topicRepo.updateConfig(params.getTopicName(), params.getConfig());
    }

    @Override
    public TopicInfo updateTopic(TopicPartitionsCreateParams params) {
        Validate.notNull(params, "TopicPartitionsCreateParams object is null");

        return topicRepo.createPartitions(params.getTopicName(), params.getNewPartitionCount());
    }

    @SuppressWarnings("unchecked")
    @Override
    public TopicInfo updateTopic(TopicMetadataUpdateParams params) {
        Validate.notNull(params, "TopicMetadataUpdateParams object is null");

        Principal principal =  securityContext.getPrincipal();

        topicRepo.get(params.getTopicName()); // sanity check

        metadataRepo.createOrReplace(
                TopicMetadataKey.with(params.getTopicName()),
                Metadata.builder().
                    description(params.getDescription()).
                    attributes(params.getAttributes()).
                    updatedAtNow().
                    updatedBy(principal).
                    build());

        if (topicRepo instanceof CachedRepo) {
            ((CachedRepo<String>)topicRepo).evict(params.getTopicName());
        }

        return topicRepo.get(params.getTopicName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public TopicInfo updateTopic(TopicMetadataDeleteParams params) {
        Validate.notNull(params, "TopicMetadataDeleteParams object is null");

        topicRepo.get(params.getTopicName()); // sanity check

        metadataRepo.delete(TopicMetadataKey.with(params.getTopicName()));

        if (topicRepo instanceof CachedRepo) {
            ((CachedRepo<String>)topicRepo).evict(params.getTopicName());
        }

        return topicRepo.get(params.getTopicName());
    }

    @Override
    public void deleteTopic(String topicName) {
        topicRepo.get(topicName); // sanity check

        metadataRepo.delete(TopicMetadataKey.with(topicName));

        topicRepo.delete(topicName);
    }

    @Override
    public TopicRecordCounterTaskExecutor getTopicRecordCounterTaskExecutor() {
        return topicRecordCounterTaskExecutor;
    }

    @Override
    public TopicOffsetFetcherTaskExecutor getTopicOffsetFetcherTaskExecutor() {
        return topicOffsetFetcherTaskExecutor;
    }

    @Override
    public TopicPurgerTaskExecutor getTopicPurgerTaskExecutor() {
        return topicPurgerTaskExecutor;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> TopicRecordFetcherTaskExecutor<K, V> getTopicRecordFetcherTaskExecutor() {
        return (TopicRecordFetcherTaskExecutor<K, V>)topicRecordFetcherTaskExecutor;
    }

    @Override
    public int getConsumerGroupCount() {
        return consumerGroupRepo.size();
    }

    @Override
    public boolean consumerGroupExists(String groupName) {
        return consumerGroupRepo.contains(groupName);
    }

    @Override
    public ConsumerGroupInfo getConsumerGroup(String groupName) {
        return consumerGroupRepo.get(groupName);
    }

    @Override
    public List<ConsumerGroupInfo> getAllConsumerGroups() {
        return consumerGroupRepo.values();
    }

    @Override
    public List<ConsumerGroupInfo> getConsumerGroups(ConsumerGroupSearchQuery query) {
        return consumerGroupRepo.values(query);
    }

    @Override
    public Page<ConsumerGroupInfo> getConsumerGroupPage(Pageable pageable) {
        return getConsumerGroupPage(null, pageable);
    }

    @Override
    public Page<ConsumerGroupInfo> getConsumerGroupPage(ConsumerGroupSearchQuery query, Pageable pageable) {
        return consumerGroupRepo.page(query, pageable);
    }

    @Override
    public List<ConsumerGroupInfo> getConsumerGroupsForTopic(String topicName) {
        return consumerGroupRepo.groupsForTopic(topicName);
    }

    @Override
    public ConsumerGroupInfo updateConsumerGroup(ConsumerGroupDeleteTopicParams params) {
        Validate.notNull(params, "ConsumerGroupDeleteTopicParams object is null");

        return consumerGroupRepo.unassignGroupFromTopic(
                params.getGroupName(),
                params.getTopicName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConsumerGroupInfo updateConsumerGroup(ConsumerGroupMetadataUpdateParams params) {
        Validate.notNull(params, "ConsumerGroupMetadataUpdateParams object is null");

        Principal principal =  securityContext.getPrincipal();

        consumerGroupRepo.get(params.getGroupName()); // sanity check

        metadataRepo.createOrReplace(
                ConsumerGroupMetadataKey.with(params.getGroupName()),
                Metadata.builder().
                    description(params.getDescription()).
                    attributes(params.getAttributes()).
                    updatedAtNow().
                    updatedBy(principal).
                    build());

        if (consumerGroupRepo instanceof CachedRepo) {
            ((CachedRepo<String>)consumerGroupRepo).evict(params.getGroupName());
        }

        return consumerGroupRepo.get(params.getGroupName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public ConsumerGroupInfo updateConsumerGroup(ConsumerGroupMetadataDeleteParams params) {
        Validate.notNull(params, "ConsumerGroupMetadataDeleteParams object is null");

        consumerGroupRepo.get(params.getGroupName()); // sanity check

        metadataRepo.delete(ConsumerGroupMetadataKey.with(params.getGroupName()));

        if (consumerGroupRepo instanceof CachedRepo) {
            ((CachedRepo<String>)consumerGroupRepo).evict(params.getGroupName());
        }

        return consumerGroupRepo.get(params.getGroupName());
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        consumerGroupRepo.get(groupName); // sanity check
        consumerGroupRepo.deleteConsumerGroup(groupName);
        metadataRepo.delete(ConsumerGroupMetadataKey.with(groupName));
    }

    @Override
    public ConsumerGroupOffsetResetterTaskExecutor getConsumerGroupOffsetResetterTaskExecutor() {
        return consumerGroupOffsetResetterTaskExecutor;
    }

    @Override
    public ConsumerGroupTopicOffsetFetcherTaskExecutor getConsumerGroupTopicOffsetFetcherTaskExecutor() {
        return consumerGroupTopicOffsetFetcherTaskExecutor;
    }

    @Override
    public int getPermissionCount() {
        return permissionRepo.size();
    }

    @Override
    public List<PermissionInfo> getAllPermissions() {
        return permissionRepo.values();
    }

    @Override
    public List<PermissionInfo> getPermissions(PermissionSearchQuery query) {
        return permissionRepo.values(query);
    }

    @Override
    public Page<PermissionInfo> getPermissionPage(Pageable pageable) {
        return permissionRepo.page(null, pageable);
    }

    @Override
    public Page<PermissionInfo> getPermissionPage(PermissionSearchQuery query, Pageable pageable) {
        return permissionRepo.page(query, pageable);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createPermission(PermissionCreateParams params) {
        Validate.notNull(params, "PermissionCreateParams object is null");

        permissionRepo.create(
                params.getResourceType(),
                params.getResourceName(),
                params.getPrincipalObject(),
                params.getPermissionType(),
                params.getOperation(),
                params.getHost());

        if (permissionRepo instanceof CachedRepo) {
            ((CachedRepo<Resource>)permissionRepo).evict(ScalaConversions.asScalaResource(
                    params.getResourceType(),
                    params.getResourceName()));
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public void updatePermission(PermissionMetadataUpdateParams params) {
        Validate.notNull(params, "PermissionMetadataUpdateParams object is null");

        Principal principal =  securityContext.getPrincipal();

        metadataRepo.createOrReplace(
                PermissionMetadataKey.with(
                        params.getPrincipalObject(),
                        params.getResourceType(),
                        params.getResourceName()),
                Metadata.builder().
                    description(params.getDescription()).
                    attributes(params.getAttributes()).
                    updatedAtNow().
                    updatedBy(principal).
                    build());

        if (permissionRepo instanceof CachedRepo) {
            ((CachedRepo<Resource>)permissionRepo).evict(ScalaConversions.asScalaResource(
                    params.getResourceType(),
                    params.getResourceName()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updatePermission(PermissionMetadataDeleteParams params) {
        Validate.notNull(params, "PermissionMetadataDeleteParams object is null");

        metadataRepo.delete(
                PermissionMetadataKey.with(
                        params.getPrincipalObject(),
                        params.getResourceType(),
                        params.getResourceName()));

        if (permissionRepo instanceof CachedRepo) {
            ((CachedRepo<Resource>)permissionRepo).evict(ScalaConversions.asScalaResource(
                    params.getResourceType(),
                    params.getResourceName()));
        }
    }

    @Override
    public void deletePermission(PermissionDeleteParams params) {
        Validate.notNull(params, "PermissionDeleteParams object is null");

        if (isLastResourcePermissionDelete(params)) {
            metadataRepo.delete(PermissionMetadataKey.with(
                    params.getPrincipalObject(),
                    params.getResourceType(),
                    params.getResourceName()));
        }

        permissionRepo.delete(
                params.getResourceType(),
                params.getResourceName(),
                params.getPrincipalObject(),
                params.getPermissionType(),
                params.getOperation(),
                params.getHost());
    }

    private boolean isLastResourcePermissionDelete(PermissionDeleteParams params) {
        for (PermissionInfo permission : getAllPermissions()) {
            if (permissionHas(permission, params.getPrincipalObject(), params.getResourceType(),
                    params.getResourceName()) &&
                    (permission.getPermissionType() != params.getPermissionType() ||
                            !permission.getHost().equals(params.getHost()) ||
                            permission.getOperation() != params.getOperation())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void deletePermissions(ResourcePermissionDeleteParams params) {
        Validate.notNull(params, "PermissionDeleteParams object is null");

        metadataRepo.delete(
                PermissionMetadataKey.with(
                params.getPrincipalObject(),
                params.getResourceType(),
                params.getResourceName()));

        for (PermissionInfo permission : getAllPermissions()) {
            if (permissionHas(permission, params.getPrincipalObject(), params.getResourceType(),
                    params.getResourceName())) {
                permissionRepo.delete(
                        permission.getResourceType(),
                        permission.getResourceName(),
                        params.getPrincipalObject(),
                        permission.getPermissionType(),
                        permission.getOperation(),
                        permission.getHost());
            }
        }
    }

    private boolean permissionHas(PermissionInfo permission, KafkaPrincipal principalObject, ResourceType resourceType,
                                  String resourceName) {
        return permission.getKafkaPrincipal().equals(principalObject) &&
                permission.getResourceType() == resourceType &&
                permission.getResourceName().equals(resourceName);
    }

    @Override
    public int getTransactionCount() {
        return transactionRepo.size();
    }

    @Override
    public boolean transactionExists(String transactionalId) {
        return transactionRepo.contains(transactionalId);
    }

    @Override
    public TransactionInfo getTransaction(String transactionalId) {
        return transactionRepo.get(transactionalId);
    }

    @Override
    public List<TransactionInfo> getAllTransactions() {
        return transactionRepo.values();
    }

    @Override
    public List<TransactionInfo> getTransactions(TransactionSearchQuery query) {
        return transactionRepo.values(query);
    }

    @Override
    public Page<TransactionInfo> getTransactionPage(Pageable pageable) {
        return transactionRepo.page(pageable);
    }

    @Override
    public Page<TransactionInfo> getTransactionPage(
            TransactionSearchQuery query,
            Pageable pageable) {
        return transactionRepo.page(query, pageable);
    }

    @Override
    public List<TransactionInfo> getTransactionsForTopic(String topicName) {
        return transactionRepo.transactionsForTopic(topicName);
    }

}
