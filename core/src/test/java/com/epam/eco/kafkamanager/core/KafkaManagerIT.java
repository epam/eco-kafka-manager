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
package com.epam.eco.kafkamanager.core;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.TopicInfo;

/**
 * @author Andrei_Tytsik
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes=Config.class)
public class KafkaManagerIT {

    @Autowired
    private KafkaManager kafkaManager;

    @Test
    public void testAllBrokersAreResolved() throws Exception {
        List<BrokerInfo> brokers = kafkaManager.getAllBrokers();
        Assertions.assertNotNull(brokers);
        for (BrokerInfo broker : brokers) {
            Assertions.assertNotNull(broker);
            Assertions.assertTrue(broker.getId() >= 0);
            Assertions.assertFalse(broker.getEndPoints().isEmpty());
        }
    }

    @Test
    public void testBrokerPageIsResolved() throws Exception {
        Page<BrokerInfo> page = kafkaManager.getBrokerPage(PageRequest.of(0, 1));
        Assertions.assertNotNull(page);
        for (BrokerInfo broker : page) {
            Assertions.assertNotNull(broker);
            Assertions.assertTrue(broker.getId() >= 0);
            Assertions.assertNotNull(broker.getEndPoints());
            Assertions.assertFalse(broker.getEndPoints().isEmpty());
        }
    }

    @Test
    public void testAllTopicsAreResolved() throws Exception {
        List<TopicInfo> topics = kafkaManager.getAllTopics();
        Assertions.assertNotNull(topics);
        for (TopicInfo topic : topics) {
            Assertions.assertNotNull(topic);
            Assertions.assertNotNull(topic.getName());
            Assertions.assertTrue(topic.getPartitionCount() >= 0);
            Assertions.assertTrue(topic.getReplicationFactor() >= 0);
        }
    }

    @Test
    public void testTopicPageIsResolved() throws Exception {
        Page<TopicInfo> page = kafkaManager.getTopicPage(PageRequest.of(0, 1));
        Assertions.assertNotNull(page);
        for (TopicInfo topic : page) {
            Assertions.assertNotNull(topic);
            Assertions.assertNotNull(topic.getName());
            Assertions.assertTrue(topic.getPartitionCount() >= 0);
            Assertions.assertTrue(topic.getReplicationFactor() >= 0);
        }
    }

    @Test
    public void testAllConsumerGroupsAreResolved() throws Exception {
        List<ConsumerGroupInfo> groups = kafkaManager.getAllConsumerGroups();
        Assertions.assertNotNull(groups);
        for (ConsumerGroupInfo group : groups) {
            Assertions.assertNotNull(group);
            Assertions.assertNotNull(group.getName());
            Assertions.assertNotNull(group.getTopicNames());
        }
    }

    @Test
    public void testConsumerGroupPageIsResolved() throws Exception {
        Page<ConsumerGroupInfo> page = kafkaManager.getConsumerGroupPage(PageRequest.of(0, 1));
        Assertions.assertNotNull(page);
        for (ConsumerGroupInfo group : page) {
            Assertions.assertNotNull(group);
            Assertions.assertNotNull(group.getName());
            Assertions.assertNotNull(group.getTopicNames());
        }
    }

    @Test
    public void testAllPermissionsAreResolved() throws Exception {
        List<PermissionInfo> permissions = kafkaManager.getAllPermissions();
        Assertions.assertNotNull(permissions);
        for (PermissionInfo permission : permissions) {
            Assertions.assertNotNull(permission);
            Assertions.assertNotNull(permission.getResourceType());
            Assertions.assertNotNull(permission.getResourceName());
            Assertions.assertNotNull(permission.getKafkaPrincipal());
            Assertions.assertNotNull(permission.getPermissionType());
            Assertions.assertNotNull(permission.getOperation());
            Assertions.assertNotNull(permission.getHost());
        }
    }

    @Test
    public void testPermissionPageIsResolved() throws Exception {
        Page<PermissionInfo> page = kafkaManager.getPermissionPage(PageRequest.of(0, 1));
        Assertions.assertNotNull(page);
        for (PermissionInfo permission : page) {
            Assertions.assertNotNull(permission);
            Assertions.assertNotNull(permission.getResourceType());
            Assertions.assertNotNull(permission.getResourceName());
            Assertions.assertNotNull(permission.getKafkaPrincipal());
            Assertions.assertNotNull(permission.getPermissionType());
            Assertions.assertNotNull(permission.getOperation());
            Assertions.assertNotNull(permission.getHost());
        }
    }

}
