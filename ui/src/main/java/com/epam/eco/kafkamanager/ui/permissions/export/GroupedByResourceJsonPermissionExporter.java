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
public class GroupedByResourceJsonPermissionExporter extends GroupedByResourcePlainPermissionExporter {

    @Override
    public void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException {
        Map<GroupKey, List<PermissionInfo>> groupedByResource = groupByResource(permissionInfos);

        List<Object> records = new ArrayList<>();
        groupedByResource.entrySet().forEach(
                entry -> records.add(
                        toJsonRecord(entry.getKey(), entry.getValue())));
        MapperUtils.writeAsPrettyJson(out, records);
    }

    private Map<String, Object> toJsonRecord(GroupKey groupKey, List<PermissionInfo> permissionInfos) {
        Map<String, Object> groupRecord = new LinkedHashMap<>();

        groupRecord.put(KEY_RESOURCE_TYPE, groupKey.getResourceType());
        groupRecord.put(KEY_RESOURCE_NAME, groupKey.getResourceName());
        groupRecord.put(KEY_PATTERN_TYPE, groupKey.getPatternType());

        List<Object> permissionRecords = new ArrayList<>();
        groupRecord.put(KEY_PERMISSIONS, permissionRecords);

        permissionInfos.forEach(permissionInfo -> {
            Map<String, Object> permissionRecord = new LinkedHashMap<>();

            permissionRecord.put(KEY_KAFKA_PRINCIPAL, permissionInfo.getKafkaPrincipal().toString());
            permissionRecord.put(KEY_OPERATION, permissionInfo.getOperation().name());
            permissionRecord.put(KEY_HOST, permissionInfo.getHost());
            Metadata metadata = permissionInfo.getMetadata().orElse(null);
            permissionRecord.put(KEY_DESCRIPTION, metadata != null ? metadata.getDescription() : null);

            permissionRecords.add(permissionRecord);
        });

        return groupRecord;
    }

}
