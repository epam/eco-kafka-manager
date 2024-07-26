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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import java.util.stream.Collectors;

import com.epam.eco.kafkamanager.Authorizer;
import com.epam.eco.kafkamanager.EntityType;
import com.epam.eco.kafkamanager.FilterClause;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.PartitionByKeyResolver;
import com.epam.eco.kafkamanager.TopicRecordFetchParams;

import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.TopicPartition;
import org.jetbrains.annotations.NotNull;
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
import com.epam.eco.kafkamanager.TopicRecordFetchParams.DataFormat;
import com.epam.eco.kafkamanager.exec.TaskResult;
import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;
import com.epam.eco.kafkamanager.ui.config.TopicBrowser;
import com.epam.eco.kafkamanager.ui.topics.TopicController;
import com.epam.eco.kafkamanager.ui.topics.browser.filter.FilterClauseCompositePredicate;
import com.epam.eco.kafkamanager.ui.topics.browser.handlers.FilterOperationEnum;
import com.epam.eco.kafkamanager.utils.PrettyHtmlMapper;

import static com.epam.eco.kafkamanager.ui.topics.browser.filter.FilterClauseCompositePredicate.TOMBSTONE_ATTRIBUTE;
import static com.epam.eco.kafkamanager.ui.topics.browser.pedicates.FilterClauseAbstractKeyPredicate.*;

