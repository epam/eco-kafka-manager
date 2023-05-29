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

import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.Record;
import com.epam.eco.kafkamanager.utils.MapperUtils;
import com.epam.eco.kafkamanager.utils.PrettyHtmlMapper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.record.TimestampType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static java.util.Objects.isNull;

/**
 * @author Andrei_Tytsik
 */
public class TabularRecords implements Iterable<Record> {

    private static final String DATETIME_PATTERN = "dd/MM/yyyy hh:mm:ss a"; // should be the same as in topic_browser.html (see #offsets-timestamp.format)
    private static final String NA = "N/A";

    private static final int TRUNCATE_SIZE = 127;

    private final List<Record> records;
    private final Map<String, Column> columns;

    private final String topicName;

    private final boolean hasSelectedColumns;

    public TabularRecords(List<Record> records, String topicName) {
        this(records, null, topicName);
    }

    public TabularRecords(List<Record> records, Collection<String> selectedColumnNames, String topicName) {
        this.topicName = topicName;
        Validate.notNull(records, "Collection of records is null");
        Validate.noNullElements(records, "Collection of records contains null elements");
        if (selectedColumnNames != null) {
            Validate.noNullElements(
                    selectedColumnNames, "Collection of selected column names contains null elements");
        }

        this.records = records.stream().
                sorted().
                collect(
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                Collections::unmodifiableList));
        this.columns = buildColumns(records, selectedColumnNames);

