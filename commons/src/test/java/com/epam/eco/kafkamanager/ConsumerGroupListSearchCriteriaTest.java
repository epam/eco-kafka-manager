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

import java.util.Arrays;
import java.util.Map;

import org.apache.kafka.common.ConsumerGroupState;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Mikhail_Vershkov
 */

public class ConsumerGroupListSearchCriteriaTest {

    private final static String TEST_GROUP_NAME = "testConsumerGroup";
    private final static String TEST_TOPIC_NAME = "testTopicName";
    private final static String TEST_MEMBER_NAME = "testMemberName";
    private final static String TEST_GROUP_DESCRIPTION = "testConsumerGroupDescription";
    private final static String TEST_WRONG_STORAGE_TYPE = "WRONG_STORAGE_TYPE";
    private final static String TEST_WRONG_OPERATION = "WRONG_OPERATION";
    private final static String TEST_WRONG_STATE = "WRONG_STATE";
    private final static String TEST_WRONG_CLAUSE_NAME = "WRONG_CLAUSE_NAME";
    private final static String TEST_GROUP_TOPICS_COUNT = "45";
    private final static String TEST_GROUP_MEMBERS_COUNT = "32";


    @Test
    public void groupNameTest() {

        AbstractSearchCriteriaImpl.SingleClause<String> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                TEST_GROUP_NAME, AbstractSearchCriteriaImpl.Operation.EQUALS);
        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("groupName_EQUALS", TEST_GROUP_NAME));
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("groupName_CONTAINS", TEST_GROUP_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(TEST_GROUP_NAME, AbstractSearchCriteriaImpl.Operation.CONTAINS);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("groupName_LIKE", TEST_GROUP_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(TEST_GROUP_NAME, AbstractSearchCriteriaImpl.Operation.LIKE);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("groupName_NOT_EMPTY", TEST_GROUP_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(TEST_GROUP_NAME, AbstractSearchCriteriaImpl.Operation.NOT_EMPTY);
        checkClause( criteria, expectedClause);

        Assertions.assertThrows(IllegalArgumentException.class, ()->
            ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("groupName_" + TEST_WRONG_OPERATION, TEST_GROUP_NAME))
        );
    }

    @Test
    public void storageTest() {

        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                  Map.of("storage_EQUALS", ConsumerGroupInfo.StorageType.ZOOKEEPER.name()));
        AbstractSearchCriteriaImpl.SingleClause<ConsumerGroupInfo.StorageType> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                ConsumerGroupInfo.StorageType.ZOOKEEPER, AbstractSearchCriteriaImpl.Operation.EQUALS);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("storage_EQUALS", ConsumerGroupInfo.StorageType.KAFKA.name()));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                ConsumerGroupInfo.StorageType.KAFKA, AbstractSearchCriteriaImpl.Operation.EQUALS);
        checkClause( criteria, expectedClause);

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("storage_EQUALS", TEST_WRONG_STORAGE_TYPE))
        );

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("storage_"+TEST_WRONG_OPERATION, TEST_WRONG_STORAGE_TYPE))
        );

    }

    @Test
    public void groupStateTest() {

        Arrays.stream(ConsumerGroupState.values()).forEach((state-> {
            ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("state_EQUALS", state.name()));
            AbstractSearchCriteriaImpl.SingleClause<ConsumerGroupState> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                    state, AbstractSearchCriteriaImpl.Operation.EQUALS);
            checkClause( criteria, expectedClause);
        }));

        Assertions.assertThrows(IllegalArgumentException.class, () ->
            ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("state_EQUALS", TEST_WRONG_STATE))
        );
        Assertions.assertThrows(IllegalArgumentException.class, () ->
            ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("state_"+TEST_WRONG_OPERATION,
                                                                      AbstractSearchCriteriaImpl.Operation.EQUALS))
        );

    }

    @Test
    public void topicsTest() {

        AbstractSearchCriteriaImpl.SingleClause<String> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                TEST_TOPIC_NAME, AbstractSearchCriteriaImpl.Operation.EQUALS);
        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("topics_EQUALS", TEST_TOPIC_NAME));
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("topics_CONTAINS", TEST_TOPIC_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_TOPIC_NAME, AbstractSearchCriteriaImpl.Operation.CONTAINS);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("topics_LIKE", TEST_TOPIC_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_TOPIC_NAME, AbstractSearchCriteriaImpl.Operation.LIKE);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("topics_NOT_EMPTY", TEST_TOPIC_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_TOPIC_NAME, AbstractSearchCriteriaImpl.Operation.NOT_EMPTY);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of(TEST_WRONG_CLAUSE_NAME + "_NOT_EMPTY", TEST_GROUP_NAME));
        Assertions.assertEquals((criteria.getClauses().stream()
                                              .mapToLong(clause->clause.getClauses().size()).sum()), 0L);

        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("topics_" + TEST_WRONG_OPERATION, TEST_GROUP_NAME))
        );
    }

    @Test
    public void topicsCountTest() {

        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("topicsCount_EQUALS", TEST_GROUP_TOPICS_COUNT));
        AbstractSearchCriteriaImpl.SingleClause<Integer> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                Integer.valueOf(TEST_GROUP_TOPICS_COUNT),
                AbstractSearchCriteriaImpl.Operation.EQUALS);
        Assertions.assertEquals(criteria.getClauses().stream().mapToLong(clause -> clause.getClauses().size()).sum(), 1L);
        checkClause(criteria,expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("topicsCount_GREATER", TEST_GROUP_TOPICS_COUNT));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                Integer.valueOf(TEST_GROUP_TOPICS_COUNT),
                AbstractSearchCriteriaImpl.Operation.GREATER);
        Assertions.assertEquals(criteria.getClauses().stream().mapToLong(clause -> clause.getClauses().size()).sum(), 1L);
        checkClause(criteria,expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("topicsCount_LESS", TEST_GROUP_TOPICS_COUNT));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                Integer.valueOf(TEST_GROUP_TOPICS_COUNT),
                AbstractSearchCriteriaImpl.Operation.LESS);
        Assertions.assertEquals(criteria.getClauses().stream().mapToLong(clause -> clause.getClauses().size()).sum(), 1L);
        checkClause(criteria,expectedClause);

        Assertions.assertThrows(IllegalArgumentException.class,
                      ()->ConsumerGroupListSearchCriteria.parseTopicCriteria(
                              Map.of("topicsCount_"+TEST_WRONG_OPERATION, TEST_GROUP_TOPICS_COUNT)));

    }

    @Test
    public void membersTest() {

        AbstractSearchCriteriaImpl.SingleClause<String> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                TEST_MEMBER_NAME, AbstractSearchCriteriaImpl.Operation.EQUALS);
        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("members_EQUALS", TEST_MEMBER_NAME));
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("members_CONTAINS", TEST_MEMBER_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_MEMBER_NAME, AbstractSearchCriteriaImpl.Operation.CONTAINS);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("members_LIKE", TEST_MEMBER_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_MEMBER_NAME, AbstractSearchCriteriaImpl.Operation.LIKE);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("members_NOT_EMPTY", TEST_MEMBER_NAME));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_MEMBER_NAME, AbstractSearchCriteriaImpl.Operation.NOT_EMPTY);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of(TEST_WRONG_CLAUSE_NAME + "_NOT_EMPTY", TEST_MEMBER_NAME));
        Assertions.assertEquals((criteria.getClauses().stream()
                                         .mapToLong(clause->clause.getClauses().size()).sum()), 0L);

        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("topics_" + TEST_WRONG_OPERATION, TEST_MEMBER_NAME))
                               );
    }

    @Test
    public void membersCountTest() {

        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("membersCount_EQUALS", TEST_GROUP_MEMBERS_COUNT));
        AbstractSearchCriteriaImpl.SingleClause<Integer> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                Integer.valueOf(TEST_GROUP_MEMBERS_COUNT),
                AbstractSearchCriteriaImpl.Operation.EQUALS);
        Assertions.assertEquals(criteria.getClauses().stream().mapToLong(clause -> clause.getClauses().size()).sum(), 1L);
        checkClause(criteria,expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("membersCount_GREATER", TEST_GROUP_MEMBERS_COUNT));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                Integer.valueOf(TEST_GROUP_MEMBERS_COUNT),
                AbstractSearchCriteriaImpl.Operation.GREATER);
        Assertions.assertEquals(criteria.getClauses().stream().mapToLong(clause -> clause.getClauses().size()).sum(), 1L);
        checkClause(criteria,expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(
                Map.of("membersCount_LESS", TEST_GROUP_MEMBERS_COUNT));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                Integer.valueOf(TEST_GROUP_MEMBERS_COUNT),
                AbstractSearchCriteriaImpl.Operation.LESS);
        Assertions.assertEquals(criteria.getClauses().stream().mapToLong(clause -> clause.getClauses().size()).sum(), 1L);
        checkClause(criteria,expectedClause);

        Assertions.assertThrows(IllegalArgumentException.class,
                                ()->ConsumerGroupListSearchCriteria.parseTopicCriteria(
                                        Map.of("membersCount_"+TEST_WRONG_OPERATION, TEST_GROUP_MEMBERS_COUNT)));

    }


    @Test
    public void descriptionTest() {

        AbstractSearchCriteriaImpl.SingleClause<String> expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>(
                TEST_GROUP_DESCRIPTION, AbstractSearchCriteriaImpl.Operation.EQUALS);
        ConsumerGroupListSearchCriteria criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("description_EQUALS", TEST_GROUP_DESCRIPTION));
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("description_CONTAINS", TEST_GROUP_DESCRIPTION));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_GROUP_DESCRIPTION, AbstractSearchCriteriaImpl.Operation.CONTAINS);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("description_LIKE", TEST_GROUP_DESCRIPTION));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_GROUP_DESCRIPTION, AbstractSearchCriteriaImpl.Operation.LIKE);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("description_NOT_EMPTY", TEST_GROUP_DESCRIPTION));
        expectedClause = new AbstractSearchCriteriaImpl.SingleClause<>( TEST_GROUP_DESCRIPTION, AbstractSearchCriteriaImpl.Operation.NOT_EMPTY);
        checkClause( criteria, expectedClause);

        criteria = ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of(TEST_WRONG_CLAUSE_NAME + "_NOT_EMPTY", TEST_GROUP_DESCRIPTION));
        Assertions.assertEquals((criteria.getClauses().stream()
                                         .mapToLong(clause->clause.getClauses().size()).sum()), 0L);

        Assertions.assertThrows(IllegalArgumentException.class,
                                () -> ConsumerGroupListSearchCriteria.parseTopicCriteria(Map.of("description_" + TEST_WRONG_OPERATION, TEST_GROUP_DESCRIPTION))
                               );
    }

    private void checkClause(ConsumerGroupListSearchCriteria criteria,
                             AbstractSearchCriteriaImpl.SingleClause expectedClause) {
        Assertions.assertTrue(criteria.getClauses().stream()
                                      .flatMap(clauses->clauses.getClauses().stream())
                                      .allMatch(clause->clause.equals(expectedClause)));

    }

}
