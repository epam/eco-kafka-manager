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

import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.kafkamanager.udmetrics.UDMetric;
import com.epam.eco.kafkamanager.udmetrics.UDMetricSearchQuery;


/**
 * @author Andrei_Tytsik
 */
@Controller
public class UDMetricsController extends UDMAbstractController {

    private static final int PAGE_SIZE = 30;

    public static final String VIEW = "udmetrics";

    public static final String ATTR_PAGE = "page";
    public static final String ATTR_UDM_NAME = "udmName";
    public static final String ATTR_SEARCH_QUERY = "searchQuery";
    public static final String ATTR_TOTAL_COUNT = "totalCount";

    public static final String MAPPING = "/udmetrics";

    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String metrics(
            @RequestParam(required=false) Integer page,
            @RequestParam Map<String, Object> paramsMap,
            Model model) {
        if (!isUdmEnabled()) {
            return VIEW_UDM_DISABLED;
        }

        UDMetricSearchQuery searchQuery = UDMetricSearchQuery.fromJson(paramsMap);
        page = page != null && page > 0 ? page -1 : 0;

        Page<UDMetric> metricPage = udMetricManager.page(
                searchQuery,
                PageRequest.of(page, PAGE_SIZE));

        model.addAttribute(ATTR_SEARCH_QUERY, searchQuery);
        model.addAttribute(ATTR_PAGE, wrap(metricPage));
        model.addAttribute(ATTR_TOTAL_COUNT, udMetricManager.getCount());

        return VIEW;
    }

    private Page<UDMetricWrapper> wrap(Page<UDMetric> page) {
        return page.map(UDMetricWrapper::wrap);
    }

}
