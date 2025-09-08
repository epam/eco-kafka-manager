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
package com.epam.eco.kafkamanager.ui.consumers;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.ConsumerGroupState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.ConsumerGroupListSearchCriteria;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataDeleteParams;
import com.epam.eco.kafkamanager.ConsumerGroupMetadataUpdateParams;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.core.utils.AuditLogger;
import com.epam.eco.kafkamanager.udmetrics.UDMetric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricManager;
import com.epam.eco.kafkamanager.udmetrics.UDMetricType;
import com.epam.eco.kafkamanager.ui.consumers.model.ConsumerGroupRecordModel;
import com.epam.eco.kafkamanager.ui.consumers.model.ConsumerGroupTableModel;
import com.epam.eco.kafkamanager.ui.metrics.udm.UDMetricWrapper;
import com.epam.eco.kafkamanager.ui.utils.ComboBoxModel;
import com.epam.eco.kafkamanager.ui.utils.MetadataWrapper;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class ConsumerGroupController {

    public static final String GROUPS_VIEW = "consumer_groups";
    public static final String GROUP_VIEW = "consumer_group";
    public static final String GROUP_METADATA_VIEW = "consumer_group_metadata";
    public static final String ATTR_GROUP = "group";
    public static final String ATTR_GROUP_LAG_UDM_TYPE = "groupLagUdmType";
    public static final String ATTR_GROUP_LAG_UDM_NAME = "groupLagUdmName";
    public static final String ATTR_GROUP_LAG_UDM = "groupLagUdm";
    public static final String ATTR_METADATA = "metadata";

    public static final String ATTR_CONSUMER_GROUP_STATES = "consumerGroupStates";
    public static final String ATTR_CONSUMER_GROUP_STORAGES = "consumerGroupStorages";

    public static final String MAPPING_GROUPS = "/consumer_groups";
    public static final String MAPPING_GROUPS_DATA = MAPPING_GROUPS + "/data";
    public static final String MAPPING_GROUP = MAPPING_GROUPS + "/{name}";
    public static final String MAPPING_DELETE = MAPPING_GROUP + "/delete";
    public static final String MAPPING_GROUP_METADATA = MAPPING_GROUP + "/metadata";

    @Autowired
    private KafkaManager kafkaManager;

    @Autowired(required=false)
    private UDMetricManager udMetricManager;

    @RequestMapping(value = MAPPING_GROUPS, method = RequestMethod.GET)
    public String groups(
            @RequestParam(required=false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {
        model.addAttribute(ATTR_CONSUMER_GROUP_STATES,
                           Arrays.stream(ConsumerGroupState.values())
                                 .map(m->ComboBoxModel.build(m.name(), m.toString()))
                                 .collect(Collectors.toList()));
        model.addAttribute(ATTR_CONSUMER_GROUP_STORAGES,
                           Arrays.stream(ConsumerGroupInfo.StorageType.values())
                                 .map(state -> ComboBoxModel.build(state.name(),StringUtils.capitalize(state.name().toLowerCase())))
                                 .collect(Collectors.toList()));
        return GROUPS_VIEW;
    }

    @RequestMapping(value = MAPPING_GROUPS_DATA, method = RequestMethod.GET)
    public ResponseEntity<ConsumerGroupTableModel> groupsList(
            @RequestParam(required=false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {
        List<ConsumerGroupInfo> groups = kafkaManager.getConsumerGroups(
                ConsumerGroupListSearchCriteria.fromJsonWith(paramsMap));
        ConsumerGroupTableModel tableModel = ConsumerGroupTableModel.builder()
                                                       .draw(1)
                                                       .recordsTotal(groups.size())
                                                       .recordsFiltered(groups.size())
                                                       .data(groups.stream()
                                                                  .map(ConsumerGroupRecordModel::new)
                                                                  .collect(Collectors.toList()))
                                                       .build();
        return ResponseEntity.ok(tableModel);

    }

    @RequestMapping(value = MAPPING_GROUP, method = RequestMethod.GET)
    public String group(@PathVariable("name") String groupName, Model model) {
        model.addAttribute(
                ATTR_GROUP,
                ConsumerGroupInfoWrapper.wrap(kafkaManager.getConsumerGroup(groupName)));

        String groupLagUdmName = UDMetricType.CONSUMER_GROUP_LAG.formatName(groupName);
        model.addAttribute(ATTR_GROUP_LAG_UDM_TYPE, UDMetricType.CONSUMER_GROUP_LAG);
        model.addAttribute(ATTR_GROUP_LAG_UDM_NAME, groupLagUdmName);
        model.addAttribute(ATTR_GROUP_LAG_UDM, getAndWrapUdm(groupLagUdmName));

        return GROUP_VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('CONSUMER_GROUP', #groupName, 'DELETE')")
    @RequestMapping(value = MAPPING_DELETE, method = RequestMethod.POST)
    public String delete(@PathVariable("name") String groupName) {
        AuditLogger.logConsumerGroupDelete(groupName);
        kafkaManager.deleteConsumerGroup(groupName);
        return "redirect:" + MAPPING_GROUPS;
    }

    @RequestMapping(value = MAPPING_GROUP_METADATA, method = RequestMethod.GET)
    public String metadata(@PathVariable("name") String groupName, Model model) {
        ConsumerGroupInfo groupInfo = kafkaManager.getConsumerGroup(groupName);
        model.addAttribute(ATTR_GROUP, ConsumerGroupInfoWrapper.wrap(groupInfo));
        if (groupInfo.getMetadata().isPresent()) {
            model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(groupInfo.getMetadata().get()));
        }
        return GROUP_METADATA_VIEW;
    }

    @RequestMapping(value = MAPPING_GROUP_METADATA, method = RequestMethod.POST)
    public String metadata(String groupName, String description, String attributes) {
        kafkaManager.updateConsumerGroup(
                ConsumerGroupMetadataUpdateParams.builder().
                groupName(groupName).
                description(description).
                attributes(!StringUtils.isBlank(attributes) ? MapperUtils.jsonToMap(attributes) : null).
                build());
        return "redirect:" + buildGroupUrl(groupName);
    }

    @RequestMapping(value = MAPPING_GROUP_METADATA, method = RequestMethod.DELETE)
    public String metadata(@PathVariable("name") String groupName) {
        kafkaManager.updateConsumerGroup(
                ConsumerGroupMetadataDeleteParams.builder().
                groupName(groupName).
                build());
        return "redirect:" + buildGroupUrl(groupName);
    }

    /*
    @RequestMapping(value="/unassign_group_from_topic", method=RequestMethod.GET)
    public String unassignGroupFromTopic(String groupName, String topicName, RedirectAttributes redirectAttrs) {
        kafkaManager.unassignConsumerGroupFromTopic(groupName, topicName);
        redirectAttrs.addAttribute(ATTR_GROUP_NAME, groupName);
        return "redirect:/" + VIEW;
    }
    */

    private UDMetricWrapper getAndWrapUdm(String udmName) {
        if (udMetricManager == null) {
            return null;
        }

        UDMetric udm = udMetricManager.get(udmName);
        return udm != null ? UDMetricWrapper.wrap(udm) : null;
    }

    private Page<ConsumerGroupInfoWrapper> wrap(Page<ConsumerGroupInfo> page) {
        return page.map(ConsumerGroupInfoWrapper::wrap);
    }

    public static String buildGroupUrl(String groupName) {
        return MAPPING_GROUP.replace("{name}", groupName);
    }

}
