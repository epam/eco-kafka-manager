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
package com.epam.eco.kafkamanager.core.autoconfigure;

import java.util.Map.Entry;

import javax.cache.CacheManager;
import javax.cache.Caching;

import org.apache.commons.collections4.MapUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryForever;
import org.apache.zookeeper.client.ZKClientConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

import com.epam.eco.kafkamanager.Authorizer;
import com.epam.eco.kafkamanager.BrokerRepo;
import com.epam.eco.kafkamanager.ConsumerGroupOffsetResetterTaskExecutor;
import com.epam.eco.kafkamanager.ConsumerGroupRepo;
import com.epam.eco.kafkamanager.ConsumerGroupTopicOffsetFetcherTaskExecutor;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.PermissionRepo;
import com.epam.eco.kafkamanager.SecurityContextAdapter;
import com.epam.eco.kafkamanager.TopicOffsetForTimeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicOffsetRangeFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicPurgerTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordCounterTaskExecutor;
import com.epam.eco.kafkamanager.TopicRecordFetcherTaskExecutor;
import com.epam.eco.kafkamanager.TopicRepo;
import com.epam.eco.kafkamanager.TransactionRepo;
import com.epam.eco.kafkamanager.core.KafkaAdminOperationsImpl;
import com.epam.eco.kafkamanager.core.KafkaManagerImpl;
import com.epam.eco.kafkamanager.core.SecurityContextAdapterImpl;
import com.epam.eco.kafkamanager.core.authz.PermitAllAuthorizer;
import com.epam.eco.kafkamanager.core.authz.kafka.KafkaAuthorizerConfiguration;
import com.epam.eco.kafkamanager.core.broker.repo.zk.ZkBrokerRepo;
import com.epam.eco.kafkamanager.core.consumer.exec.ConsumerGroupOffsetResetterTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.consumer.exec.ConsumerGroupTopicOffsetFetcherTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.consumer.repo.CompositeConsumerGroupRepo;
import com.epam.eco.kafkamanager.core.consumer.repo.kafka.KafkaConsumerGroupRepo;
import com.epam.eco.kafkamanager.core.consumer.repo.zk.ZkConsumerGroupRepo;
import com.epam.eco.kafkamanager.core.metadata.repo.kafka.KafkaMetadataRepo;
import com.epam.eco.kafkamanager.core.permission.repo.zk.ZkPermissionRepo;
import com.epam.eco.kafkamanager.core.spring.AsyncStartingBeanProcessor;
import com.epam.eco.kafkamanager.core.topic.exec.TopicOffsetForTimeFetcherTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.topic.exec.TopicOffsetRangeFetcherTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.topic.exec.TopicPurgerTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.topic.exec.TopicRecordCounterTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.topic.exec.TopicRecordFetcherTaskExecutorImpl;
import com.epam.eco.kafkamanager.core.topic.repo.zk.ZkTopicRepo;
import com.epam.eco.kafkamanager.core.txn.repo.kafka.KafkaTransactionRepo;
import com.epam.eco.kafkamanager.core.utils.RetriableZookeeperFactory;

/**
 * @author Andrei_Tytsik
 */
@Configuration
@EnableConfigurationProperties(KafkaManagerProperties.class)
@EnableGlobalMethodSecurity(prePostEnabled=true)
@Import(KafkaAuthorizerConfiguration.class)
public class KafkaManagerAutoConfiguration {

    @Autowired
    private KafkaManagerProperties properties;

    @Bean
    @ConditionalOnMissingBean
    public SecurityContextAdapter securityContextAdapter() {
        return new SecurityContextAdapterImpl();
    }

    @Bean
    public KafkaAdminOperations kafkaAdminOperations() {
        return new KafkaAdminOperationsImpl();
    }

    @Bean
    public KafkaManager kafkaManager() {
        return new KafkaManagerImpl();
    }

    @Bean
    public TopicRecordCounterTaskExecutor topicRecordCounterTaskExecutor() {
        return new TopicRecordCounterTaskExecutorImpl(cacheManager());
    }

    @Bean
    public TopicOffsetRangeFetcherTaskExecutor topicOffsetRangeFetcherTaskExecutor() {
        return new TopicOffsetRangeFetcherTaskExecutorImpl(cacheManager());
    }

    @Bean
    public TopicOffsetForTimeFetcherTaskExecutor topicOffsetForTimeFetcherTaskExecutor() {
        return new TopicOffsetForTimeFetcherTaskExecutorImpl();
    }

    @Bean
    public TopicPurgerTaskExecutor topicPurgerTaskExecutor() {
        return new TopicPurgerTaskExecutorImpl();
    }

    @Bean
    public TopicRecordFetcherTaskExecutor<?, ?> topicRecordFetcherTaskExecutor() {
        return new TopicRecordFetcherTaskExecutorImpl<>();
    }

    @Bean
    public ConsumerGroupOffsetResetterTaskExecutor consumerGroupOffsetResetterTaskExecutor() {
        return new ConsumerGroupOffsetResetterTaskExecutorImpl();
    }

    @Bean
    public ConsumerGroupTopicOffsetFetcherTaskExecutor consumerGroupTopicOffsetFetcherTaskExecutor() {
        return new ConsumerGroupTopicOffsetFetcherTaskExecutorImpl(cacheManager());
    }

    @Bean(destroyMethod="close")
    @ConditionalOnMissingBean
    public CacheManager cacheManager() {
        return Caching.getCachingProvider().getCacheManager();
    }

    @Bean
    public BrokerRepo brokerRepo() {
        return new ZkBrokerRepo();
    }

    @Bean
    public TopicRepo topicRepo() {
        return new ZkTopicRepo();
    }

    @Bean
    public ConsumerGroupRepo consumerGroupRepo() {
        return new CompositeConsumerGroupRepo();
    }

    @Bean("ZK")
    public ConsumerGroupRepo zkConsumerGroupRepo() {
        return new ZkConsumerGroupRepo();
    }

    @Bean("KF")
    public ConsumerGroupRepo kafkaConsumerGroupRepo() {
        return new KafkaConsumerGroupRepo();
    }

    @Bean
    public PermissionRepo permissionRepo() {
        return new ZkPermissionRepo();
    }

    @Bean
    public MetadataRepo metadataRepo() {
        return new KafkaMetadataRepo();
    }

    @Bean
    public TransactionRepo transactionRepo() {
        return new KafkaTransactionRepo();
    }

    @Bean(destroyMethod="close")
    public CuratorFramework curatorFramework(KafkaAdminOperations adminOperations) {
        ZKClientConfig clientConfig = null;
        if (MapUtils.isNotEmpty(properties.getZkClientConfig())) {
            clientConfig = new ZKClientConfig();
            for (Entry<String, String> entry : properties.getZkClientConfig().entrySet()) {
                clientConfig.setProperty(entry.getKey(), entry.getValue());
            }
        }

        CuratorFramework client = CuratorFrameworkFactory.builder().
                zkClientConfig(clientConfig).
                connectString(adminOperations.getZkConnect()).
                retryPolicy(new RetryForever(3000)).
                zookeeperFactory(new RetriableZookeeperFactory()).
                build();
        client.start();

        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    public Authorizer authorizer() {
        return PermitAllAuthorizer.INSTANCE;
    }

    @Bean
    public AsyncStartingBeanProcessor asyncStartingBeanProcessor() {
        return new AsyncStartingBeanProcessor();
    }

}