        hasSelectedColumns = selectedColumnNames != null && !selectedColumnNames.isEmpty();
    }

    private Map<String, Column> buildColumns(List<Record> records, Collection<String> selectedColumnNames) {
        Map<String, Column> columns = new TreeMap<>();

        Set<String> presentColumnNames = new HashSet<>();
        records.forEach(
                record -> presentColumnNames.addAll(record.getColumnNames()));

        presentColumnNames.forEach(
                columnName -> columns.put(
                        columnName,
                        new Column(
                                columnName,
                                true,
                                selectedColumnNames == null || selectedColumnNames.contains(columnName),
                                topicName)));

        if (selectedColumnNames != null) {
            selectedColumnNames.stream()
                   .forEach( columnName ->
                        columns.putIfAbsent(columnName,
                            new Column(columnName, false, true, topicName))
                    );
        }

        return columns;
    }

    public Record getRecord(int index) {
        return records.get(index);
    }

    public List<Record> getRecords() {
        return records;
    }

    public List<Column> listColumns() {
        return new ArrayList<>(columns.values());
    }

    public List<Column> listTopicColumns() {
        return columns.values().stream()
                      .filter(column->topicName.equals(column.getTopicName()))
                      .collect(Collectors.toList());
    }

    public List<String> listColumnsAsString() {
        return columns.values().stream().map(Column::getName).collect(Collectors.toList());
    }

    public List<Column> listSelectedColumns() {
        if (!hasSelectedColumns) {
            return listColumns();
        }
        return columns.values().stream().
                filter(Column::isSelected).
                collect(Collectors.toList());
    }

    public List<Column> listPresentColumns() {
        return columns.values().stream().
                filter(Column::isPresent).
                collect(Collectors.toList());
    }

    public boolean isEmpty() {
        return records.isEmpty();
    }

    public int size() {
        return records.size();
    }

    @Override
    public Iterator<Record> iterator() {
        return records.iterator();
    }

    public static Builder builder(String topicName) {
        return new Builder(topicName);
    }

    public static class Record implements Comparable<Record> {

        private final ConsumerRecord<?, ?> consumerRecord;
        private final Map<String, Object> tabularValue;
        private final Map<String, Object> attributes;

        private final Map<String, String> headers;

        private final RecordSchema registrySchema;

        private final ObjectMapper mapper = new ObjectMapper();

        private String attributesJson;
        private String attributesPrettyJson;
        private String headersJson;
        private String headersPrettyJson;

        public Record(
                ConsumerRecord<?, ?> consumerRecord,
                Map<String, Object> tabularValue,
                Map<String, Object> attributes,
                Map<String, String> headers,
                RecordSchema registrySchema) {

            Validate.notNull(consumerRecord, "Consumer Record is null");

            this.registrySchema = registrySchema;
            this.consumerRecord = consumerRecord;
            this.tabularValue = tabularValue != null ? new TreeMap<>(tabularValue) : null;
            this.attributes = attributes != null ? new TreeMap<>(attributes) : null;
            this.headers = headers;
        }

        public int getPartition() {
            return consumerRecord.partition();
        }

        public long getOffset() {
            return consumerRecord.offset();
        }

        public long getTimestamp() {
            return consumerRecord.timestamp();
        }

        public Map<String, String> getHeaders() {
            return headers;
        }

        public TimestampType getTimestampType() {
            return consumerRecord.timestampType();
        }

        public String getTimestampFormatted() {
            return formatTimestamp(getTimestamp(), getTimestampType());
        }

        public int getKeySize() {
            return consumerRecord.serializedKeySize();
        }

        public String getKeySizeFormatted() {
            return formatSize(getKeySize());
        }

        public int getValueSize() {
            return consumerRecord.serializedValueSize();
        }

        public String getValueSizeFormatted() {
            return formatSize(getValueSize());
        }

        public RecordSchema getRegistrySchema() {
            return registrySchema;
        }

        public String getKeyValueSizeFormatted() {
            String keySize = getKeySizeFormatted();
            keySize = keySize != null ? keySize : "-";

            String valueSize = getValueSizeFormatted();
            valueSize = valueSize != null ? valueSize : "-";

            return keySize + " / " + valueSize;
        }

        public Object getKey() {
            return consumerRecord.key();
        }

        public boolean isNullKey() {
            return getKey() == null;
        }

        public boolean isNullValue() {
            return tabularValue == null;
        }

        public Object get(Column column) {
            return get(column.getName());
        }
        public Object getShort(Column column) {
            return getShort(column.getName());
        }
        public boolean isTruncated(Column column) {
            return isTruncated(column.getName());
        }
        public Object get(String columnName) {
            return tabularValue != null ? tabularValue.get(columnName) : null;
        }
        public String getShort(String columnName) {
            if(isNull(tabularValue) || isNull(tabularValue.get(columnName))) {
                return null;
            }
            String truncatedValue = tabularValue.get(columnName).toString();
            if(truncatedValue.length()>TRUNCATE_SIZE) {
                truncatedValue = truncatedValue.substring(0,TRUNCATE_SIZE);
            }
            return truncatedValue;

        }
        public boolean isTruncated(String columnName) {
            if(isNull(tabularValue) || isNull(tabularValue.get(columnName))) {
                return false;
            }
            return tabularValue.get(columnName).toString().length()>TRUNCATE_SIZE;

        }
        public Class<?> type(Column column) {
            return type(column.getName());
        }

        public Class<?> type(String columnName) {
            Object value = get(columnName);
            return value != null ? value.getClass() : null;
        }

        public String typeSimpleName(Column column) {
            return typeSimpleName(column.getName());
        }

        public String typeSimpleName(String columnName) {
            Class<?> type = type(columnName);
            return type != null ? type.getSimpleName() : NA;
        }

        public boolean containsColumn(Column column) {
            return containsColumn(column.getName());
        }

        public boolean containsColumn(String columnName) {
            return tabularValue != null && tabularValue.containsKey(columnName);
        }

        public Set<String> getColumnNames() {
            return tabularValue != null ? tabularValue.keySet() : Collections.emptySet();
        }

        public boolean hasAttributes() {
            return attributes != null && !attributes.isEmpty();
        }

        public boolean hasHeaders() {
            return headers != null && !headers.isEmpty();
        }

        public String getAttributesJson() {
            if (!hasAttributes()) {
                return null;
            }
            if (attributesJson != null) {
                return attributesJson;
            }

            attributesJson = MapperUtils.toJson(attributes);
            return attributesJson;
        }

        public String getHeadersJson() {
            if (!hasHeaders()) {
                return null;
            }
            if (headersJson != null) {
                return headersJson;
            }

            headersJson = MapperUtils.toJson(headers);
            return headersJson;
        }

        public String getAttributesPrettyJson() {
            if (!hasAttributes()) {
                return null;
            }
            if (attributesPrettyJson != null) {
                return attributesPrettyJson;
            }

            attributesPrettyJson = PrettyHtmlMapper.toPretty(attributes, PrettyHtmlMapper.PrettyFormat.HTML);
            return attributesPrettyJson;
        }

        public String getHeadersPrettyJson() {
            if (!hasHeaders()) {
                return null;
            }
            if (headersPrettyJson != null) {
                return headersPrettyJson;
            }

            headersPrettyJson = PrettyHtmlMapper.toPretty(headers,PrettyHtmlMapper.PrettyFormat.HTML);

            return headersPrettyJson;
        }

        public String getContentPrettyJson(Column column) {
            Object content = get(column);
            if(content instanceof Map) {
                return PrettyHtmlMapper.toPretty((Map)content, PrettyHtmlMapper.PrettyFormat.JSON);
            }
            try {
                return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(mapper.readTree(content.toString()));
            } catch (JsonProcessingException e) {
                return content.toString();
            }
        }

        @Override // is not consistent with equals
        public int compareTo(Record that) {
            int result = ObjectUtils.compare(this.getPartition(), that.getPartition());
            if (result == 0) {
                result = ObjectUtils.compare(this.getOffset(), that.getOffset());
            }
            return result;
        }

        private static String formatTimestamp(long timestamp, TimestampType timestampType) {
            if (timestamp < 0) {
                return null;
            }

            String formatted = DateFormatUtils.formatUTC(timestamp, DATETIME_PATTERN);

            if (TimestampType.CREATE_TIME == timestampType) {
                formatted += " (client)";
            } else if (TimestampType.LOG_APPEND_TIME == timestampType) {
                formatted += " (server)";
            }

            return formatted;
        }

        private static String formatSize(long sizeInBytes) {
            if (sizeInBytes < 0) {
                return null;
            }

            return FileUtils.byteCountToDisplaySize(sizeInBytes);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Record record = (Record) o;
            return Objects.equals(consumerRecord, record.consumerRecord) &&
                    Objects.equals(tabularValue, record.tabularValue) &&
                    Objects.equals(attributes, record.attributes);
        }

        @Override
        public int hashCode() {
            return Objects.hash(consumerRecord, tabularValue, attributes);
        }
    }

    public static class Column {

        private final String name;
        private final boolean present;
        private final boolean selected;
        private final String topicName;

        private Column(String name,
                       boolean present,
                       boolean selected,
                       String topicName) {

            Validate.notNull(name, "Name is null");
            Validate.notNull(topicName, "Topic name is null");

            this.name = name;
            this.present = present;
            this.selected = selected;
            this.topicName = topicName;
        }

        public String getName() {
            return name;
        }

        public boolean isPresent() {
            return present;
        }

        public boolean isSelected() {
            return selected;
        }

        public String getTopicName() {
            return topicName;
        }
    }

    public static class Builder {

        private final String topicName;
        private final Set<String> selectedColumnNames = new HashSet<>();
        private final List<Record> records = new ArrayList<>();

        public Builder(String topicName) {
            this.topicName = topicName;
        }

        public Builder addSelectedColumnName(String columnName) {
            this.selectedColumnNames.add(columnName);
            return this;
        }

        public Builder addSelectedColumnNames(Collection<String> columnNames) {
            this.selectedColumnNames.addAll(columnNames);
            return this;
        }

        public Builder addRecord(Record record) {
            this.records.add(record);
            return this;
        }

        public Builder addRecords(Collection<Record> records) {
            this.records.addAll(records);
            return this;
        }

        public TabularRecords build() {
            return new TabularRecords(records, selectedColumnNames, topicName);
        }

    }

}
