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
package com.epam.eco.kafkamanager.ui.topics.browser;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.PartitionRecordFetchResult;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.RecordFetchRequest;
import com.epam.eco.kafkamanager.RecordFetchRequest.DataFormat;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.ui.topics.TopicController;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class TopicBrowserController {

    public static final String VIEW = "topic_browser";

    public static final String MAPPING = TopicController.MAPPING_TOPIC + "/browser";

    public static final String ATTR_TOPIC_NAME = "topicName";
    public static final String ATTR_FETCH_PARAMS = "fetchParams";
    public static final String ATTR_OFFSET_RANGES = "offsetRanges";
    public static final String ATTR_FETCHED_RECORDS = "fetchedRecords";
    public static final String ATTR_FETCH_SUMMARY = "fetchSummary";
    public static final String ATTR_NEXT_OFFSETS = "nextOffsets";

    private static final long DEFAULT_FETCH_TIMEOUT = 30_000;

    @Autowired
    private KafkaManager kafkaManager;

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String params(
            @PathVariable("name") String topicName,
            Model model) {
        RecordFetchParams fetchParams = (RecordFetchParams)model.asMap().get(ATTR_FETCH_PARAMS);
        if (fetchParams == null) {
            fetchParams = RecordFetchParams.with(null);
            fetchParams.setTopicName(topicName);
        }

        handleParamsRequest(fetchParams, model::addAttribute);

        return VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.POST)
    public String fetch(
            @PathVariable("name") String topicName,
            @RequestParam Map<String, Object> requestParams,
            RedirectAttributes redirectAttrs) {
        RecordFetchParams fetchParams = RecordFetchParams.with(requestParams);
        fetchParams.setTopicName(topicName);

        handleParamsRequest(fetchParams, redirectAttrs::addFlashAttribute);
        handleFetchRequest(fetchParams, redirectAttrs::addFlashAttribute);

        return "redirect:" + buildBrowserUrl(topicName);
    }

    private void handleParamsRequest(RecordFetchParams fetchParams, BiConsumer<String, Object> modelAttributes) {
        Map<Integer, OffsetRange> offsetRanges = fetchOffsetRanges(fetchParams.getTopicName());

        setDefaultDataFormatsIfMissing(fetchParams);
        populateMissingAndFixInvalidOffsets(offsetRanges, fetchParams);

        modelAttributes.accept(ATTR_FETCH_PARAMS, fetchParams);
        modelAttributes.accept(ATTR_OFFSET_RANGES, offsetRanges);
    }

    private void handleFetchRequest(RecordFetchParams fetchParams, BiConsumer<String, Object> modelAttributes) {
        RecordFetchRequest fetchRequest = toFetchRequest(fetchParams);

        TaskResult<RecordFetchResult<Object, Object>> taskResult = kafkaManager.getTopicRecordFetcherTaskExecutor()
                .executeDetailed(fetchParams.getTopicName(), fetchRequest);

        RecordFetchResult<Object, Object> fetchResult = taskResult.getValue();

        TabularRecords tabularRecords = ToTabularRecordsConverter.from(fetchParams, fetchResult);

        modelAttributes.accept(ATTR_FETCHED_RECORDS, tabularRecords);
        modelAttributes.accept(ATTR_FETCH_SUMMARY, buildFetchSummary(taskResult));
        modelAttributes.accept(ATTR_NEXT_OFFSETS, getNextOffsetsOrNullIfEndOfTopic(fetchResult));
    }

    private RecordFetchRequest toFetchRequest(RecordFetchParams fetchParams) {
        return new RecordFetchRequest(
                fetchParams.getKeyFormat(),
                fetchParams.getValueFormat(),
                fetchParams.getPartitionOffsets(),
                fetchParams.getLimit(),
                fetchParams.getTimeout() > 0 ? fetchParams.getTimeout() : DEFAULT_FETCH_TIMEOUT);
    }

    private Map<Integer, OffsetRange> fetchOffsetRanges(String topicName) {
        Map<TopicPartition, OffsetRange> ranges = kafkaManager.
                getTopicOffsetFetcherTaskExecutor().getResultIfActualOrRefresh(topicName).getValue();
        return ranges.entrySet().stream().
                collect(
                        Collectors.toMap(
                                entry -> entry.getKey().partition(),
                                Map.Entry::getValue));
    }

    private Map<Integer, Long> getNextOffsetsOrNullIfEndOfTopic(
            RecordFetchResult<Object, Object> fetchResult) {
        Map<Integer, Long> nextOffsets = null;
        for (PartitionRecordFetchResult<Object, Object> perPartinionResult : fetchResult.getPerPartitionResults()) {
            if (perPartinionResult.getScannedOffsets().getLargest() < perPartinionResult.getPartitionOffsets().getLargest()) {
                nextOffsets = nextOffsets != null ? nextOffsets : new HashMap<>();
                nextOffsets.put(
                        perPartinionResult.getPartition().partition(),
                        perPartinionResult.getScannedOffsets().getLargest() + 1);
            }
        }
        return nextOffsets;
    }

    private String buildFetchSummary(TaskResult<RecordFetchResult<Object, Object>> workerResult) {
        return String.format(
                "%d record(s) fetched in %s",
                workerResult.getValue().count(), workerResult.getElapsedFormattedAsHMS());
    }

    private void setDefaultDataFormatsIfMissing(RecordFetchParams fetchParams) {
        fetchParams.setKeyFormatIfMissing(DataFormat.STRING);
        fetchParams.setValueFormatIfMissing(DataFormat.STRING);
    }

    private void populateMissingAndFixInvalidOffsets(
            Map<Integer, OffsetRange> offsetRanges,
            RecordFetchParams fetchParams) {
        fetchParams.listPartitions().forEach(partition -> {
            if (!offsetRanges.containsKey(partition)) {
                fetchParams.removePartitionOffset(partition);
            }
        });
        offsetRanges.forEach((key, range) -> {
            int partition = key;
            long offset = fetchParams.getPartitionOffset(partition);
            if (offset < range.getSmallest() || !fetchParams.containsPartition(partition)) {
                fetchParams.addPartitionOffset(partition, range.getSmallest());
            } else if (offset > range.getLargest()) {
                fetchParams.addPartitionOffset(partition, range.getLargest());
            }
        });
    }

    public static String buildBrowserUrl(String topicName) {
        return MAPPING.replace("{name}", topicName);
    }

}
