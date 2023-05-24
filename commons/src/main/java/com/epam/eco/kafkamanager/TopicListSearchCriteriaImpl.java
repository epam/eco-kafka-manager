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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonIgnore;

import static java.util.Objects.isNull;

/**
 * @author Mikhail_Vershkov
 */
public class TopicListSearchCriteriaImpl implements TopicSearchCriteria {

    private static final String OPERATION_SEPARATOR = "_";
    private static final String TOPIC_NAME_ATTR = "topicName";
    private static final String PARTITION_COUNT_ATTR = "partitionCount";
    private static final String REPLICATION_COUNT_ATTR = "replicationFactor";
    private static final String CONSUMER_COUNT_ATTR = "consumerCount";
    private static final String REPLICATION_STATE_ATTR = "replicationState";
    private static final String CONFIG_STRING_ATTR = "configString";
    private static final String DESCRIPTION_ATTR = "description";

    private static final String REGEX_CONFIG_STRING_PATTERN = "((.)+:(.)+(;|))+";
    private static final String[] ARRAY_ATTRS = {TOPIC_NAME_ATTR, PARTITION_COUNT_ATTR, REPLICATION_COUNT_ATTR, CONSUMER_COUNT_ATTR, REPLICATION_STATE_ATTR, CONFIG_STRING_ATTR, DESCRIPTION_ATTR};


    public enum Operation {
        EQUALS, CONTAINS, GREATER, LESS, LIKE, NOT_EMPTY
    }

    private final Set<ClausesWithHandler> clauses;
    private final KafkaManager kafkaManager;

    private TopicListSearchCriteriaImpl(Set<ClausesWithHandler> clauses, KafkaManager kafkaManager) {
        this.clauses = clauses;
        this.kafkaManager = kafkaManager;
    }

    @JsonIgnore
    public KafkaManager kafkaManager() {
        return kafkaManager;
    }

