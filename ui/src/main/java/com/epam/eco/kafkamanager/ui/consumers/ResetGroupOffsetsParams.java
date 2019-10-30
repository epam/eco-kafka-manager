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
package com.epam.eco.kafkamanager.ui.consumers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;
import org.apache.kafka.common.TopicPartition;

import com.epam.eco.commons.kafka.KafkaUtils;
import com.epam.eco.commons.kafka.TopicPartitionComparator;

/**
 * @author Andrei_Tytsik
 */
public class ResetGroupOffsetsParams extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final String GROUP_NAME = "groupName";
    public static final String PARTITION_OFFSET = "p_%s";
    public static final String PARTITION_ENABLED = "pe_%s";

    private static final Pattern PARTITION_OFFSET_PATTERN = Pattern.compile("^p_(.+)$");

    public ResetGroupOffsetsParams(Map<String, Object> requestParams) {
        if (requestParams != null) {
            putAll(requestParams);
        }
    }

    public String getGroupName() {
        return (String)get(GROUP_NAME);
    }

    public void setGroupName(String groupName) {
        put(GROUP_NAME, groupName);
    }

    public boolean isPartitionEnabled(TopicPartition partition) {
        Boolean enabled = getAsBoolean(formatPartitionEnabledKey(partition));
        return enabled != null ? enabled : true;
    }

    public void setPartitionEnabled(TopicPartition partition, boolean enabled) {
        put(formatPartitionEnabledKey(partition), enabled);
    }

    public long getPartitionOffset(TopicPartition partition) {
        Long offset = getAsLong(formatPartitionOffsetKey(partition));
        return offset != null ? offset : 0L;
    }

    public void addPartitionOffset(TopicPartition partition, long offset) {
        put(formatPartitionOffsetKey(partition), offset);
    }

    public void addPartitionOffsetOnCondition(TopicPartition partition, long offset, Predicate<Long> condition) {
        long offsetOld = getPartitionOffset(partition);
        if (condition.test(offsetOld)) {
            addPartitionOffset(partition, offset);
        }
    }

    public void removePartitionOffset(TopicPartition partition) {
        remove(formatPartitionOffsetKey(partition));
    }

    public boolean containsPartition(TopicPartition partition) {
        Long offset = getAsLong(formatPartitionOffsetKey(partition));
        return offset != null;
    }

    public List<TopicPartition> listPartitions() {
        return keySet().stream().
            filter(key -> PARTITION_OFFSET_PATTERN.matcher(key).matches()).
                map(this::extractPartitionFromOffsetKey).
            sorted(TopicPartitionComparator.INSTANCE).
            collect(Collectors.toList());
    }

    public List<List<TopicPartition>> listPartitionBatches(int batchSize) {
        return ListUtils.partition(listPartitions(), batchSize);
    }

    public Map<TopicPartition, Long> getPartitionOffsets() {
        return listPartitions().stream().
                filter(this::isPartitionEnabled).
                collect(Collectors.toMap(
                        Function.identity(),
                        this::getPartitionOffset));
    }

    private Long getAsLong(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number)value).longValue();
        } else if (value instanceof String) {
            return Long.valueOf((String)value);
        } else {
            throw new RuntimeException(
                    String.format(
                            "Can't convert %s to %s",
                            value.getClass().getName(), Long.class.getName()));
        }
    }

    private Boolean getAsBoolean(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof Boolean) {
            return (Boolean)value;
        } else if (value instanceof String) {
            if (
                    "1".equals(value) ||
                    "true".equalsIgnoreCase((String)value) ||
                    "on".equalsIgnoreCase((String)value)) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        } else {
            throw new RuntimeException(
                    String.format(
                            "Can't convert %s to %s",
                            value.getClass().getName(), Boolean.class.getName()));
        }
    }

    private String formatPartitionEnabledKey(TopicPartition partition) {
        return String.format(PARTITION_ENABLED, partition);
    }

    private String formatPartitionOffsetKey(TopicPartition partition) {
        return String.format(PARTITION_OFFSET, partition);
    }

    private TopicPartition extractPartitionFromOffsetKey(String key) {
        Matcher matcher = PARTITION_OFFSET_PATTERN.matcher(key);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    String.format("Can't extract TopicPartition from key '%s'", key));
        }

        String topicPartitionString = matcher.group(1);
        return KafkaUtils.parseTopicPartition(topicPartitionString);
    }

    public static ResetGroupOffsetsParams with(Map<String, Object> requestParams) {
        return new ResetGroupOffsetsParams(requestParams);
    }

}
