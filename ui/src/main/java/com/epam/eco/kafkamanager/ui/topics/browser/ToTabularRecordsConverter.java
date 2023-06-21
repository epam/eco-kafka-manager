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

import org.apache.commons.lang3.Validate;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;
import com.epam.eco.kafkamanager.ui.topics.browser.TabularRecords.Record;

/**
 * @author Andrei_Tytsik
 */
public class ToTabularRecordsConverter {

    public static TabularRecords from(
            TopicBrowseParams browseParams,
            RecordFetchResult<?, ?> fetchResult) {
        Validate.notNull(browseParams, "Params object is null");
        Validate.notNull(fetchResult, "Result is null");

        RecordValueTabulator<?> valueTabulator = determineValueTabulator(browseParams);

        TabularRecords.Builder builder = TabularRecords.builder(browseParams.getTopicName());
        for (ConsumerRecord<?, ?> record : fetchResult) {
            Record tabularRecord = toTabularRecord(record, valueTabulator);
            builder.addRecord(tabularRecord);
        }

        List<String> selectedColumns = browseParams.listColumns();
        if (selectedColumns != null && !selectedColumns.isEmpty()) {
            builder.addSelectedColumnNames(selectedColumns);
        }

        return builder.build();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Record toTabularRecord(
            ConsumerRecord<?, ?> consumerRecord,
            RecordValueTabulator<?> valueTabulator) {
        Map<String, Object> tabularValue =
                valueTabulator.toTabularValue((ConsumerRecord)consumerRecord);

        Map<String, Object> attributes =
                valueTabulator.getAttributes((ConsumerRecord)consumerRecord);

        Map<String, String> headers = new HashMap<>();
        consumerRecord.headers().forEach( header -> headers.put(header.key(),new String(header.value())));

        return new Record(
                consumerRecord,
                tabularValue,
                attributes,
                headers,
                valueTabulator.getSchema(consumerRecord));
    }

    private static RecordValueTabulator<?> determineValueTabulator(
            TopicBrowseParams browseParams) {
        DataFormat dataFormat = browseParams.getValueFormat();
        if (DataFormat.AVRO == dataFormat) {
            return new AvroRecordValueTabulator(browseParams.getKafkaTopicConfig());
        } else if (DataFormat.PROTOCOL_BUFFERS == dataFormat) {
            return new ProtobufRecordValueTabulator(browseParams.getKafkaTopicConfig());
        } else if (
                DataFormat.STRING == dataFormat ||
                        DataFormat.JSON_STRING == dataFormat ||
                        DataFormat.HEX_STRING == dataFormat) {
            return new NoopRecordValueTabulator();
        } else {
            throw new RuntimeException(
                    String.format("Data format not supported: %s", dataFormat));
        }
    }

}
