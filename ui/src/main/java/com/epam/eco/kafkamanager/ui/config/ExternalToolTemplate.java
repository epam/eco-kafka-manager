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
public class ExternalToolTemplate {

    private final static String DEFAULT_ICON = "fa-external-link";
    private String name;
    private String urlTemplate;
    private String icon;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrlTemplate() {
        return urlTemplate;
    }

    public void setUrlTemplate(String urlTemplate) {
        this.urlTemplate = urlTemplate;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    private boolean show() {
        return (nonNull(urlTemplate) && nonNull(name));
    }

    public String resolve(String topicName) {
        return urlTemplate.replace("{topicname}", topicName);
    }

    public String getIcon() {
        return isNull(icon) ? DEFAULT_ICON : icon;
    }
}
