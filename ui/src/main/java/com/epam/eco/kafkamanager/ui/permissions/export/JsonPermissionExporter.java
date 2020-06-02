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
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class JsonPermissionExporter implements PermissionExporter {

    @Override
    public void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException {
        List<Object> records = new ArrayList<>(permissionInfos.size());
        for (PermissionInfo permissionInfo : permissionInfos) {
            records.add(toJsonRecord(permissionInfo));
        }
        MapperUtils.writeAsPrettyJson(out, records);
    }

    private Map<String, Object> toJsonRecord(PermissionInfo permissionInfo) {
        Map<String, Object> record = new LinkedHashMap<>();
        record.put(KEY_KAFKA_PRINCIPAL, permissionInfo.getKafkaPrincipal().toString());
        record.put(KEY_RESOURCE_TYPE, permissionInfo.getResourceType().name());
        record.put(KEY_RESOURCE_NAME, permissionInfo.getResourceName());
        record.put(KEY_PERMISSION_TYPE, permissionInfo.getPermissionType().name());
        record.put(KEY_OPERATION, permissionInfo.getOperation().name());
        record.put(KEY_HOST, permissionInfo.getHost());
        Metadata metadata = permissionInfo.getMetadata().orElse(null);
        record.put(KEY_DESCRIPTION, metadata != null ? metadata.getDescription() : null);
        return record;
    }

}
