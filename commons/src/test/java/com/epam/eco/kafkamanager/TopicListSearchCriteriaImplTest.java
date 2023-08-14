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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.kafka.common.ConsumerGroupState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.epam.eco.kafkamanager.utils.TopicSearchCriteriaUtils;

import static com.epam.eco.kafkamanager.utils.TopicSearchCriteriaUtils.CONFIG_MAP_COMPACT;
import static com.epam.eco.kafkamanager.utils.TopicSearchCriteriaUtils.CONFIG_MAP_DELETE;
import static org.mockito.ArgumentMatchers.anyString;

/**
 * @author Mikhail_Vershkov
 */

public class TopicListSearchCriteriaImplTest {

    private static final String STAFF_ROLES_TOPIC_NAME = "topicNameStaffByRoles";
    private static final String STAFF_TOPIC_NAME = "topicNameStaff";
    private static final String TOPIC_NAME_CONTAINS = "StaffByRoles";
    private static final String CONSUMER_GROUPS_TEST_TOPIC = "testTopic";
    private static final String DESCRIPTION = "topic description";
    private static final String DESCRIPTION_TEST = "topic test the string description";
    private static final KafkaManager kafkaManager = Mockito.mock(KafkaManager.class);

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
                           new Metadata(DESCRIPTION,Map.of(),new Date(),"User"));
                   groupInfos.add( groupInfo );
                   groupInfos.add( groupInfo );
                   groupInfos.add( groupInfo );

                   return groupInfos;
               });
    }

    @Test
    public void topicNameLikeTest() {
        Map<String, String> filterMap = Map.of("topicName_LIKE","%staff%roles%");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils
                .generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void topicNameEqualsTest() {
        Map<String, String> filterMap = Map.of("topicName_EQUALS", STAFF_TOPIC_NAME);
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils
                .generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
    }

    @Test
    public void topicNameContainsTest() {
        Map<String, String> filterMap = Map.of("topicName_CONTAINS", TOPIC_NAME_CONTAINS);
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils
                .generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void partitionCountGreaterTest() {
        Map<String, String> filterMap = Map.of("partitionCount_GREATER", "2");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("partitionCount_GREATER", "3");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }
    @Test
    public void partitionCountLessTest() {

        Map<String, String> filterMap = Map.of("partitionCount_LESS", "2");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
        filterMap = Map.of("partitionCount_LESS", "4");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));

    }


    @Test
    public void partitionCountBetweenTest() {
        Map<String, String> filterMap = Map.of("partitionCount_GREATER", "2", "partitionCount_LESS", "4");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));

        filterMap = Map.of("partitionCount_GREATER", "3", "partitionCount_LESS", "5");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

        filterMap = Map.of("partitionCount_GREATER", "1", "partitionCount_LESS", "2");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

        filterMap = Map.of("partitionCount_GREATER", "4", "partitionCount_LESS", "2");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

    }


    @Test
    public void replicationFactorGreaterTest() {
        Map<String, String> filterMap = Map.of("replicationFactor_GREATER", "2");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("replicationFactor_GREATER", "9");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }
    @Test
    public void replicationFactorLessTest() {

        Map<String, String> filterMap = Map.of("replicationFactor_LESS", "2");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
        filterMap = Map.of("replicationFactor_LESS", "4");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));

    }


    @Test
    public void replicationFactorBetweenTest() {
        Map<String, String> filterMap = Map.of("replicationFactor_GREATER", "2", "replicationFactor_LESS", "4");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));

        filterMap = Map.of("replicationFactor_GREATER", "3", "replicationFactor_LESS", "5");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

        filterMap = Map.of("replicationFactor_GREATER", "1", "replicationFactor_LESS", "2");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

        filterMap = Map.of("replicationFactor_GREATER", "4", "replicationFactor_LESS", "2");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

    }

    @Test
    public void consumerCountGreaterTest() {
        Map<String, String> filterMap = Map.of("consumerCount_GREATER", "2");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 3, 3,  Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("consumerCount_GREATER", "4");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }
    @Test
    public void consumerCountLessTest() {

        Map<String, String> filterMap = Map.of("consumerCount_LESS", "2");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
        filterMap = Map.of("consumerCount_LESS", "4");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));

    }


    @Test
    public void consumerCountBetweenTest() {
        Map<String, String> filterMap = Map.of("consumerCount_GREATER", "2", "consumerCount_LESS", "4");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));

        filterMap = Map.of("consumerCount_GREATER", "3", "consumerCount_LESS", "5");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

        filterMap = Map.of("consumerCount_GREATER", "1", "consumerCount_LESS", "2");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

        filterMap = Map.of("consumerCount_GREATER", "4", "consumerCount_LESS", "2");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));

    }

    @Test
    public void replicationStateHasReplicatedTest() {
        Map<String, String> filterMap = Map.of("replicationState_EQUALS", "ANY_REPLICATED");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 2, 2, Map.of(), DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("replicationState_EQUALS", "FULLY_REPLICATED");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
        filterMap = Map.of("replicationState_EQUALS", "UNDER_REPLICATED");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
    }

    @Test
    public void replicationStateHasNotReplicatedTest() {
        Map<String, String> filterMap = Map.of("replicationState_EQUALS", "ANY_REPLICATED");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 2, 0, Map.of(), DESCRIPTION, false);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("replicationState_EQUALS", "FULLY_REPLICATED");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("replicationState_EQUALS", "UNDER_REPLICATED");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void configAsMapEqualsTest() {
        Map<String, String> filterMap = Map.of("configString_EQUALS", "cleanup.policy: compact");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 3, 3, CONFIG_MAP_COMPACT, DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_EQUALS", "cleanup.policy: delete");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 3, 3, CONFIG_MAP_DELETE, DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_EQUALS", "cleanup.policy: delete; min.cleanable.dirty.ratio: 0.5");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_EQUALS", "cleanup.policy: delete; min.cleanable.dirty.ratio: 0.1");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void configAsStringTest() {
        Map<String, String> filterMap = Map.of("configString_EQUALS", "compact");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(CONSUMER_GROUPS_TEST_TOPIC, 3, 3, CONFIG_MAP_COMPACT, DESCRIPTION);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_EQUALS", "cleanup.policy");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_CONTAINS", "cleanup");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_LIKE", "%clean%");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_NOT_EMPTY", "");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertTrue(criteria.matches(topicInfo));
        filterMap = Map.of("configString_EQUALS", "");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
        filterMap = Map.of("configString_CONTAINS", "delete");
        criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void descriptionLikeTest() {
        Map<String, String> filterMap = Map.of("description_LIKE","%test%string%");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION_TEST);
        Assertions.assertTrue(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void descriptionEqualsTest() {
        Map<String, String> filterMap = Map.of("description_EQUALS", DESCRIPTION_TEST);
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION_TEST);
        Assertions.assertTrue(criteria.matches(topicInfo));
    }

    @Test
    public void descriptionContainsTest() {
        Map<String, String> filterMap = Map.of("description_CONTAINS", "test the string", "description_LIKE", "%test%string%");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION_TEST);
        Assertions.assertTrue(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }

    @Test
    public void descriptionNotEmptyTest() {
        Map<String, String> filterMap = Map.of("description_NOT_EMPTY", "");
        TopicListSearchCriteria criteria = TopicListSearchCriteria.fromJsonWith(filterMap, kafkaManager);
        TopicInfo topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_ROLES_TOPIC_NAME, 3, 3, Map.of(), DESCRIPTION_TEST);
        Assertions.assertTrue(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), "");
        Assertions.assertFalse(criteria.matches(topicInfo));
        topicInfo = TopicSearchCriteriaUtils.generateTopicInfo(STAFF_TOPIC_NAME, 3, 3, Map.of(), null);
        Assertions.assertFalse(criteria.matches(topicInfo));
    }


}
