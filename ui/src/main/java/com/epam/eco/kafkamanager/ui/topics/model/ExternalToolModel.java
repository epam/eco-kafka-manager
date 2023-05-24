/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.ui.topics.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * @author Mikhail_Vershkov
 */

public class ExternalToolModel implements Serializable {

    private String toolName;
    private String url;
    private String icon;

    public ExternalToolModel() {
    }

    @JsonGetter("toolName")
    public String getToolName() {
        return toolName;
    }
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }
    @JsonGetter("url")
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }
    @JsonGetter("icon")
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public static Builder builder() {
        return new Builder();
    }
    static class Builder {

        private ExternalToolModel toolModel = new ExternalToolModel();

        public Builder toolName(String toolName) {
            toolModel.setToolName(toolName);
            return this;
        }
        public Builder url(String url) {
            toolModel.setUrl(url);
            return this;
        }
        public Builder icon(String icon) {
            toolModel.setIcon(icon);
            return this;
        }
        public ExternalToolModel build() {
            return toolModel;
        }
    }
}
