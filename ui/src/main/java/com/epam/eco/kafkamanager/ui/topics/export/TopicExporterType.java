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
package com.epam.eco.kafkamanager.ui.topics.export;

/**
 * @author Andrei_Tytsik
 */
public enum TopicExporterType {

    PLAIN(new PlainTopicExporter(), "text/plain"),
    JSON(new JsonTopicExporter(), "application/json");

    private final TopicExporter exporter;
    private final String contentType;

    TopicExporterType(TopicExporter exporter, String contentType) {
        this.exporter = exporter;
        this.contentType = contentType;
    }

    public TopicExporter exporter() {
        return exporter;
    }

    public String contentType() {
        return contentType;
    }

}
