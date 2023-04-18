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

import java.util.Collection;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * @author Andrei_Tytsik
 */
@Validated
@ConfigurationProperties(prefix = "eco.kafkamanager.ui")
public class KafkaManagerUiProperties {

    private String gaTrackingId;
    private DataCatalogUrlTemplate dataCatalogTool;
    private SchemaCatalogTemplate schemaCatalogTool;
    private GrafanaUrlTemplate grafanaMetrics;
    private Collection<ExternalToolTemplate> externalTools;
    private TopicBrowser topicBrowser;
    private Boolean showGridInTopicBrowser = Boolean.FALSE;
    public SchemaCatalogTemplate getSchemaCatalogTool() {
        return schemaCatalogTool;
    }

    public void setSchemaCatalogTool(SchemaCatalogTemplate schemaCatalogTool) {
        this.schemaCatalogTool = schemaCatalogTool;
    }

    public ExternalToolTemplate getDataCatalogTool() {return dataCatalogTool;}

    public void setDataCatalogTool(DataCatalogUrlTemplate dataCatalogTool) {this.dataCatalogTool = dataCatalogTool;}

    public String getGaTrackingId() {
        return gaTrackingId;
    }

    public void setGaTrackingId(String gaTrackingId) {
        this.gaTrackingId = gaTrackingId;
    }

    public TopicBrowser getTopicBrowser() {
        return topicBrowser;
    }

    public void setTopicBrowser(TopicBrowser topicBrowser) {
        this.topicBrowser = topicBrowser;
    }

    public Boolean getShowGridInTopicBrowser() {
        return showGridInTopicBrowser;
    }

    public void setShowGridInTopicBrowser(Boolean showGridInTopicBrowser) {
        this.showGridInTopicBrowser = showGridInTopicBrowser;
    }

    public GrafanaUrlTemplate getGrafanaMetrics() {
        return grafanaMetrics;
    }

    public void setGrafanaMetrics(GrafanaUrlTemplate grafanaMetrics) {
        this.grafanaMetrics = grafanaMetrics;
    }

    public Collection<ExternalToolTemplate> getExternalTools() {
        return externalTools;
    }

    public void setExternalTools(Collection<ExternalToolTemplate> externalTools) {
        this.externalTools = externalTools;
    }
}
