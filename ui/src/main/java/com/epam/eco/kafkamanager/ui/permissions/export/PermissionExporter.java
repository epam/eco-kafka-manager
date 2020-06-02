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
package com.epam.eco.kafkamanager.ui.permissions.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.epam.eco.kafkamanager.PermissionInfo;

/**
 * @author Andrei_Tytsik
 */
public interface PermissionExporter {

    String HEADER_KAFKA_PRINCIPAL = "Kafka Principal";
    String HEADER_RESOURCE_TYPE = "Resource Type";
    String HEADER_RESOURCE_NAME = "Resource Name";
    String HEADER_PERMISSION_TYPE = "Permission Type";
    String HEADER_OPERATION = "Operation";
    String HEADER_HOST = "Host";
    String HEADER_DESCRIPTION = "Description";

    List<String> HEADERS = Collections.unmodifiableList(Arrays.asList(
            HEADER_KAFKA_PRINCIPAL,
            HEADER_RESOURCE_TYPE,
            HEADER_RESOURCE_NAME,
            HEADER_PERMISSION_TYPE,
            HEADER_OPERATION,
            HEADER_HOST,
            HEADER_DESCRIPTION
    ));

    String KEY_PERMISSIONS = "permissions";
    String KEY_KAFKA_PRINCIPAL = "kafkaPrincipal";
    String KEY_RESOURCE_TYPE = "resourceType";
    String KEY_RESOURCE_NAME = "resourceName";
    String KEY_PERMISSION_TYPE = "permissionType";
    String KEY_OPERATION = "operation";
    String KEY_HOST = "host";
    String KEY_DESCRIPTION = "description";

    void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException;

}
