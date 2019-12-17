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
package com.epam.eco.kafkamanager.ui.brokers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.commons.kafka.config.BrokerConfigDef;
import com.epam.eco.kafkamanager.BrokerConfigUpdateParams;
import com.epam.eco.kafkamanager.BrokerInfo;
import com.epam.eco.kafkamanager.BrokerMetadataDeleteParams;
import com.epam.eco.kafkamanager.BrokerMetadataUpdateParams;
import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.ui.utils.MetadataWrapper;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class BrokerController {

    public static final String BROKERS_VIEW = "brokers";
    public static final String BROKER_VIEW = "broker";
    public static final String BROKER_METADATA_VIEW = "broker_metadata";
    public static final String BROKER_CONFIG_UPDATE_VIEW = "broker_config_update";

    public static final String ATTR_BROKER = "broker";
    public static final String ATTR_CONFIG_DEF = "configDef";
    public static final String ATTR_PAGE = "page";
    public static final String ATTR_TOTAL_COUNT = "totalCount";
    public static final String ATTR_METADATA = "metadata";

    public static final String MAPPING_BROKERS = "/brokers";
    public static final String MAPPING_BROKER = "/brokers/{id}";
    public static final String MAPPING_BROKER_METADATA = MAPPING_BROKER + "/metadata";
    public static final String MAPPING_CONFIG = MAPPING_BROKER + "/config";

    private static final int PAGE_SIZE = 10;

    @Autowired
    private KafkaAdminOperations kafkaAdminOperations;

    @Autowired
    private KafkaManager kafkaManager;

    @RequestMapping(value = MAPPING_BROKERS, method = RequestMethod.GET)
    public String brokers(Integer page, Model model) {
        page = page != null && page > 0 ? page - 1 : 0;
        Page<BrokerInfo> brokerPage = kafkaManager.getBrokerPage(
                PageRequest.of(page, PAGE_SIZE));
        model.addAttribute(ATTR_PAGE, wrap(brokerPage));
        model.addAttribute(ATTR_TOTAL_COUNT, kafkaManager.getBrokerCount());
        return BROKERS_VIEW;
    }

    @RequestMapping(value = MAPPING_BROKER, method = RequestMethod.GET)
    public String broker(@PathVariable("id") Integer brokerId, Model model) {
        model.addAttribute(
                ATTR_BROKER,
                BrokerInfoWrapper.wrap(kafkaManager.getBroker(brokerId), kafkaAdminOperations));
        return BROKER_VIEW;
    }

    @RequestMapping(value = MAPPING_BROKER_METADATA, method = RequestMethod.GET)
    public String metadata(@PathVariable("id") Integer brokerId, Model model) {
        BrokerInfo brokerInfo = kafkaManager.getBroker(brokerId);
        model.addAttribute(ATTR_BROKER, BrokerInfoWrapper.wrap(brokerInfo, kafkaAdminOperations));
        if (brokerInfo.getMetadata().isPresent()) {
            model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(brokerInfo.getMetadata().get()));
        }
        return BROKER_METADATA_VIEW;
    }

    @RequestMapping(value = MAPPING_BROKER_METADATA, method = RequestMethod.POST)
    public String metadata(Integer brokerId, String description, String attributes) {
        kafkaManager.updateBroker(
                BrokerMetadataUpdateParams.builder().
                brokerId(brokerId).
                description(description).
                attributes(!StringUtils.isBlank(attributes) ? MapperUtils.jsonToMap(attributes) : null).
                build());
        return "redirect:" + buildBrokerUrl(brokerId);
    }

    @RequestMapping(value = MAPPING_BROKER_METADATA, method = RequestMethod.DELETE)
    public String metadata(@PathVariable("id") Integer brokerId) {
        kafkaManager.updateBroker(
                BrokerMetadataDeleteParams.builder().
                brokerId(brokerId).
                build());
        return "redirect:" + buildBrokerUrl(brokerId);
    }

    @PreAuthorize("@authorizer.isPermitted('BROKER', #topicName, 'ALTER_CONFIG')")
    @RequestMapping(value=MAPPING_CONFIG, method = RequestMethod.GET)
    public String config(@PathVariable("id") Integer brokerId, Model model) {
        model.addAttribute(
                ATTR_BROKER,
                BrokerInfoWrapper.wrap(kafkaManager.getBroker(brokerId), kafkaAdminOperations));

        return BROKER_CONFIG_UPDATE_VIEW;
    }

    @PreAuthorize("@authorizer.isPermitted('BROKER', #paramsMap.get('topicName'), 'ALTER_CONFIG')")
    @RequestMapping(value=MAPPING_CONFIG, method=RequestMethod.POST)
    public String config(
            @RequestParam Integer brokerId,
            @RequestParam Map<String, String> paramsMap) {

        BrokerConfigUpdateParams params = BrokerConfigUpdateParams.builder().
                brokerId(brokerId).
                config(extractConfigsFromParams(paramsMap, false)).
                build();

        BrokerInfo brokerInfo = kafkaManager.updateBroker(params);
        return "redirect:" + buildBrokerUrl(brokerInfo.getId());
    }

    public static Map<String, String> extractConfigsFromParams(
            Map<String, String> paramsMap,
            boolean skipNulls) {
        Map<String, String> configs = new HashMap<>((int) Math.ceil(paramsMap.size() / 0.75));
        for (Entry<String, String> entry : paramsMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (
                    BrokerConfigDef.INSTANCE.key(key) == null ||
                    (skipNulls && StringUtils.isBlank(value))) {
                continue;
            }
            configs.put(key, StringUtils.stripToNull(value));
        }
        return configs;
    }

    private Page<BrokerInfoWrapper> wrap(Page<BrokerInfo> page) {
        return page.map(brokerInfo -> BrokerInfoWrapper.wrap(brokerInfo, kafkaAdminOperations));
    }

    public static String buildBrokerUrl(Integer brokerId) {
        return MAPPING_BROKER.replace("{id}", "" + brokerId);
    }

}
