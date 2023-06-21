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
package com.epam.eco.kafkamanager.ui.config;

/**
 * @author Mikhail_Vershkov
 */
public class GrafanaUrlTemplate extends ExternalToolTemplate {

    private static final String DEFAULT_ICON = "fa-area-chart";
    private String env;

    public String getEnv() {
        return env;
    }

    public void setEnv(String env) {
        this.env = env;
    }

    @Override
    public String resolve(String topicName) {
        return super.getUrlTemplate().replace("{topicname}", topicName).replace("{env}", env);
    }

    @Override
    public String getIcon() {
        return DEFAULT_ICON;
    }

}
