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
package com.epam.eco.kafkamanager.ui.metrics;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.actuate.metrics.MetricsEndpoint.ListNamesResponse;
import org.springframework.boot.actuate.metrics.MetricsEndpoint.MetricResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class MetricsStateController {

    public static final String VIEW = "metrics_state";
    public static final String ATTR_STATE = "state";

    @Autowired
    private MetricsEndpoint metricsEndpoint;

    @RequestMapping(value="/metrics_state", method=RequestMethod.GET)
    public String state(Model model) throws Exception {
        Map<String, Object> state = new TreeMap<>();
        ListNamesResponse names = metricsEndpoint.listNames();
        names.getNames().forEach(name -> {
            MetricResponse metric = metricsEndpoint.metric(name, Collections.emptyList());
            state.put(name, metric);
        });
        String stateJson = MapperUtils.toPrettyJson(state);
        model.addAttribute(ATTR_STATE, stateJson);
        return VIEW;
    }

}
