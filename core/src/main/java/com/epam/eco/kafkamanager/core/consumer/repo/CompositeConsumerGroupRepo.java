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
package com.epam.eco.kafkamanager.core.consumer.repo;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Resource;

import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupRepo;
import com.epam.eco.kafkamanager.NotFoundException;
import com.epam.eco.kafkamanager.SearchCriteria;
import com.epam.eco.kafkamanager.repo.AbstractKeyValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

/**
 * @author Andrei_Tytsik
 */
public class CompositeConsumerGroupRepo extends AbstractKeyValueRepo<String, ConsumerGroupInfo, SearchCriteria<ConsumerGroupInfo>> implements ConsumerGroupRepo, CachedRepo<String> {

    @Resource(name="ZK")
    private ConsumerGroupRepo zkConsumerGroupRepo;

    @Resource(name="KF")
    private ConsumerGroupRepo kafkaConsumerGroupRepo;

    @Override
    public int size() {
        return zkConsumerGroupRepo.size() + kafkaConsumerGroupRepo.size();
    }

    @Override
    public boolean contains(String groupName) {
        return
                kafkaConsumerGroupRepo.contains(groupName) ||
                zkConsumerGroupRepo.contains(groupName);
    }

    @Override
    public ConsumerGroupInfo get(String groupName) {
        try {
            return kafkaConsumerGroupRepo.get(groupName);
        } catch (RuntimeException ex) {
            return zkConsumerGroupRepo.get(groupName);
        }
    }

    @Override
    public List<ConsumerGroupInfo> values() {
        List<ConsumerGroupInfo> groupInfos = new ArrayList<>();
        groupInfos.addAll(kafkaConsumerGroupRepo.values());
        groupInfos.addAll(zkConsumerGroupRepo.values());
        return groupInfos;
    }

    @Override
    public List<ConsumerGroupInfo> values(List<String> groupNames) {
        Validate.noNullElements(groupNames, "Group names list can't be null or contain null elements");

        List<ConsumerGroupInfo> groupInfos = new ArrayList<>();
        for (String groupName : groupNames) {
            if (kafkaConsumerGroupRepo.contains(groupName)) {
                groupInfos.add(kafkaConsumerGroupRepo.get(groupName));
            } else if (zkConsumerGroupRepo.contains(groupName)) {
                groupInfos.add(zkConsumerGroupRepo.get(groupName));
            }
        }
        return groupInfos;
    }

    @Override
    public List<String> keys() {
        List<String> groupNames = new ArrayList<>();
        groupNames.addAll(kafkaConsumerGroupRepo.keys());
        groupNames.addAll(zkConsumerGroupRepo.keys());
        return groupNames;
    }

    @Override
    public List<ConsumerGroupInfo> groupsForTopic(String topicName) {
        Validate.notBlank(topicName, "Topic name can't be blank");

        List<ConsumerGroupInfo> groupsByTopicName = new ArrayList<>();
        groupsByTopicName.addAll(kafkaConsumerGroupRepo.groupsForTopic(topicName));
        groupsByTopicName.addAll(zkConsumerGroupRepo.groupsForTopic(topicName));
        return groupsByTopicName;
    }

    @Override
    public ConsumerGroupInfo unassignGroupFromTopic(String groupName, String topicName) {
        Validate.notBlank(groupName, "Group name can't be blank");
        Validate.notBlank(topicName, "Topic name can't be blank");

        if (kafkaConsumerGroupRepo.contains(groupName)) {
            return kafkaConsumerGroupRepo.unassignGroupFromTopic(groupName, topicName);
        } else if (zkConsumerGroupRepo.contains(groupName)) {
            return zkConsumerGroupRepo.unassignGroupFromTopic(groupName, topicName);
        } else {
            throw new NotFoundException(String.format("Consumer group '%s' doesn't exist", groupName));
        }
    }

    @Override
    public void deleteConsumerGroup(String groupName) {
        Validate.notBlank(groupName, "Group name can't be blank");

        if (kafkaConsumerGroupRepo.contains(groupName)) {
            kafkaConsumerGroupRepo.deleteConsumerGroup(groupName);
        } else if (zkConsumerGroupRepo.contains(groupName)) {
            zkConsumerGroupRepo.deleteConsumerGroup(groupName);
        } else {
            throw new NotFoundException(String.format("Consumer group '%s' doesn't exist", groupName));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void evict(String groupName) {
        if (kafkaConsumerGroupRepo instanceof CachedRepo) {
            ((CachedRepo<String>)kafkaConsumerGroupRepo).evict(groupName);
        }

        if (zkConsumerGroupRepo instanceof CachedRepo) {
            ((CachedRepo<String>)zkConsumerGroupRepo).evict(groupName);
        }
    }

}
