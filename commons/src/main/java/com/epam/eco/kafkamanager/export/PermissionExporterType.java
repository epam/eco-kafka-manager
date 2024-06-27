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
package com.epam.eco.kafkamanager.export;

/**
 * @author Andrei_Tytsik
 */
public enum PermissionExporterType {

    PLAIN(new PlainPermissionExporter(), "text/plain"),
    JSON(new JsonPermissionExporter(), "application/json"),
    CSV(new CsvPermissionExporter(), "text/csv"),
    GROUPED_BY_RESOURCE_PLAIN(new GroupedByResourcePlainPermissionExporter(), "text/plain"),
    GROUPED_BY_RESOURCE_JSON(new GroupedByResourceJsonPermissionExporter(), "application/json"),
    GROUPED_BY_PRINCIPAL_PLAIN(new GroupedByPrincipalPlainPermissionExporter(), "text/plain"),
    GROUPED_BY_PRINCIPAL_JSON(new GroupedByPrincipalJsonPermissionExporter(), "application/json");

    private final PermissionExporter exporter;
    private final String contentType;

    PermissionExporterType(PermissionExporter exporter, String contentType) {
        this.exporter = exporter;
        this.contentType = contentType;
    }

    public PermissionExporter exporter() {
        return exporter;
    }

    public String contentType() {
        return contentType;
    }

}
