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
package com.epam.eco.kafkamanager.ui.topics;

import com.epam.eco.kafkamanager.ui.config.KafkaManagerUiProperties;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */
public class GrafanaMetricsUrlResolver {

    private final static String DEFAULT_ICON = "fa-area-chart";
    private final KafkaManagerUiProperties kafkaManagerUiProperties;

    public GrafanaMetricsUrlResolver(KafkaManagerUiProperties kafkaManagerUiProperties) {
        this.kafkaManagerUiProperties = kafkaManagerUiProperties;
    }

    public Boolean showColumn() {
        return (nonNull(kafkaManagerUiProperties.getGrafanaMetrics())
                && nonNull(kafkaManagerUiProperties.getGrafanaMetrics().getUrlTemplate()));
    }

    public String resolve(String topicName) {
        String env = kafkaManagerUiProperties.getGrafanaMetrics().getVarEnv();
        env = isNull(env) ? "integration" : env;
        return kafkaManagerUiProperties.getGrafanaMetrics().getUrlTemplate()
                .replace("{topicname}", topicName)
                .replace("{varEnv}", env);
    }

    public String getToolName() {
        return kafkaManagerUiProperties.getGrafanaMetrics().getName();
    }

    public String getIcon() {
        String icon = kafkaManagerUiProperties.getGrafanaMetrics().getIcon();
        return isNull(icon) ? DEFAULT_ICON : icon;
    }

}
