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

import java.util.Map;
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

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.KafkaManager;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class ConsumerGroupOffsetResetterController {

    public static final String VIEW = "consumer_group_offset_resetter";

    public static final String MAPPING = ConsumerGroupController.MAPPING_GROUP + "/offset_resetter";

    public static final String ATTR_GROUP_NAME = "groupName";
    public static final String ATTR_RESET_PARAMS = "resetParams";
    public static final String ATTR_RESETTABLE_RANGES = "resettableRanges";
    public static final String ATTR_GROUP = "group";
    public static final String ATTR_SUCCEED = "succeed";

    @Autowired
    private KafkaManager kafkaManager;

    @PreAuthorize("@authorizer.isPermitted('CONSUMER_GROUP', #groupName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String params(
            @PathVariable("name") String groupName,
            Model model) {
        ConsumerGroupInfo groupInfo = kafkaManager.getConsumerGroup(groupName);

        ResetGroupOffsetsParams resetParams = ResetGroupOffsetsParams.with(null);
        resetParams.setGroupName(groupName);
        populateGroupOffsets(groupInfo, resetParams);

        Map<TopicPartition, ResettableOffsetRange> resettableRanges =
                fetchAndBuildResettableOffsetRanges(groupInfo);

        model.addAttribute(ATTR_GROUP, ConsumerGroupInfoWrapper.wrap(groupInfo));
        model.addAttribute(ATTR_RESET_PARAMS, resetParams);
        model.addAttribute(ATTR_RESETTABLE_RANGES, resettableRanges);

        return VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('CONSUMER_GROUP', #groupName, 'READ')")
    @RequestMapping(value=MAPPING, method=RequestMethod.POST)
    public String reset(
            @PathVariable("name") String groupName,
            @RequestParam Map<String, Object> requestParams,
            RedirectAttributes redirectAttrs) {
        ResetGroupOffsetsParams resetParams = ResetGroupOffsetsParams.with(requestParams);
        resetParams.setGroupName(groupName);
        ConsumerGroupInfo groupInfo = kafkaManager.getConsumerGroup(resetParams.getGroupName());

        Map<TopicPartition, Long> offsets = resetParams.getPartitionOffsets();
        if (offsets.isEmpty()) {
            throw new RuntimeException("No offsets selected");
        }

        offsets = filterOutUselessOffsets(offsets, groupInfo);
        if (offsets.isEmpty()) {
            throw new RuntimeException(
                    "Selected offsets are equal to current consumer group offsets");
        }

        kafkaManager.getConsumerGroupOffsetResetterTaskExecutor().execute(groupName, offsets);

        redirectAttrs.addFlashAttribute(ATTR_SUCCEED, Boolean.TRUE);

        return "redirect:" + buildOffsetResetterUrl(groupName);
    }

    private void populateGroupOffsets(
            ConsumerGroupInfo groupInfo,
            ResetGroupOffsetsParams resetParams) {
        groupInfo.getOffsets().forEach(resetParams::addPartitionOffset);
    }

    private Map<TopicPartition, Long> filterOutUselessOffsets(
            Map<TopicPartition, Long> offsets,
            ConsumerGroupInfo groupInfo) {
        return offsets.entrySet().stream().
                filter(entry -> !entry.getValue().equals(groupInfo.getOffset(entry.getKey()))).
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue));
    }

    private Map<TopicPartition, ResettableOffsetRange> fetchAndBuildResettableOffsetRanges(
            ConsumerGroupInfo groupInfo) {
        return kafkaManager.getConsumerGroupTopicOffsetFetcherTaskExecutor()
                .getResultIfActualOrRefresh(groupInfo.getName()).getValue()
                .entrySet().stream().
                collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> ResettableOffsetRange.with(
                                entry.getValue().getSmallest(),
                                entry.getValue().isLargestInclusive() ? entry.getValue().getLargest() + 1 : entry.getValue().getLargest())));
    }

    public static String buildOffsetResetterUrl(String groupName) {
        return MAPPING.replace("{name}", groupName);
    }

}
