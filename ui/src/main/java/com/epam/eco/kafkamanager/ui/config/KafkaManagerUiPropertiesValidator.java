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
package com.epam.eco.kafkamanager.ui.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * @author Mikhail_Vershkov
 */

class KafkaManagerUiPropertiesValidator implements Validator {

    private static final String ENV = "grafanaMetrics.env";
    private static final String URL_TEMPLATE = "urlTemplate";
    private static final String SCHEMA_CATALOG_URL_TEMPLATE = "schemaCatalogTool." + URL_TEMPLATE;
    private static final String DATA_CATALOG_URL_TEMPLATE = "dataCatalogTool." + URL_TEMPLATE;
    private static final String EXTERNAL_TOOLS_URL_TEMPLATE = "externalTools";// + URL_TEMPLATE;

    public boolean supports(@NotNull Class clazz) {
        return KafkaManagerUiProperties.class.isAssignableFrom(clazz);
    }

    public void validate(@NotNull Object target, @NotNull Errors errors) {

        KafkaManagerUiProperties properties = (KafkaManagerUiProperties) target;

        grafanaMetricsTemplateValidate(errors, properties);
        schemaCatalogTemplateValidate(errors, properties);
        dataCatalogTemplateValidate(errors, properties);
        externalToolsTemplateValidate(errors, properties);

    }

    private void grafanaMetricsTemplateValidate(@NotNull Errors errors, KafkaManagerUiProperties properties) {
        if(nonNull(properties.getGrafanaMetrics()) &&
                isNull(properties.getGrafanaMetrics().getEnv())) {
            errors.rejectValue(ENV, "field.grafanaMetrics.env.required",
                               "Property eco.kafkamanager.ui.grafanaMetrics.env required!");
        }
        if(nonNull(properties.getGrafanaMetrics()) &&
                isNull(properties.getGrafanaMetrics().getUrlTemplate())) {
            errors.rejectValue(URL_TEMPLATE, "field.grafanaMetrics.urlTemplate.required",
                               "Property eco.kafkamanager.ui.grafanaMetrics.urlTemplate required!");
        }
    }

    private void schemaCatalogTemplateValidate(@NotNull Errors errors, KafkaManagerUiProperties properties) {
        if(nonNull(properties.getSchemaCatalogTool()) &&
                isNull(properties.getSchemaCatalogTool().getUrlTemplate())) {
            errors.rejectValue(SCHEMA_CATALOG_URL_TEMPLATE, "field.schemaCatalog.urlTemplate.required",
                               "Property eco.kafkamanager.ui.schemaCatalogTool.urlTemplate required!");
        }
    }

    private void dataCatalogTemplateValidate(@NotNull Errors errors, KafkaManagerUiProperties properties) {
        if(nonNull(properties.getDataCatalogTool()) &&
                isNull(properties.getDataCatalogTool().getUrlTemplate())) {
            errors.rejectValue(DATA_CATALOG_URL_TEMPLATE, "field.dataCatalog.urlTemplate.required",
                               "Property eco.kafkamanager.ui.dataCatalogTool.urlTemplate required!");
        }
    }

    private void externalToolsTemplateValidate(@NotNull Errors errors, KafkaManagerUiProperties properties) {
        if(nonNull(properties.getExternalTools()) && !properties.getExternalTools().isEmpty()) {
            for(ExternalToolTemplate toolTemplate: properties.getExternalTools()) {
                if(isNull(toolTemplate.getUrlTemplate())) {
                    errors.rejectValue(EXTERNAL_TOOLS_URL_TEMPLATE, "field.externalTools.urlTemplate.required",
                                       "Property eco.kafkamanager.ui.externalTools.urlTemplate required!");
                }
            }
        }
    }
}