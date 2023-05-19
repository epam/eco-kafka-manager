/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.common.ConsumerGroupState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.epam.eco.kafkamanager.utils.TopicInfoGenerator;

import static com.epam.eco.kafkamanager.utils.TopicSearchCriteriaUtils.CONFIG_MAP_COMPACT;
import static com.epam.eco.kafkamanager.utils.TopicSearchCriteriaUtils.CONFIG_MAP_DELETE;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Mikhail_Vershkov
 */

public class TopicListSearchTest {

    private final static Map<String,TopicInfoGenerator.RecordConfig> TOPIC_LIST_CONFIG =
           Stream.of(
                   new TopicInfoGenerator.RecordConfig("testTopic", 3, 3, CONFIG_MAP_COMPACT, "description like test a string", true),
                   new TopicInfoGenerator.RecordConfig("testTopicLike", 2, 0, CONFIG_MAP_DELETE, "", false),
                   new TopicInfoGenerator.RecordConfig("testTopicContains", 3, 0, CONFIG_MAP_COMPACT, "description like test an string", true),
                   new TopicInfoGenerator.RecordConfig("testTopicEquals", 2, 3, CONFIG_MAP_COMPACT, "description equals", true),
                   new TopicInfoGenerator.RecordConfig("testTopicLikeACatAnimal", 5, 2, CONFIG_MAP_DELETE, "", false),
                   new TopicInfoGenerator.RecordConfig("testTopicLikeAMonkeyAnimal", 3, 2, CONFIG_MAP_COMPACT, "", false),
                   new TopicInfoGenerator.RecordConfig("testTopicContainsElephant", 10, 2, CONFIG_MAP_COMPACT, "description like test appoint string", true),
                   new TopicInfoGenerator.RecordConfig("testTopicContainsMascotAnimal", 2, 3, CONFIG_MAP_COMPACT, "description test string contains", true),
                   new TopicInfoGenerator.RecordConfig("testTopicEqualsBabun", 3, 1, CONFIG_MAP_DELETE, "description equals", false),
                   new TopicInfoGenerator.RecordConfig("testTopicContainsChicken", 8, 3, CONFIG_MAP_DELETE, "description test string contains", true),
                   new TopicInfoGenerator.RecordConfig("testTopicLikeABoarAnimal", 12, 3, CONFIG_MAP_COMPACT, "description equals", false)
                    ).collect(Collectors.toMap(TopicInfoGenerator.RecordConfig::getTopicName, Function.identity()));

    private static final KafkaManager kafkaManager = Mockito.mock(KafkaManager.class);
    private TopicListSearchCriteriaImpl criteria;


    @BeforeAll
    public static void beforeAll() {
        Mockito.when(kafkaManager.getConsumerGroupsForTopic(anyString()))
               .thenAnswer(invocationOnMock -> {
                   List<ConsumerGroupInfo> groupInfos = new LinkedList<>();
                   ConsumerGroupInfo groupInfo = new ConsumerGroupInfo(
                           invocationOnMock.getArgument(0),
                           0,
                           ConsumerGroupState.STABLE,
                           "TCP",
                           "partitionAssignor",
                           List.of(),
                           Map.of(),
                           Map.of(),
                           ConsumerGroupInfo.StorageType.KAFKA,
                           new Metadata("test description", Map.of(), new Date(), "User"));
                   groupInfos.add( groupInfo );
                   groupInfos.add( groupInfo );
                   groupInfos.add( groupInfo );

                   return groupInfos;
               });
    }

    @Test
    public void testManyLikeClauses() {
        Map<String, String> filterMap = Map.of(
                "topicName_LIKE","%testTopicLike%Animal%",
                "partitionCount_GREATER","1",
                "partitionCount_LESS","5",
                "replicationFactor_EQUALS", "2",
                "configString_EQUALS", "cleanup.policy: compact");
        criteria = TopicListSearchCriteriaImpl.fromJsonWith(filterMap, kafkaManager);
        List<TopicInfo> topicInfos = TopicInfoGenerator.generate(TOPIC_LIST_CONFIG).values().stream()
                                          .filter(topicInfo -> criteria.matches(topicInfo)).toList();
        Assertions.assertEquals(1, topicInfos.size());
        Assertions.assertEquals("testTopicLikeAMonkeyAnimal", topicInfos.listIterator().next().getName());
    }

    @Test
    public void testManyContainsClauses() {
        Map<String, String> filterMap = Map.of(
                "topicName_CONTAINS","Contains",
                "partitionCount_GREATER","1",
                "partitionCount_LESS","5",
                "replicationFactor_EQUALS", "3",
                "configString_EQUALS", "cleanup.policy: compact");
        criteria = TopicListSearchCriteriaImpl.fromJsonWith(filterMap, kafkaManager);
        List<TopicInfo> topicInfos = TopicInfoGenerator.generate(TOPIC_LIST_CONFIG).values().stream()
                                                       .filter(topicInfo -> criteria.matches(topicInfo)).toList();
        Assertions.assertEquals(1, topicInfos.size());
        Assertions.assertEquals("testTopicContainsMascotAnimal", topicInfos.listIterator().next().getName());
    }

    @Test
    public void testManyEqualsClauses() {
        Map<String, String> filterMap = Map.of(
                "topicName_EQUALS","testTopicEqualsBabun",
                "partitionCount_GREATER","0",
                "partitionCount_LESS","5",
                "replicationFactor_EQUALS", "1",
                "replicationFactor_GREATER", "0",
                "replicationFactor_LESS", "100",
                "configString_EQUALS", "cleanup.policy: delete");
        criteria = TopicListSearchCriteriaImpl.fromJsonWith(filterMap, kafkaManager);
        List<TopicInfo> topicInfos = TopicInfoGenerator.generate(TOPIC_LIST_CONFIG).values().stream()
                                                       .filter(topicInfo -> criteria.matches(topicInfo)).toList();
        Assertions.assertEquals(1, topicInfos.size());
        Assertions.assertEquals("testTopicEqualsBabun", topicInfos.listIterator().next().getName());
    }

    @Test
    public void testDescriptionLikeClauses() {
        Map<String, String> filterMap = Map.of(
                "topicName_LIKE","testTopic%",
                "partitionCount_GREATER","0",
                "partitionCount_LESS","5",
                "replicationFactor_GREATER", "0",
                "replicationFactor_LESS", "100",
                "configString_EQUALS", "cleanup.policy: compact",
                "description_NOT_EMPTY","",
                "description_LIKE","%test%string%");
        criteria = TopicListSearchCriteriaImpl.fromJsonWith(filterMap, kafkaManager);
        List<TopicInfo> topicInfos = TopicInfoGenerator.generate(TOPIC_LIST_CONFIG).values().stream()
                                                       .filter(topicInfo -> criteria.matches(topicInfo)).toList();
        Assertions.assertEquals(2, topicInfos.size());
        Iterator<TopicInfo> results = topicInfos.listIterator();
        Assertions.assertEquals("testTopicContainsMascotAnimal", results.next().getName());
        Assertions.assertEquals("testTopic", results.next().getName());
    }
}