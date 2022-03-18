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
package com.epam.eco.kafkamanager.ui.permissions.export;

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PermissionInfo;

/**
 * @author Andrei_Tytsik
 */
public class PlainPermissionExporter implements PermissionExporter {

    @Override
    public void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException {
        for (PermissionInfo permissionInfo : permissionInfos) {
            out.
                append(permissionInfo.getKafkaPrincipal().toString()).append(" ").
                append(permissionInfo.getResourceType().name()).append(" ").
                append(permissionInfo.getResourceName()).append(" ").
                append(permissionInfo.getPatternType().name()).append(" ").
                append(permissionInfo.getPermissionType().name()).append(" ").
                append(permissionInfo.getOperation().name()).append(" ").
                append(permissionInfo.getHost()).append(" ").
                append(permissionInfo.getMetadata().map(Metadata::getDescription).orElse("")).append("\n");
        }
    }

}
