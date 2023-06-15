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

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */
public class ExternalToolTemplate extends UrlTemplate {

    private static final String DEFAULT_ICON = "fa-external-link";

    public boolean show() {
        return (nonNull(super.getUrlTemplate()) && nonNull(super.getName()));
    }

    public String resolveWithTopic(String topicName) {
        return super.getUrlTemplate().replace("{topicname}", topicName);
    }
    public String resolveWithSchema(String schemaName) {
        return super.getUrlTemplate().replace("{schemaname}", schemaName);
    }

    public String resolve(String topicName) {
        return resolveWithTopic(topicName);
    }

    @Override
    public String getIcon() {
        return isNull(super.getIcon()) ? DEFAULT_ICON : super.getIcon();
    }
}
