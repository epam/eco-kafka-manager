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

import java.util.Collection;
import java.util.Map;

import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;

/**
 * @author Andrei_Tytsik
 */
public interface KafkaAdminOperations {

    Config describeBrokerConfig(int brokerId);
    Map<Integer, Config> describeBrokerConfigs(Collection<Integer> brokerIds);
    Config describeTopicConfig(String topicName);
    Map<String, Config> describeTopicConfigs(Collection<String> topicNames);
    void createAcl(AclBinding aclBinding);
    void deleteAcl(AclBindingFilter aclBindingFilter);
    void createTopic(
            String topicName,
            int partitionCount,
            int replicationFactor,
            Map<String, String> config);
    void createPartitions(String topicName, int newPartitionCount);
    void deleteTopic(String topicName);
    void deleteAllRecords(String topicName);

    /**
     * @deprecated use {#link {@link #alterTopicConfigs(String, Map)}}
     */
    @Deprecated
    void alterTopicConfig(String topicName, Map<String, String> configs);

    void alterTopicConfigs(String topicName, Map<String, String> configs);

    /**
     * Workaround to guarantee consistency between consequent AdminClient calls.
     *
     * Example case that randomly fails w/o the verification:
     *  1 alter config cleanup.policy=delete
     *  2 delete records (AdminClient fails with PolicyViolationException as cleanup.policy != delete)
     */
    boolean verifyTopicConfigsAltered(String topicName, Map<String, String> configs);

    void alterBrokerConfigs(int brokerId, Map<String, String> configs);

    int getDefaultReplicationFactor();
    Map<String, ConsumerGroupDescription> describeAllConsumerGroups();
    Map<String, Map<TopicPartition, OffsetAndMetadata>> listAllConsumerGroupOffsets();
    void deleteConsumerGroup(String groupName);
    String getZkConnect();

}
