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

/**
 * @author Andrei_Tytsik
 */
public class TabularRecords implements Iterable<Record> {

    private static final String DATETIME_PATTERN = "dd/MM/yyyy hh:mm:ss a"; // should be the same as in topic_browser.html (see #offsets-timestamp.format)
    private static final String NA = "N/A";

    private final List<Record> records;
    private final Map<String, Column> columns;
    private final boolean hasSelectedColumns;

    public TabularRecords(List<Record> records) {
        this(records, null);
    }

    public TabularRecords(List<Record> records, Collection<String> selectedColumnNames) {
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
                                selectedColumnNames == null || selectedColumnNames.contains(columnName))));

        if (selectedColumnNames != null) {
            selectedColumnNames.forEach(
                    columnName -> {
                        if (!columns.containsKey(columnName)) {
                            columns.put(columnName, new Column(columnName, false, true));
                        }
                    });
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

    public List<Column> getColumnsGroup(int groupIndex, int columnsPerGroup) {
        int from = groupIndex * columnsPerGroup;
        if (from >= columns.size()) {
            return Collections.emptyList();
        }

        int to = from + columnsPerGroup;
        to = to <= columns.size() ? to : columns.size();

        List<Column> columnsAll = listColumns();
        List<Column> columnsGroup = new ArrayList<>(to - from);
        for (int i = from; i < to; i++) {
            columnsGroup.add(columnsAll.get(i));
        }
        return columnsGroup;
    }

    public int determineColumnsGroupSize(int numberOfGroups, int minGroupSize) {
        int size = (int) Math.ceil((double) columns.size() / numberOfGroups);
        return size < minGroupSize ? minGroupSize : size;
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

    public static Builder builder() {
        return new Builder();
    }

    public static class Record implements Comparable<Record> {

        private final ConsumerRecord<?, ?> consumerRecord;
        private final Map<String, Object> tabularValue;
        private final Map<String, Object> attributes;

        private final Map<String, String> headers;
        private String attributesJson;
        private String attributesPrettyJson;
        private String headersJson;
        private String headersPrettyJson;

        public Record(
                ConsumerRecord<?, ?> consumerRecord,
                Map<String, Object> tabularValue,
                Map<String, Object> attributes,
                Map<String, String> headers) {
            Validate.notNull(consumerRecord, "Consumer Record is null");

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

        public Object get(String columnName) {
            return tabularValue != null ? tabularValue.get(columnName) : null;
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

            attributesPrettyJson = PrettyHtmlMapper.toPrettyHtml(attributes);
            return attributesPrettyJson;
        }

        public String getHeadersPrettyJson() {
            if (!hasHeaders()) {
                return null;
            }
            if (headersPrettyJson != null) {
                return headersPrettyJson;
            }

            headersPrettyJson = PrettyHtmlMapper.toPrettyHtml(headers);

            return headersPrettyJson;
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

        private Column(String name, boolean present, boolean selected) {
            Validate.notNull(name, "Name is null");

            this.name = name;
            this.present = present;
            this.selected = selected;
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

    }

    public static class Builder {

        private final Set<String> selectedColumnNames = new HashSet<>();
        private final List<Record> records = new ArrayList<>();

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
            return new TabularRecords(records, selectedColumnNames);
        }

    }

}
