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

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;

import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.TopicInfo;

/**
 * @author Andrei_Tytsik
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes=Config.class)
public class KafkaManagerIT {

    @Autowired
    private KafkaManager kafkaManager;

    @Test
    public void testAllBrokersAreResolved() throws Exception {
        List<BrokerInfo> brokers = kafkaManager.getAllBrokers();
        Assert.assertNotNull(brokers);
        for (BrokerInfo broker : brokers) {
            Assert.assertNotNull(broker);
            Assert.assertTrue(broker.getId() >= 0);
            Assert.assertFalse(broker.getEndPoints().isEmpty());
        }
    }

    @Test
    public void testBrokerPageIsResolved() throws Exception {
        Page<BrokerInfo> page = kafkaManager.getBrokerPage(PageRequest.of(0, 1));
        Assert.assertNotNull(page);
        for (BrokerInfo broker : page) {
            Assert.assertNotNull(broker);
            Assert.assertTrue(broker.getId() >= 0);
            Assert.assertNotNull(broker.getEndPoints());
            Assert.assertFalse(broker.getEndPoints().isEmpty());
        }
    }

    @Test
    public void testAllTopicsAreResolved() throws Exception {
        List<TopicInfo> topics = kafkaManager.getAllTopics();
        Assert.assertNotNull(topics);
        for (TopicInfo topic : topics) {
            Assert.assertNotNull(topic);
            Assert.assertNotNull(topic.getName());
            Assert.assertTrue(topic.getPartitionCount() >= 0);
            Assert.assertTrue(topic.getReplicationFactor() >= 0);
        }
    }

    @Test
    public void testTopicPageIsResolved() throws Exception {
        Page<TopicInfo> page = kafkaManager.getTopicPage(PageRequest.of(0, 1));
        Assert.assertNotNull(page);
        for (TopicInfo topic : page) {
            Assert.assertNotNull(topic);
            Assert.assertNotNull(topic.getName());
            Assert.assertTrue(topic.getPartitionCount() >= 0);
            Assert.assertTrue(topic.getReplicationFactor() >= 0);
        }
    }

    @Test
    public void testAllConsumerGroupsAreResolved() throws Exception {
        List<ConsumerGroupInfo> groups = kafkaManager.getAllConsumerGroups();
        Assert.assertNotNull(groups);
        for (ConsumerGroupInfo group : groups) {
            Assert.assertNotNull(group);
            Assert.assertNotNull(group.getName());
            Assert.assertNotNull(group.getTopicNames());
        }
    }

    @Test
    public void testConsumerGroupPageIsResolved() throws Exception {
        Page<ConsumerGroupInfo> page = kafkaManager.getConsumerGroupPage(PageRequest.of(0, 1));
        Assert.assertNotNull(page);
        for (ConsumerGroupInfo group : page) {
            Assert.assertNotNull(group);
            Assert.assertNotNull(group.getName());
            Assert.assertNotNull(group.getTopicNames());
        }
    }

    @Test
    public void testAllPermissionsAreResolved() throws Exception {
        List<PermissionInfo> permissions = kafkaManager.getAllPermissions();
        Assert.assertNotNull(permissions);
        for (PermissionInfo permission : permissions) {
            Assert.assertNotNull(permission);
            Assert.assertNotNull(permission.getResourceType());
            Assert.assertNotNull(permission.getResourceName());
            Assert.assertNotNull(permission.getKafkaPrincipal());
            Assert.assertNotNull(permission.getPermissionType());
            Assert.assertNotNull(permission.getOperation());
            Assert.assertNotNull(permission.getHost());
        }
    }

    @Test
    public void testPermissionPageIsResolved() throws Exception {
        Page<PermissionInfo> page = kafkaManager.getPermissionPage(PageRequest.of(0, 1));
        Assert.assertNotNull(page);
        for (PermissionInfo permission : page) {
            Assert.assertNotNull(permission);
            Assert.assertNotNull(permission.getResourceType());
            Assert.assertNotNull(permission.getResourceName());
            Assert.assertNotNull(permission.getKafkaPrincipal());
            Assert.assertNotNull(permission.getPermissionType());
            Assert.assertNotNull(permission.getOperation());
            Assert.assertNotNull(permission.getHost());
        }
    }

}
