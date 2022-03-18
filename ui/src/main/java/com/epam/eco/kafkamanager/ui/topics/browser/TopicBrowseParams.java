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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.collections4.ListUtils;

import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;

/**
 * @author Andrei_Tytsik
 */
public class TopicBrowseParams extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final String TOPIC_NAME = "topicName";
    public static final String KEY_FORMAT = "keyFormat";
    public static final String VALUE_FORMAT = "valueFormat";
    public static final String OFFSETS_TIMESTAMP = "offsetsTimestamp";
    public static final String TIMEOUT = "timeout";
    public static final String LIMIT = "limit";
    public static final String PARTITION_OFFSET = "p_%d";
    public static final String PARTITION_ENABLED = "pe_%d";
    public static final String COLUMN_ENABLED = "ce_%s";

    private static final Pattern PARTITION_OFFSET_PATTERN = Pattern.compile("^p_(0|[1-9]\\d*)$");
    private static final Pattern COLUMN_ENABLED_PATTERN = Pattern.compile("^ce_(.+)$");

    public TopicBrowseParams(Map<String, Object> requestParams) {
        if (requestParams != null) {
            putAll(requestParams);
        }
    }

    public String getTopicName() {
        return (String)get(TOPIC_NAME);
    }

    public void setTopicName(String topicName) {
        put(TOPIC_NAME, topicName);
    }

    public DataFormat getKeyFormat() {
        return getAsDataFormat(KEY_FORMAT);
    }

    public void setKeyFormat(DataFormat keyFormat) {
        put(KEY_FORMAT, keyFormat);
    }

    public void setKeyFormat(String keyFormat) {
        put(KEY_FORMAT, keyFormat);
    }

    public void setKeyFormatIfMissing(DataFormat keyFormat) {
        if (getKeyFormat() == null) {
            setKeyFormat(keyFormat);
        }
    }

    public void setKeyFormatIfMissing(String keyFormat) {
        if (getKeyFormat() == null) {
            setKeyFormat(keyFormat);
        }
    }

    public void deleteKeyFormat() {
        remove(KEY_FORMAT);
    }

    public DataFormat getValueFormat() {
        return getAsDataFormat(VALUE_FORMAT);
    }

    public void setValueFormat(DataFormat valueFormat) {
        put(VALUE_FORMAT, valueFormat);
    }

    public void setValueFormat(String valueFormat) {
        put(VALUE_FORMAT, valueFormat);
    }

    public void deleteValueFormat() {
        remove(VALUE_FORMAT);
    }

    public void setValueFormatIfMissing(DataFormat valueFormat) {
        if (getValueFormat() == null) {
            setValueFormat(valueFormat);
        }
    }

    public void setValueFormatIfMissing(String valueFormat) {
        if (getValueFormat() == null) {
            setValueFormat(valueFormat);
        }
    }

    public String getOffsetsTimestamp() {
        return (String)get(OFFSETS_TIMESTAMP);
    }

    public void setOffsetsTimestamp(String offsetsTimestamp) {
        put(OFFSETS_TIMESTAMP, offsetsTimestamp);
    }

    public List<String> listColumns() {
        return keySet().stream().
                filter(key -> COLUMN_ENABLED_PATTERN.matcher(key).matches()).
                map(this::extractColumnFromEnabledKey).
                filter(this::isColumnEnabled).
                sorted().
                collect(Collectors.toList());
    }

    public long getTimeout() {
        Long limit = getAsLong(TIMEOUT);
        return limit != null ? limit : -1;
    }

    public void setTimeout(long timeout) {
        put(TIMEOUT, timeout);
    }

    public long getLimit() {
        Long limit = getAsLong(LIMIT);
        return limit != null ? limit : TopicRecordFetchParams.MAX_LIMIT;
    }

    public void setLimit(long limit) {
        put(LIMIT, limit);
    }

    public boolean isColumnEnabled(String column) {
        Boolean enabled = getAsBoolean(formatColumnEnabledKey(column));
        return enabled != null ? enabled : false;
    }

    public void setColumnEnabled(String column, boolean enabled) {
        put(formatColumnEnabledKey(column), enabled);
    }

    public boolean isPartitionEnabled(int partition) {
        Boolean enabled = getAsBoolean(formatPartitionEnabledKey(partition));
        return enabled != null ? enabled : true;
    }

    public void setPartitionEnabled(int partition, boolean enabled) {
        put(formatPartitionEnabledKey(partition), enabled);
    }

    public long getPartitionOffset(int partition) {
        Long offset = getAsLong(formatPartitionOffsetKey(partition));
        return offset != null ? offset : 0L;
    }

    public void addPartitionOffset(int partition, long offset) {
        put(formatPartitionOffsetKey(partition), offset);
    }

    public void addPartitionOffsetOnCondition(int partition, long offset, Predicate<Long> condition) {
        long offsetOld = getPartitionOffset(partition);
        if (condition.test(offsetOld)) {
            addPartitionOffset(partition, offset);
        }
    }

    public void removePartitionOffset(int partition) {
        remove(formatPartitionOffsetKey(partition));
    }

    public boolean containsPartition(int partition) {
        Long offset = getAsLong(formatPartitionOffsetKey(partition));
        return offset != null;
    }

    public List<Integer> listPartitions() {
        return keySet().stream().
            filter(key -> PARTITION_OFFSET_PATTERN.matcher(key).matches()).
            map(this::extractPartitionFromOffsetKey).
            sorted().
            collect(Collectors.toList());
    }

    public List<List<Integer>> listPartitionBatches(int batchSize) {
        return ListUtils.partition(listPartitions(), batchSize);
    }

    public Map<Integer, Long> getPartitionOffsets() {
        return listPartitions().stream().
                filter(this::isPartitionEnabled).
                collect(Collectors.toMap(
                        Function.identity(),
                        this::getPartitionOffset));
    }

    private DataFormat getAsDataFormat(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof DataFormat) {
            return (DataFormat)value;
        } else if (value instanceof String) {
            return DataFormat.valueOf((String)value);
        } else {
            throw new RuntimeException(
                    String.format(
                            "Can't convert %s to %s",
                            value.getClass().getName(), DataFormat.class.getName()));
        }
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

    private String formatColumnEnabledKey(String column) {
        return String.format(COLUMN_ENABLED, column);
    }

    private String formatPartitionEnabledKey(int partition) {
        return String.format(PARTITION_ENABLED, partition);
    }

    private String formatPartitionOffsetKey(int partition) {
        return String.format(PARTITION_OFFSET, partition);
    }

    private int extractPartitionFromOffsetKey(String key) {
        Matcher matcher = PARTITION_OFFSET_PATTERN.matcher(key);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    String.format("Can't extract partition from key '%s'", key));
        }

        String partitionString = matcher.group(1);
        return Integer.parseInt(partitionString);
    }

    private String extractColumnFromEnabledKey(String key) {
        Matcher matcher = COLUMN_ENABLED_PATTERN.matcher(key);
        if (!matcher.find()) {
            throw new IllegalArgumentException(
                    String.format("Can't extract column from key '%s'", key));
        }

        return matcher.group(1);
    }

    public static TopicBrowseParams with(Map<String, Object> requestParams) {
        return new TopicBrowseParams(requestParams);
    }

}
