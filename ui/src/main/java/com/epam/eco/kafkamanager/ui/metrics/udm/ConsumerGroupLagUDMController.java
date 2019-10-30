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
package com.epam.eco.kafkamanager.ui.metrics.udm;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.epam.eco.kafkamanager.ConsumerGroupInfo;
import com.epam.eco.kafkamanager.udmetrics.UDMetric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricType;
import com.epam.eco.kafkamanager.ui.consumers.ConsumerGroupController;
import com.epam.eco.kafkamanager.ui.utils.RedirectUrlUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class ConsumerGroupLagUDMController extends UDMAbstractController {

    public static final String VIEW = "udmetric_consumer_group_lag";

    public static final String ATTR_CONSUMER_GROUP = "consumerGroup";
    public static final String ATTR_CONSUMER_GROUP_NAME = "consumerGroupName";
    public static final String ATTR_UDM = "udm";

    public static final String MAPPING = UDMetricsController.MAPPING + "/consumer_group_lag/{name}";

    public static final String UDM_CONFIG_TOPIC_NAMES = "topicNames";

    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String metric(@PathVariable("name") String groupName, Model model) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        if (!StringUtils.isBlank(groupName)) {
            UDMetric udm = udMetricManager.get(buildMetricName(groupName));

            ConsumerGroupInfo groupInfo =
                    kafkaManager.consumerGroupExists(groupName) ?
                    kafkaManager.getConsumerGroup(groupName) :
                    null;

            model.addAttribute(ATTR_CONSUMER_GROUP_NAME, groupName);
            model.addAttribute(ATTR_CONSUMER_GROUP, groupInfo);
            model.addAttribute(ATTR_UDM, udm);
        }

        return VIEW;
    }

    @RequestMapping(value=MAPPING, method=RequestMethod.POST)
    public String save(
            @PathVariable("name") String groupName,
            String[] topicNames,
            RedirectAttributes redirectAttrs) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        Validate.notEmpty(topicNames, "Collection of Topics is null or empty");

        udMetricManager.createOrReplace(
                UDMetricType.CONSUMER_GROUP_LAG,
                groupName,
                buildConfig(topicNames));

        return "redirect:" + ConsumerGroupController.buildGroupUrl(groupName);
    }

    @RequestMapping(value=MAPPING, method=RequestMethod.DELETE)
    public String delete(
            @RequestHeader(name = "Referer", required = false) String referer,
            @PathVariable("name") String groupName) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        udMetricManager.remove(buildMetricName(groupName));

        return RedirectUrlUtils.withRedirectPrefix(referer, UDMetricsController.MAPPING);
    }

    private Map<String, Object> buildConfig(String[] topicNames) {
        return Collections.singletonMap(UDM_CONFIG_TOPIC_NAMES, Arrays.asList(topicNames));
    }

    public static String buildMetricName(String groupName) {
        return UDMetricType.CONSUMER_GROUP_LAG.formatMetricName(groupName);
    }

    public static String buildMetricUrl(UDMetric udm) {
        Validate.notNull(udm, "UDM is null");

        return buildMetricUrl(udm.getResourceName());
    }

    public static String buildMetricUrl(String groupName) {
        Validate.notBlank(groupName, "Consumer group name is blank");

        return MAPPING.replace("{name}", groupName);
    }

}