    @Override
    public boolean matches(TopicInfo obj) {
        Validate.notNull(obj, "Topic Info is null");
        return clauses.stream().allMatch(clause -> clause.match(obj));
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj) {
            return true;
        }
        if(obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TopicListSearchCriteriaImpl that = (TopicListSearchCriteriaImpl) obj;
        return Objects.equals(this.clauses, that.clauses);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clauses);
    }

    @Override
    public String toString() {
        return "{ clauses: " + clauses + "}";
    }

    public static TopicListSearchCriteriaImpl fromJsonWith(Map<String, ?> map, KafkaManager kafkaManager) {
        return parseTopicCriteria(map, kafkaManager);
    }

    static Map<String, String> parseConfigString(String configString) {
        String config = StringUtils.stripToNull(configString);
        if(config == null) {
            return Collections.emptyMap();
        }

        String[] parts = StringUtils.split(config, ";");
        Map<String, String> configMap = new HashMap<>((int) Math.ceil(parts.length / 0.75));
        for(String configEntry : parts) {
            int colonIdx = configEntry.indexOf(':');
            String key = colonIdx >= 0 ? StringUtils.stripToNull(configEntry.substring(0, colonIdx)) : null;
            String value = colonIdx >= 0 ? StringUtils.stripToNull(configEntry.substring(colonIdx + 1)) : null;
            configMap.put(key, value);
        }
        return configMap;
    }

    private static boolean like(String value, String regexp) {
        return value.toLowerCase()
                    .matches(regexp.toLowerCase().replace("?", ".")
                                   .replaceAll("%", ".*"));
    }

    private static boolean ifKeyExists(String key) {
        return Arrays.stream(ARRAY_ATTRS).anyMatch(key::startsWith);
    }

    private static TopicListSearchCriteriaImpl parseTopicCriteria(Map<String, ?> map, KafkaManager kafkaManager) {

        Set<SingleClause<String>> topicClauses = new HashSet<>();
        Set<SingleClause<Integer>> partitionCountClauses = new HashSet<>();
        Set<SingleClause<Integer>> replicationFactorClauses = new HashSet<>();
        Set<SingleClause<Integer>> consumerCountClauses = new HashSet<>();
        ReplicationState replicationStateClause = ReplicationState.ANY_REPLICATED;
        Set<SingleClause<String>> configStringClauses = new HashSet<>();

        Set<SingleClause<String>> descriptionClauses = new HashSet<>();

        for(String key : map.keySet()) {

            if(ifKeyExists(key) && key.indexOf(OPERATION_SEPARATOR) > 0) {

                String filterColumn = key.substring(0, key.indexOf(OPERATION_SEPARATOR));
                String rawOperation = key.substring(key.indexOf(OPERATION_SEPARATOR) + 1);
                Operation filterOperation = Operation.valueOf(rawOperation);

                switch (filterColumn) {
                    case TOPIC_NAME_ATTR -> topicClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    case PARTITION_COUNT_ATTR -> partitionCountClauses.add(
                            new SingleClause<>(Integer.valueOf((String) map.get(key)), filterOperation));
                    case REPLICATION_COUNT_ATTR -> replicationFactorClauses.add(
                            new SingleClause<>(Integer.valueOf((String) map.get(key)), filterOperation));
                    case CONSUMER_COUNT_ATTR -> consumerCountClauses.add(
                            new SingleClause<>(Integer.valueOf((String) map.get(key)), filterOperation));
                    case REPLICATION_STATE_ATTR -> replicationStateClause = ReplicationState.valueOf((String) map.get(key));
                    case CONFIG_STRING_ATTR -> configStringClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    case DESCRIPTION_ATTR -> descriptionClauses.add(new SingleClause<>((String) map.get(key), filterOperation));
                    default -> {
                    }
                }
            }

        }

        return new TopicListSearchCriteriaImpl(
                Set.of(new ClausesWithHandler<>(topicClauses, stringClausesHandler, TopicInfo::getName),
                       new ClausesWithHandler<>(partitionCountClauses, numericClausesHandler, TopicInfo::getPartitionCount),
                       new ClausesWithHandler<>(replicationFactorClauses, numericClausesHandler, TopicInfo::getReplicationFactor),
                       new ClausesWithHandler<>(consumerCountClauses, numericClausesHandler,
                                                topicInfo -> kafkaManager.getConsumerGroupsForTopic(
                                                        topicInfo.getName()).size()),
                       new ClausesWithHandler<>(Set.of(new SingleClause<>(replicationStateClause, Operation.EQUALS)),
                                                replicationStateClausesHandler, topicInfo -> topicInfo),
                       new ClausesWithHandler<>(configStringClauses, configMapClausesHandler, topicInfo -> topicInfo),
                       new ClausesWithHandler<>(descriptionClauses, stringClausesHandler,
                                                topicInfo -> topicInfo.getMetadata().map(Metadata::getDescription).orElse(null))),
                kafkaManager);
    }

    private static class SingleClause<T> {
        private final T filterValue;
        private final Operation operation;

        public SingleClause(T filterValue, Operation operation) {
            this.filterValue = filterValue;
            this.operation = operation;
        }

        @Override
        public boolean equals(Object o) {
            if(this == o)
                return true;
            if(! (o instanceof SingleClause<?> that))
                return false;

            if(! filterValue.equals(that.filterValue))
                return false;
            return operation == that.operation;
        }

        @Override
        public int hashCode() {
            int result = filterValue.hashCode();
            result = 31 * result + operation.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "SingleClause {" + "filterValue=" + filterValue + ", operation=" + operation + '}';
        }

        public T getFilterValue() {
            return filterValue;
        }

        public Operation getOperation() {
            return operation;
        }
    }

    private static class ClausesWithHandler<T, V> {
        private final Set<SingleClause<T>> clauses;
        private final BiPredicate<Set<SingleClause<T>>, V> clausesHandler;
        private final Function<TopicInfo, V> valueExtractor;

        private ClausesWithHandler(Set<SingleClause<T>> clauses,
                                   BiPredicate<Set<SingleClause<T>>, V> clausesHandler,
                                   Function<TopicInfo, V> valueExtractor) {
            this.clauses = clauses;
            this.clausesHandler = clausesHandler;
            this.valueExtractor = valueExtractor;
        }

        boolean match(TopicInfo obj) {
            return getClausesHandler().test(getClauses(),getValueExtractor().apply(obj));
        }
        public Set<SingleClause<T>> getClauses() {
            return clauses;
        }

        public BiPredicate<Set<SingleClause<T>>, V> getClausesHandler() {
            return clausesHandler;
        }

        public Function<TopicInfo, V> getValueExtractor() {
            return valueExtractor;
        }

    }

    private static final BiPredicate<Set<SingleClause<String>>, String> stringClausesHandler = (Set<SingleClause<String>> stringClauses, String value) -> stringClauses.stream().allMatch(
            clause -> compareStringValues(clause.getFilterValue(), value, clause.getOperation()));


    private static final BiPredicate<Set<SingleClause<Integer>>, Integer> numericClausesHandler = (Set<SingleClause<Integer>> numericClauses, Integer value) -> numericClauses.stream().allMatch(
            clause -> compareNumberValues(clause.getFilterValue(), value, clause.getOperation()));

    private static final BiPredicate<Set<SingleClause<ReplicationState>>, TopicInfo> replicationStateClausesHandler = (Set<SingleClause<ReplicationState>> clauses, TopicInfo topicInfo) -> clauses.stream().allMatch(
            clause -> {
                Boolean underReplicated = null;
                if(ReplicationState.FULLY_REPLICATED == clause.getFilterValue()) {
                    underReplicated = Boolean.FALSE;
                } else if(ReplicationState.UNDER_REPLICATED == clause.getFilterValue()) {
                    underReplicated = Boolean.TRUE;
                }
                return (underReplicated == null || topicInfo.hasUnderReplicatedPartitions() == underReplicated);
            });

    private static final BiPredicate<Set<SingleClause<String>>, TopicInfo> configMapClausesHandler =
            (Set<SingleClause<String>> clauses, TopicInfo topicInfo) ->
                    clauses.stream().allMatch(clause -> configMapSingleClauseHandler(clause, topicInfo));

    private static boolean configMapSingleClauseHandler(SingleClause<String> clause, TopicInfo topicInfo) {
        if((isNull(clause.getFilterValue()) || clause.getFilterValue().isEmpty()) && clause.getOperation()!=Operation.NOT_EMPTY) {
            return false;
        }
        if(Pattern.matches(REGEX_CONFIG_STRING_PATTERN, clause.getFilterValue()) && clause.getOperation()==Operation.EQUALS) {
            return topicInfo.getConfig().entrySet().containsAll(parseConfigString(clause.getFilterValue()).entrySet());
        } else if(!Pattern.matches(REGEX_CONFIG_STRING_PATTERN, clause.getFilterValue()) && clause.getOperation()==Operation.EQUALS) {
            return topicInfo.getConfig().entrySet().stream().anyMatch(entry->entry.getKey().equals(clause.getFilterValue())
                    || entry.getValue().equals(clause.getFilterValue()));
        }
        return compareStringValues(clause.getFilterValue(), stripJsonString(topicInfo.getConfig().toString()), clause.getOperation());
    }

    private static String stripJsonString(String string) {
        return string
                  .replace("{","")
                  .replace("}","");
    }

    private static boolean compareStringValues(String filterValue, String value, Operation operation) {
        if((isNull(filterValue) || StringUtils.isBlank(filterValue)) && operation != Operation.NOT_EMPTY) {
            return true;
        }
        if(isNull(value)) {
            return false;
        }
        switch (operation) {
            case EQUALS:
                return filterValue.equalsIgnoreCase(value);
            case CONTAINS:
                return StringUtils.containsIgnoreCase(value, filterValue);
            case NOT_EMPTY:
                return !StringUtils.isBlank(value);
            case LIKE: {
                try {
                    return like(value, filterValue);
                } catch (PatternSyntaxException exception) {
                    return false;
                }
            }
            default: {
                return false;
            }
        }
    }

    private static boolean compareNumberValues(Integer filterValue, Integer value, Operation operation) {
        if(isNull(value)) {
            return false;
        }
        if(isNull(filterValue) && operation != Operation.NOT_EMPTY) {
            return true;
        }
        switch (operation) {
            case EQUALS:
                return value.equals(filterValue);
            case GREATER:
                return value > filterValue;
            case LESS:
                return value < filterValue;
            default: {
                return false;
            }
        }
    }
            

}
