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
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.epam.eco.commons.kafka.OffsetRange;
import com.epam.eco.commons.kafka.helpers.PartitionRecordFetchResult;
import com.epam.eco.commons.kafka.helpers.RecordFetchResult;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.ui.topics.TopicController;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class TopicBrowserController {

    public static final String VIEW = "topic_browser";

    public static final String MAPPING = TopicController.MAPPING_TOPIC + "/browser";
    public static final String MAPPING_OFFSETS_FOR_TIMES = MAPPING + "/offsets_for_times";

    public static final String ATTR_TOPIC_NAME = "topicName";
    public static final String ATTR_BROWSE_PARAMS = "browseParams";
    public static final String ATTR_OFFSET_RANGES = "offsetRanges";
    public static final String ATTR_FETCHED_RECORDS = "fetchedRecords";
    public static final String ATTR_FETCH_SUMMARY = "fetchSummary";
    public static final String ATTR_NEXT_OFFSETS = "nextOffsets";
    public static final String ATTR_OFFSETS_FOR_TIMES = "offsetsForTimes";

    private static final long DEFAULT_FETCH_TIMEOUT = 30_000;

    @Autowired
    private KafkaManager kafkaManager;

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String params(
            @PathVariable("name") String topicName,
            Model model) {
        TopicBrowseParams browseParams = (TopicBrowseParams)model.asMap().get(ATTR_BROWSE_PARAMS);
        if (browseParams == null) {
            browseParams = TopicBrowseParams.with(null);
            browseParams.setTopicName(topicName);
        }

        handleParamsRequest(browseParams, model::addAttribute);

        return VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.POST)
    public String fetch(
            @PathVariable("name") String topicName,
            @RequestParam Map<String, Object> requestParams,
            RedirectAttributes redirectAttrs) {
        TopicBrowseParams browseParams = TopicBrowseParams.with(requestParams);
        browseParams.setTopicName(topicName);

        handleParamsRequest(browseParams, redirectAttrs::addFlashAttribute);
        handleFetchRequest(browseParams, redirectAttrs::addFlashAttribute);

        return "redirect:" + buildBrowserUrl(topicName);
    }

    @RequestMapping(value=MAPPING_OFFSETS_FOR_TIMES, method=RequestMethod.GET)
    public @ResponseBody
    ResponseEntity<?> fetchOffsetsForTimes(
            @PathVariable("name") String topicName,
            @RequestParam(name="timestamp", required=true) Long timestamp) {
        Map<TopicPartition, Long> offsetsForTimes =
                kafkaManager.getTopicOffsetForTimeFetcherTaskExecutor().execute(topicName, timestamp);
        Map<Integer, Long> offsets = offsetsForTimes.entrySet().stream().
                filter(e -> e.getValue() != null).
                collect(Collectors.toMap(
                        entry -> entry.getKey().partition(),
                        entry -> entry.getValue()));
        return ResponseEntity.ok().body(offsets);
    }

    private void handleParamsRequest(
            TopicBrowseParams browseParams,
            BiConsumer<String, Object> modelAttributes) {
        Map<Integer, OffsetRange> offsetRanges = fetchOffsetRanges(browseParams.getTopicName());

        setDefaultDataFormatsIfMissing(browseParams);
        populateMissingAndFixInvalidOffsets(offsetRanges, browseParams);

        modelAttributes.accept(ATTR_BROWSE_PARAMS, browseParams);
        modelAttributes.accept(ATTR_OFFSET_RANGES, offsetRanges);
    }

    private void handleFetchRequest(TopicBrowseParams browseParams, BiConsumer<String, Object> modelAttributes) {
        TopicRecordFetchParams fetchParams = toFetchParams(browseParams);

        TaskResult<RecordFetchResult<Object, Object>> taskResult = kafkaManager.getTopicRecordFetcherTaskExecutor().
                executeDetailed(browseParams.getTopicName(), fetchParams);

        RecordFetchResult<Object, Object> fetchResult = taskResult.getValue();

        TabularRecords tabularRecords = ToTabularRecordsConverter.from(browseParams, fetchResult);

        modelAttributes.accept(ATTR_FETCHED_RECORDS, tabularRecords);
        modelAttributes.accept(ATTR_FETCH_SUMMARY, buildFetchSummary(taskResult));
        modelAttributes.accept(ATTR_NEXT_OFFSETS, getNextOffsetsOrNullIfEndOfTopic(fetchResult));
    }

    private TopicRecordFetchParams toFetchParams(TopicBrowseParams browseParams) {
        return new TopicRecordFetchParams(
                browseParams.getKeyFormat(),
                browseParams.getValueFormat(),
                browseParams.getPartitionOffsets(),
                browseParams.getLimit(),
                browseParams.getTimeout() > 0 ? browseParams.getTimeout() : DEFAULT_FETCH_TIMEOUT);
    }

    private Map<Integer, OffsetRange> fetchOffsetRanges(String topicName) {
        Map<TopicPartition, OffsetRange> ranges = kafkaManager.
                getTopicOffsetRangeFetcherTaskExecutor().getResultIfActualOrRefresh(topicName).getValue();
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

    private void setDefaultDataFormatsIfMissing(TopicBrowseParams fetchRequestParams) {
        fetchRequestParams.setKeyFormatIfMissing(DataFormat.STRING);
        fetchRequestParams.setValueFormatIfMissing(DataFormat.STRING);
    }

    private void populateMissingAndFixInvalidOffsets(
            Map<Integer, OffsetRange> offsetRanges,
            TopicBrowseParams fetchRequestParams) {
        fetchRequestParams.listPartitions().forEach(partition -> {
            if (!offsetRanges.containsKey(partition)) {
                fetchRequestParams.removePartitionOffset(partition);
            }
        });
        offsetRanges.forEach((key, range) -> {
            int partition = key;
            long offset = fetchRequestParams.getPartitionOffset(partition);
            if (offset < range.getSmallest() || !fetchRequestParams.containsPartition(partition)) {
                fetchRequestParams.addPartitionOffset(partition, range.getSmallest());
            } else if (offset > range.getLargest()) {
                fetchRequestParams.addPartitionOffset(partition, range.getLargest());
            }
        });
    }

    public static String buildBrowserUrl(String topicName) {
        return MAPPING.replace("{name}", topicName);
    }

}