import static com.epam.eco.kafkamanager.ui.utils.HeaderUtils.getReplacedHeaderMap;
import static java.util.Objects.nonNull;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class TopicBrowserController {

    public static final String VIEW = "topic_browser";
    public static final String MAPPING = TopicController.MAPPING_TOPIC + "/browser";
    public static final String ATTR_BROWSE_PARAMS = "browseParams";
    public static final String ATTR_SHOW_GRID = "showGrid";
    public static final String ATTR_ENABLE_ANIMATION = "enableAnimation";
    public static final String ATTR_OFFSET_RANGES = "offsetRanges";
    public static final String ATTR_REAL_RANGE_BOUNDS = "realRangeBounds";
    public static final String ATTR_OFFSET_RANGES_SUMMARY = "offsetRangesSummary";
    public static final String ATTR_OFFSET_FETCHED_RANGES_SUMMARY = "offsetFetchedRangesSummary";
    public static final String ATTR_FETCHED_RECORDS = "fetchedRecords";
    public static final String ATTR_COLUMNS_LIST = "columnsList";
    public static final String ATTR_FETCH_SUMMARY = "fetchSummary";
    public static final String ATTR_CURR_OFFSETS = "currentOffsets";
    public static final String ATTR_HAS_NEXT_OFFSETS = "hasNextOffsets";
    public static final String ATTR_HAS_PREVIOUS_OFFSETS = "hasPreviousOffsets";
    public static final String ATTR_SCHEMA_CATALOG_URL_TEMPLATE = "schemaCatalogUrlTemplate";
    public static final String ATTR_FILTER_CLAUSE = "filter-clause";
    public static final String ATTR_FILTER_OPERATIONS = "filterOperations";
    public static final String ATTR_WRITE_ALLOWED = "writeAllowed";

    public static final String INITIAL_FILTER_ATTRIBUTE = "initialFilterColumns";
    public static final Set<String> INITIAL_FILTER_COLUMNS = Set.of(TOMBSTONE_ATTRIBUTE,KEY_ATTRIBUTE);
    private static final long DEFAULT_FETCH_TIMEOUT = 30_000;

    @Autowired
    private KafkaManager kafkaManager;

    @Autowired
    private KafkaAdminOperations kafkaAdminOperations;

    @Autowired
    private KafkaManagerUiProperties properties;

    @Autowired
    private PartitionByKeyResolver partitionByKeyResolver;

    @Autowired
    private Authorizer authorizer;

    @Autowired
    private KafkaRecordRepublisher republisher;


    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String params(
            @PathVariable("name") String topicName,
            Model model) {
        TopicBrowseParams browseParams = (TopicBrowseParams) model.asMap().get(ATTR_BROWSE_PARAMS);
        if (browseParams==null) {
            browseParams = TopicBrowseParams.with(null);
            browseParams.setTopicName(topicName);
        }

        handleParamsRequest(browseParams, model::addAttribute);
        model.addAttribute(ATTR_SCHEMA_CATALOG_URL_TEMPLATE, properties.getSchemaCatalogTool());
        model.addAttribute(ATTR_WRITE_ALLOWED, authorizer.isPermitted(EntityType.TOPIC, topicName,
                                                                      Authorizer.Operation.WRITE));
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

    @PreAuthorize("@authorizer.isPermitted('TOPIC', #topicName, 'WRITE')")
    @RequestMapping(value=MAPPING + "/republishRecord", method=RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> republishRecord(
            @PathVariable("name") String topicName,
            @RequestParam ReplacementType replacementType,
            @RequestParam String recordId,
            @RequestParam String headers,
            @RequestParam Integer timeout) {
        return republisher.republish(
                topicName,
                replacementType,
                recordId,
                headers,
                timeout);
    }

    @RequestMapping(value=MAPPING + "/headers", method=RequestMethod.POST)
    public @ResponseBody ResponseEntity<String> replaceHeaders(
            @RequestParam ReplacementType replacementType,
            @RequestParam(name="headers") String headers) {

        if(StringUtils.isEmpty(headers)) {
            return ResponseEntity.ok("{}");
        }
        Map<String, String> headerMap;
        try {
            headerMap = getReplacedHeaderMap(
                    replacementType,
                    headers,
                    properties.getTopicBrowser());
            return ResponseEntity.ok(PrettyHtmlMapper.toPretty(headerMap, PrettyHtmlMapper.PrettyFormat.JSON));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private void handleParamsRequest(
            TopicBrowseParams browseParams,
            BiConsumer<String, Object> modelAttributes) {
        Map<Integer, OffsetRange> offsetRanges = fetchOffsetRanges(browseParams.getTopicName());

        setDefaultDataFormatsIfMissing(browseParams);
        populateMissingAndFixInvalidOffsets(offsetRanges, browseParams);
        OffsetRange offsetRangesSummary = getOffsetRangesSummary(offsetRanges);

        addTopicConfigParams(browseParams);

        TopicBrowser topicBrowser = properties.getTopicBrowser();

        modelAttributes.accept(ATTR_BROWSE_PARAMS, browseParams);
        modelAttributes.accept(ATTR_OFFSET_RANGES, offsetRanges);
        modelAttributes.accept(INITIAL_FILTER_ATTRIBUTE, INITIAL_FILTER_COLUMNS);
        modelAttributes.accept(ATTR_SHOW_GRID, nonNull(topicBrowser) ? topicBrowser.getShowGrid() : Boolean.TRUE);
        modelAttributes.accept(ATTR_ENABLE_ANIMATION,  nonNull(topicBrowser) ? topicBrowser.getEnableAnimation() : Boolean.TRUE);
        modelAttributes.accept(ATTR_FILTER_OPERATIONS, FilterOperationEnum.getFilterOperations());

        modelAttributes.accept(ATTR_OFFSET_RANGES_SUMMARY, offsetRangesSummary);

    }

    private void addTopicConfigParams(TopicBrowseParams browserParams) {
        browserParams.setKafkaTopicConfig(kafkaAdminOperations.describeTopicConfig(browserParams.getTopicName()));
    }

    private void handleFetchRequest(TopicBrowseParams browseParams,
                                    BiConsumer<String, Object> modelAttributes) {

        TopicRecordFetchParams<String,Object> fetchParams = toFetchParams(browseParams);

        TaskResult<RecordFetchResult<Object, Object>> taskResult = kafkaManager.getTopicRecordFetcherTaskExecutor().
                executeDetailed(browseParams.getTopicName(), fetchParams);

        RecordFetchResult<Object, Object> fetchResult = taskResult.getValue();

        TabularRecords tabularRecords = ToTabularRecordsConverter.from(browseParams, fetchResult);

        List<String> columns = new TreeList<>();
        columns.add(KEY_ATTRIBUTE);
        columns.add(TOMBSTONE_ATTRIBUTE);
        columns.addAll(tabularRecords.getHeaderFilterLabels());
        columns.addAll(tabularRecords.listColumnsAsString());

        modelAttributes.accept(ATTR_FILTER_CLAUSE, browseParams.getFilterClauses());
        modelAttributes.accept(ATTR_COLUMNS_LIST, columns);

        modelAttributes.accept(ATTR_FETCHED_RECORDS, tabularRecords);
        modelAttributes.accept(ATTR_FETCH_SUMMARY, buildFetchSummary(taskResult));

        OffsetRange offsetRangesSummary = getFetchOffsetRangesSummary(fetchResult);
        modelAttributes.accept(ATTR_OFFSET_FETCHED_RANGES_SUMMARY, offsetRangesSummary);
        modelAttributes.accept(ATTR_REAL_RANGE_BOUNDS, getRealRangeBounds(fetchResult));
        modelAttributes.accept(ATTR_CURR_OFFSETS, getCurrentOffsetRange(fetchResult));
        modelAttributes.accept(ATTR_HAS_NEXT_OFFSETS, isNextOffsetRangeAvailable(fetchResult));
        modelAttributes.accept(ATTR_HAS_PREVIOUS_OFFSETS, isPreviousOffsetRangeAvailable(fetchResult));
    }

    private <K,V> TopicRecordFetchParams<K,V> toFetchParams(TopicBrowseParams browseParams) {
        return new TopicRecordFetchParams<>(
                browseParams.getKeyFormat(),
                browseParams.getValueFormat(),
                getFetchedOffsets(browseParams),
                browseParams.getLimit(),
                browseParams.getTimeout() > 0 ? browseParams.getTimeout() : DEFAULT_FETCH_TIMEOUT,
                browseParams.getFetchMode(),
                browseParams.getTimestamp(),
                properties.getTopicBrowser().getUseCache(),
                properties.getTopicBrowser().getCacheExpirationPeriodMin(),
                new FilterClauseCompositePredicate<>(browseParams)
        );
    }

    private Map<Integer, OffsetRange> getFetchedOffsets(TopicBrowseParams browseParams) {
        return properties.getTopicBrowser().isFilterByKeyPartition() && isItSearchByKeyEqualsClause(browseParams) ?
                   getOffsetsFilteredOffsetsByKeyPartition(browseParams) :
                   browseParams.getPartitionOffsets();
    }

    @NotNull
    private Map<Integer, OffsetRange> getOffsetsFilteredOffsetsByKeyPartition(TopicBrowseParams browseParams) {
        return browseParams.getPartitionOffsets().entrySet().stream()
                   .filter(entry -> entry.getKey().equals(getKeyPartition(browseParams)))
                   .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private boolean isItSearchByKeyEqualsClause(TopicBrowseParams browseParams) {
        Map<String,List<FilterClause>> clauses = browseParams.getFilterClausesAsMap();
        return clauses.containsKey(KEY_ATTRIBUTE)
                  && clauses.get(KEY_ATTRIBUTE).stream()
                                 .anyMatch(clause->FilterOperationEnum.getOperationEnum(clause.getOperation())==FilterOperationEnum.EQUALS);
    }

    private int getKeyPartition(TopicBrowseParams browseParams) {
        Map<String,List<FilterClause>> clauses = browseParams.getFilterClausesAsMap();
        FilterClause filterClause = clauses.get(KEY_ATTRIBUTE).stream()
                .filter(clause->FilterOperationEnum.getOperationEnum(clause.getOperation())==FilterOperationEnum.EQUALS)
                .findFirst()
                .orElse(null);
        return nonNull(filterClause) ?
                   partitionByKeyResolver.getPartitionByKey(filterClause.getValue(),browseParams.getPartitionOffsets().size()) : 0;
    }

    private Map<Integer, OffsetRange> fetchOffsetRanges(String topicName) {
        Map<TopicPartition, OffsetRange> ranges = kafkaManager
                .getTopicOffsetRangeFetcherTaskExecutor().getResultIfActualOrRefresh(topicName).getValue();
        return ranges.entrySet().stream().
                collect(
                Collectors.toMap(
                        entry -> entry.getKey().partition(),
                        Map.Entry::getValue));
    }

    private OffsetRange getOffsetRangesSummary(Map<Integer, OffsetRange> offsetRanges) {
        long smallest = offsetRanges.values().stream()
                                    .map(OffsetRange::getSmallest).min(Comparator.naturalOrder())
                                    .orElse(0L);
        OffsetRange largest = offsetRanges.values().stream()
                                          .max(Comparator.comparing(OffsetRange::getLargest))
                                          .orElse(OffsetRange.with(smallest,smallest,false));
        return OffsetRange.with(smallest,largest.getLargest(),largest.isLargestInclusive());
    }

    private OffsetRange getFetchOffsetRangesSummary(RecordFetchResult<Object, Object> fetchResult) {
        long smallest = fetchResult.getPerPartitionResults().stream()
                                   .map(result->result.getScannedOffsets().getSmallest())
                                   .min(Comparator.naturalOrder())
                                   .orElse(0L);
        OffsetRange largest = fetchResult.getPerPartitionResults().stream()
                                         .map(PartitionRecordFetchResult::getScannedOffsets)
                                         .max(Comparator.comparing(OffsetRange::getLargest))
                                         .orElse(OffsetRange.with(smallest,smallest,false));
        return OffsetRange.with(smallest,largest.getLargest(),largest.isLargestInclusive());
    }

    private Map<TopicPartition,OffsetRange> getRealRangeBounds(RecordFetchResult<Object, Object> fetchResult) {
        return fetchResult.getPerPartitionResults().stream()
                          .collect(Collectors.toMap(
                                  PartitionRecordFetchResult::getPartition,
                                  PartitionRecordFetchResult::getPartitionOffsets));

    }

    private Map<Integer, OffsetRange> getCurrentOffsetRange(
            RecordFetchResult<Object, Object> fetchResult) {
        return fetchResult.getPerPartitionResults().stream()
                          .collect(Collectors.toMap(
                                  perPartitionResult -> perPartitionResult.getPartition().partition(),
                                  PartitionRecordFetchResult::getScannedOffsets));
    }

    private boolean isNextOffsetRangeAvailable( RecordFetchResult<Object, Object> fetchResult) {
        for(PartitionRecordFetchResult<Object, Object> perPartitionResult : fetchResult.getPerPartitionResults()) {
            if(perPartitionResult.getScannedOffsets().getLargest() < perPartitionResult.getPartitionOffsets().getLargest()) {
                return true;
            }
        }
        return false;
    }

    private boolean isPreviousOffsetRangeAvailable( RecordFetchResult<Object, Object> fetchResult) {
        for(PartitionRecordFetchResult<Object, Object> perPartitionResult : fetchResult.getPerPartitionResults()) {
            if(perPartitionResult.getScannedOffsets().getSmallest() > perPartitionResult.getPartitionOffsets().getSmallest()) {
                return true;
            }
        }
        return false;
    }

    private String buildFetchSummary(TaskResult<RecordFetchResult<Object, Object>> workerResult) {
        return String.format(
                "%d record(s) fetched in %s",
                workerResult.getValue().count(), workerResult.getElapsedFormattedAsHMS());
    }

    private void setDefaultDataFormatsIfMissing(TopicBrowseParams fetchRequestParams) {
        fetchRequestParams.setKeyFormatIfMissing(DataFormat.STRING);
        fetchRequestParams.setValueFormatIfMissing(DataFormat.AVRO);
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
            OffsetRange paramOffsets = fetchRequestParams.getPartitionOffset(partition);
            if(!fetchRequestParams.containsPartition(partition)) {
                fetchRequestParams.addPartitionOffset(partition, range);

            } else if (paramOffsets.getSmallest() < range.getSmallest() &&
                    paramOffsets.getLargest() < range.getSmallest()) {
                fetchRequestParams.addPartitionOffset(partition,
                                                      OffsetRange.with(range.getSmallest(), range.isSmallestInclusive(),
                                                                       range.getLargest(),range.isLargestInclusive()));

            } else if (paramOffsets.getSmallest() < range.getSmallest() &&
                    paramOffsets.getLargest() >= range.getSmallest()) {
                fetchRequestParams.addPartitionOffset(partition,
                                                      OffsetRange.with(range.getSmallest(), range.isSmallestInclusive(),
                                                                       paramOffsets.getLargest(),range.isLargestInclusive()));

            } else if (paramOffsets.getLargest() > range.getLargest() &&
                    paramOffsets.getSmallest() < range.getLargest() ) {
                fetchRequestParams.addPartitionOffset(partition,
                                                      OffsetRange.with(paramOffsets.getSmallest(), paramOffsets.isSmallestInclusive(),
                                                                       range.getLargest(), range.isLargestInclusive()));
            }
            else if (paramOffsets.getLargest()> range.getLargest() &&
                    paramOffsets.getSmallest()>range.getLargest() ) {
                fetchRequestParams.addPartitionOffset(partition,
                                                      OffsetRange.with(paramOffsets.getSmallest(), range.getLargest(), range.isLargestInclusive()));
            }
        });
    }

    public static String buildBrowserUrl(String topicName) {
        return MAPPING.replace("{name}", topicName);
    }


}