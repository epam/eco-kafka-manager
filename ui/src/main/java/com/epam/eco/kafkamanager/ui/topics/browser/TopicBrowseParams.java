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
import org.apache.kafka.clients.admin.Config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.kafkamanager.FetchMode;
import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Andrei_Tytsik
 */
public class TopicBrowseParams extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public static final String TOPIC_NAME = "topicName";
    public static final String KEY_FORMAT = "keyFormat";

    public static final String KAFKA_TOPIC_CONFIG = "kafkaTopicConfig";
    public static final String VALUE_FORMAT = "valueFormat";
    public static final String OFFSETS_TIMESTAMP = "offsetsTimestamp";
    public static final String TIMEOUT = "timeout";
    public static final String LIMIT = "limit";
    public static final String PARTITION_MIN_OFFSET = "p_min_%d";
    public static final String PARTITION_MIN_INCLUSIVE_OFFSET = "p_min_inc_%d";
    public static final String PARTITION_MAX_OFFSET = "p_max_%d";
    public static final String PARTITION_MAX_INCLUSIVE_OFFSET = "p_max_inc_%d";
    public static final String PARTITION_ENABLED = "pe_%d";
    public static final String COLUMN_ENABLED = "ce_%s";

    public static final String FETCH_MODE = "fetch-mode";
    public static final String FULL_SCREEN = "full-screen";
    public static final String FILTER_CLAUSE = "filter-clause";
    public static final String TIMESTAMP = "timestamp";

    private static final Pattern PARTITION_MIN_OFFSET_PATTERN = Pattern.compile("^p_min_(0|[1-9]\\d*)$");
    private static final Pattern COLUMN_ENABLED_PATTERN = Pattern.compile("^ce_(.+)$");

    private static final Long DEFAULT_TIMEOUT = 20_000L;

    public TopicBrowseParams(Map<String, Object> requestParams) {
        if (requestParams != null) {
            putAll(requestParams);
        }
    }

    public FetchMode getFetchMode() {
        return getAsFetchMode(FETCH_MODE);
    }

    public Boolean getFullScreen() {
        Boolean fullScreen = getAsBoolean(FULL_SCREEN);
        return ! isNull(fullScreen) && fullScreen;
    }

    Long getTimestamp() {
        return getAsLong(TIMESTAMP);
    }
    public boolean isAvroOrProtobufValueFormat() {
        return getAsDataFormat(VALUE_FORMAT) == DataFormat.AVRO || getAsDataFormat(VALUE_FORMAT) == DataFormat.PROTOCOL_BUFFERS;
    }

    public boolean isAvroValueFormat() {
        return getAsDataFormat(VALUE_FORMAT) == DataFormat.AVRO;
    }
    public boolean isStringValueFormat() {
        return getAsDataFormat(VALUE_FORMAT) == DataFormat.STRING;
    }
    public boolean isJsonValueFormat() {
        return getAsDataFormat(VALUE_FORMAT) == DataFormat.JSON_STRING;
    }

    public Config getKafkaTopicConfig() {
        return (Config)get(KAFKA_TOPIC_CONFIG);
    }

    public void setKafkaTopicConfig(Config kafkaTopicConfig) {
        put(KAFKA_TOPIC_CONFIG, kafkaTopicConfig);
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
        Long timeout = getAsLong(TIMEOUT);
        return timeout != null ? timeout : DEFAULT_TIMEOUT;
    }

    public void setTimeout(long timeout) {
        put(TIMEOUT, timeout);
    }

    public long getLimit() {
        Long limit = getAsLong(LIMIT);
        return limit != null ? limit : TopicRecordFetchParams.DEFAULT_LIMIT;
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

    public OffsetRange getPartitionOffset(int partition) {
        Long offsetMin = getAsLong(formatPartitionMinOffsetKey(partition));
        Boolean minOffsetInclusive = getAsBoolean(formatPartitionMinInclusiveOffsetKey(partition));
        Long offsetMax = getAsLong(formatPartitionMaxOffsetKey(partition));
        Boolean maxOffsetInclusive = getAsBoolean(formatPartitionMaxInclusiveOffsetKey(partition));
        return OffsetRange.with(nonNull(offsetMin) ? offsetMin : 0L, nonNull(minOffsetInclusive) ? minOffsetInclusive : false,
                                nonNull(offsetMax) ? offsetMax : 0L, nonNull(maxOffsetInclusive) ? maxOffsetInclusive : false);
    }

    public void addPartitionOffset(int partition, OffsetRange offset) {
        put(formatPartitionMinOffsetKey(partition), offset.getSmallest());
        put(formatPartitionMinInclusiveOffsetKey(partition), offset.isSmallestInclusive());
        put(formatPartitionMaxOffsetKey(partition), offset.getLargest());
        put(formatPartitionMaxInclusiveOffsetKey(partition), offset.isLargestInclusive());
    }

    public void addPartitionOffsetOnCondition(int partition, long offset, Predicate<OffsetRange> condition) {
        OffsetRange offsetRangeOld = getPartitionOffset(partition);
        if (condition.test(offsetRangeOld)) {
            addPartitionOffset(partition, offsetRangeOld);
        }
    }

    public void removePartitionOffset(int partition) {
        remove(formatPartitionMinOffsetKey(partition));
        remove(formatPartitionMaxOffsetKey(partition));
    }

    public boolean containsPartition(int partition) {
        Long offset = getAsLong(formatPartitionMinOffsetKey(partition));
        return offset != null;
    }

    public List<Integer> listPartitions() {
        return keySet().stream().
                filter(key -> PARTITION_MIN_OFFSET_PATTERN.matcher(key).matches()).
                               map(this::extractPartitionFromOffsetKey).
                               sorted().
                               collect(Collectors.toList());
    }

    public List<List<Integer>> listPartitionBatches(int batchSize) {
        return ListUtils.partition(listPartitions(), batchSize);
    }

    public Map<Integer, OffsetRange> getPartitionOffsets() {
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

    private FetchMode getAsFetchMode(String key) {
        Object value = get(key);
        if (value == null) {
            return null;
        }

        if (value instanceof FetchMode) {
            return (FetchMode)value;
        } else if (value instanceof String) {
            return FetchMode.valueOf((String)value);
        } else {
            throw new RuntimeException(
                    String.format(
                            "Can't convert %s to %s",
                            value.getClass().getName(), FetchMode.class.getName()));
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

    public List<FilterClause> getFilterClauses() {
        List<FilterClause> filterClauses=null;
        String filterString = (String)get(FILTER_CLAUSE);
        if(nonNull(filterString) && filterString.length()>0) {
            try {
                filterClauses = new ObjectMapper().readValue(filterString, new TypeReference<>() {});
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return filterClauses;

    }

    public Map<String,List<FilterClause>> getFilterClausesAsMap() {
        List<FilterClause> filterClauses = getFilterClauses();
        return isNull(filterClauses) ?
               new HashMap<>() :
               filterClauses.stream()
                            .collect(Collectors.groupingBy(FilterClause::getColumn));

    }

    private String formatColumnEnabledKey(String column) {
        return String.format(COLUMN_ENABLED, column);
    }

    private String formatPartitionEnabledKey(int partition) {
        return String.format(PARTITION_ENABLED, partition);
    }

    private String formatPartitionMinOffsetKey(int partition) {
        return String.format(PARTITION_MIN_OFFSET, partition);
    }
    private String formatPartitionMaxOffsetKey(int partition) {
        return String.format(PARTITION_MAX_OFFSET, partition);
    }

    private String formatPartitionMinInclusiveOffsetKey(int partition) {
        return String.format(PARTITION_MIN_INCLUSIVE_OFFSET, partition);
    }
    private String formatPartitionMaxInclusiveOffsetKey(int partition) {
        return String.format(PARTITION_MAX_INCLUSIVE_OFFSET, partition);
    }

    private int extractPartitionFromOffsetKey(String key) {
        Matcher matcher = PARTITION_MIN_OFFSET_PATTERN.matcher(key);
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
