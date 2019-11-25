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

import java.util.Collections;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.epam.eco.kafkamanager.TopicInfo;
import com.epam.eco.kafkamanager.udmetrics.UDMetric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricType;
import com.epam.eco.kafkamanager.ui.topics.TopicController;
import com.epam.eco.kafkamanager.ui.utils.RedirectUrlUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class TopicOffsetIncreaseUDMController extends UDMAbstractController {

    public static final String VIEW = "udmetric_topic_offset_increase";

    public static final String ATTR_TOPIC = "topic";
    public static final String ATTR_TOPIC_NAME = "topicName";
    public static final String ATTR_UDM = "udm";

    public static final String MAPPING = UDMetricsController.MAPPING + "/topic_offset_increase/{name}";

    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String metric(@PathVariable("name") String topicName, Model model) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        if (!StringUtils.isBlank(topicName)) {
            UDMetric udm = udMetricManager.get(buildMetricName(topicName));

            TopicInfo topicInfo =
                    kafkaManager.topicExists(topicName) ?
                    kafkaManager.getTopic(topicName) :
                    null;

            model.addAttribute(ATTR_TOPIC_NAME, topicName);
            model.addAttribute(ATTR_TOPIC, topicInfo);
            model.addAttribute(ATTR_UDM, udm);
        }

        return VIEW;
    }

    @RequestMapping(value=MAPPING, method=RequestMethod.POST)
    public String save(@PathVariable("name") String topicName, RedirectAttributes redirectAttrs) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        udMetricManager.createOrReplace(
                UDMetricType.TOPIC_OFFSET_INCREASE,
                topicName,
                Collections.emptyMap());

        return "redirect:" + TopicController.buildTopicUrl(topicName);
    }

    @RequestMapping(value=MAPPING, method=RequestMethod.DELETE)
    public String delete(
            @RequestHeader(name = "Referer", required = false) String referer,
            @PathVariable("name") String topicName) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        udMetricManager.remove(buildMetricName(topicName));

        return RedirectUrlUtils.withRedirectPrefix(referer, UDMetricsController.MAPPING);
    }

    public static String buildMetricName(String topicName) {
        return UDMetricType.TOPIC_OFFSET_INCREASE.formatName(topicName);
    }

    public static String buildMetricUrl(UDMetric udm) {
        Validate.notNull(udm, "UDM is null");

        return buildMetricUrl(udm.getResourceName());
    }

    public static String buildMetricUrl(String topicName) {
        Validate.notBlank(topicName, "Topic name is blank");

        return MAPPING.replace("{name}", topicName);
    }

}
