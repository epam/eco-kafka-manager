/*
 * Copyright 2020 EPAM Systems
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

import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.udmetrics.UDMetricManager;

/**
 * @author Andrei_Tytsik
 */
public abstract class UDMAbstractController {

    public static final String VIEW_UDM_DISABLED = "udmetrics_disabled";

    @Autowired
    protected KafkaManager kafkaManager;

    @Autowired(required=false)
    protected UDMetricManager udMetricManager;

    public boolean isUdmEnabled() {
        return udMetricManager != null;
    }

}
