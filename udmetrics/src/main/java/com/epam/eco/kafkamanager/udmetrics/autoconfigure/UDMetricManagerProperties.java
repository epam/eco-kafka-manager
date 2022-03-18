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
package com.epam.eco.kafkamanager.udmetrics.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Andrei_Tytsik
 */
@ConfigurationProperties(prefix = "eco.kafkamanager.udmetrics")
public class UDMetricManagerProperties {

    private boolean enabled = false;
    private long calculationIntervalInMs = 1 * 60 * 1000;

    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public long getCalculationIntervalInMs() {
        return calculationIntervalInMs;
    }
    public void setCalculationIntervalInMs(long calculationIntervalInMs) {
        this.calculationIntervalInMs = calculationIntervalInMs;
    }

}
