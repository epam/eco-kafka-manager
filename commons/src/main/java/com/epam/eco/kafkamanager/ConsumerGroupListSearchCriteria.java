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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.kafka.common.ConsumerGroupState;


/**
 * @author Mikhail_Vershkov
 */
public class ConsumerGroupListSearchCriteria extends AbstractSearchCriteriaImpl<ConsumerGroupInfo> {

    private static final String GROUP_NAME_ATTR = "groupName";
    private static final String STATE_ATTR = "state";
    private static final String STORAGE_ATTR = "storage";
    private static final String TOPICS_ATTR = "topics";
    private static final String TOPICS_COUNT_ATTR = "topicsCount";
    private static final String MEMBERS_ATTR = "members";
    private static final String MEMBERS_COUNT_ATTR = "membersCount";
    private static final String DESCRIPTION_ATTR = "description";
    private static final String[] ARRAY_ATTRS = {GROUP_NAME_ATTR, STATE_ATTR, STORAGE_ATTR, TOPICS_ATTR,
            TOPICS_COUNT_ATTR, MEMBERS_ATTR, MEMBERS_COUNT_ATTR, DESCRIPTION_ATTR};


    private ConsumerGroupListSearchCriteria(Set<ClausesWithHandler> clauses) {
        super(clauses);
    }

    public static ConsumerGroupListSearchCriteria fromJsonWith(Map<String, ?> map) {
        return parseTopicCriteria(map);
    }

    static boolean ifKeyExists(String key) {
        return Arrays.stream(ARRAY_ATTRS).anyMatch(key::startsWith);
    }

    public static ConsumerGroupListSearchCriteria parseTopicCriteria(Map<String, ?> map) {

        Set<SingleClause<String>> groupNameClauses = new HashSet<>();
        Set<SingleClause<ConsumerGroupState>> stateClauses = new HashSet<>();
        Set<SingleClause<ConsumerGroupInfo.StorageType>> storageClauses = new HashSet<>();
        Set<SingleClause<String>> topicsClauses  = new HashSet<>();
        Set<SingleClause<Integer>> topicsCountClauses = new HashSet<>();
        Set<SingleClause<String>> membersClauses = new HashSet<>();
        Set<SingleClause<Integer>> membersCountClauses = new HashSet<>();
        Set<SingleClause<String>> descriptionClauses = new HashSet<>();

        for(String key : map.keySet()) {

            if(ifKeyExists(key) && key.indexOf(OPERATION_SEPARATOR) > 0) {

                String filterColumn = key.substring(0, key.indexOf(OPERATION_SEPARATOR));
                String rawOperation = key.substring(key.indexOf(OPERATION_SEPARATOR) + 1);
                Operation filterOperation = Operation.valueOf(rawOperation);

                switch (filterColumn) {
                    case GROUP_NAME_ATTR -> groupNameClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    case STATE_ATTR -> stateClauses.add(new SingleClause<>(ConsumerGroupState.valueOf((String) map.get(key)), filterOperation));
                    case STORAGE_ATTR -> storageClauses.add(new SingleClause<>(ConsumerGroupInfo.StorageType.valueOf((String)map.get(key)), filterOperation));
                    case TOPICS_ATTR -> topicsClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    case TOPICS_COUNT_ATTR -> topicsCountClauses.add(new SingleClause<>(Integer.valueOf((String) map.get(key)), filterOperation));
                    case MEMBERS_ATTR -> membersClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    case MEMBERS_COUNT_ATTR -> membersCountClauses.add(new SingleClause<>(Integer.valueOf((String) map.get(key)), filterOperation));
                    case DESCRIPTION_ATTR -> descriptionClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    default -> {
                    }
                }
            }

        }

        return new ConsumerGroupListSearchCriteria(
                Set.of(new ClausesWithHandler<>(groupNameClauses, stringClausesHandler, ConsumerGroupInfo::getName),
                       new ClausesWithHandler<ConsumerGroupInfo.StorageType, ConsumerGroupInfo, ConsumerGroupInfo>(storageClauses, storageClausesHandler,
                                                                                                        consumerGroupInfo -> consumerGroupInfo),
                       new ClausesWithHandler<ConsumerGroupState,ConsumerGroupInfo,ConsumerGroupInfo>(stateClauses, stateClausesHandler, consumerGroupInfo -> consumerGroupInfo),
                       new ClausesWithHandler<String,ConsumerGroupInfo,ConsumerGroupInfo>(topicsClauses, topicsClausesHandler, consumerGroupInfo -> consumerGroupInfo),
                       new ClausesWithHandler<Integer,Integer,ConsumerGroupInfo>(topicsCountClauses, numericClausesHandler, consumerInfo->consumerInfo.getTopicNames().size()),
                       new ClausesWithHandler<String,ConsumerGroupInfo,ConsumerGroupInfo>(membersClauses, membersClausesHandler, groupInfo -> groupInfo),
                       new ClausesWithHandler<Integer,Integer,ConsumerGroupInfo>(membersCountClauses, numericClausesHandler, consumerInfo->consumerInfo.getMembers().size()),
                       new ClausesWithHandler<String,String,ConsumerGroupInfo>(descriptionClauses, stringClausesHandler,
                                                                               groupInfo -> groupInfo.getMetadata().map(Metadata::getDescription).orElse(null))
        ));
    }

    private static final BiPredicate<Set<SingleClause<ConsumerGroupInfo.StorageType>>, ConsumerGroupInfo> storageClausesHandler = (Set<SingleClause<ConsumerGroupInfo.StorageType>> clauses, ConsumerGroupInfo groupInfo) -> clauses.stream().allMatch(
       clause -> groupInfo.getStorageType() == clause.getFilterValue());

    private static final BiPredicate<Set<SingleClause<ConsumerGroupState>>, ConsumerGroupInfo> stateClausesHandler = (Set<SingleClause<ConsumerGroupState>> clauses, ConsumerGroupInfo groupInfo) -> clauses.stream().allMatch(
            clause -> groupInfo.getState() == clause.getFilterValue());

    private static final BiPredicate<Set<SingleClause<String>>, ConsumerGroupInfo> topicsClausesHandler = (Set<SingleClause<String>> clauses, ConsumerGroupInfo groupInfo) -> clauses.stream().allMatch(
            clause -> groupInfo.getTopicNames().stream().map(topic->compareStringValues(clause.getFilterValue(), topic, clause.getOperation() )).reduce((x,y) -> x||y).orElse(false));

    private static final BiPredicate<Set<SingleClause<String>>, ConsumerGroupInfo> membersClausesHandler = (Set<SingleClause<String>> clauses, ConsumerGroupInfo groupInfo) -> clauses.stream().allMatch(
            clause -> groupInfo.getMembers().stream().map(member->compareStringValues(clause.getFilterValue(),
                                                                                      member.getMemberId() + member.getClientHost(), clause.getOperation() )).reduce((x,y) -> x||y).orElse(false));



}
