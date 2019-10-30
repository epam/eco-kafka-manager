/*
 * Copyright 2019 EPAM Systems
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.kafka.common.security.auth.KafkaPrincipal;

import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.ui.utils.KafkaPrincipalComparator;

/**
 * @author Andrei_Tytsik
 */
public class GroupedByPrincipalPlainPermissionExporter implements PermissionExporter {

    @Override
    public void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException {
        Map<KafkaPrincipal, List<PermissionInfo>> groupedByPrincipal = groupByPrincipal(permissionInfos);
        for (Map.Entry<KafkaPrincipal, List<PermissionInfo>> entry : groupedByPrincipal.entrySet()) {
            KafkaPrincipal principal = entry.getKey();
            List<PermissionInfo> group = entry.getValue();

            out.
                append(principal.toString()).append("\n");

            for (PermissionInfo permissionInfo : group) {
                out.
                    append("\t").
                    append(permissionInfo.getResourceType().name()).append(" ").
                    append(permissionInfo.getResourceName()).append(" ").
                    append(permissionInfo.getPermissionType().name()).append(" ").
                    append(permissionInfo.getOperation().name()).append(" ").
                    append(permissionInfo.getHost()).append("\n");
            }

            out.
                append("\n");
        }
    }

    protected Map<KafkaPrincipal, List<PermissionInfo>> groupByPrincipal(
            Collection<PermissionInfo> permissionInfos) {
        Map<KafkaPrincipal, List<PermissionInfo>> groupedByPrincipal =
                new TreeMap<>(KafkaPrincipalComparator.INSTANCE);

        permissionInfos.forEach(permissionInfo -> {
            List<PermissionInfo> group =
                    groupedByPrincipal.computeIfAbsent(permissionInfo.getKafkaPrincipal(), k -> new ArrayList<>());

            group.add(permissionInfo);
        });

        groupedByPrincipal.values().forEach(Collections::sort);

        return groupedByPrincipal;
    }

}
