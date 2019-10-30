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
import java.util.Objects;
import java.util.TreeMap;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.common.resource.ResourceType;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PermissionInfo;

/**
 * @author Andrei_Tytsik
 */
public class GroupedByResourcePlainPermissionExporter implements PermissionExporter {

    @Override
    public void export(Collection<PermissionInfo> permissionInfos, Writer out) throws IOException {
        Map<GroupKey, List<PermissionInfo>> groupedByResource = groupByResource(permissionInfos);
        for (Map.Entry<GroupKey, List<PermissionInfo>> entry : groupedByResource.entrySet()) {
            GroupKey key = entry.getKey();
            List<PermissionInfo> group = entry.getValue();

            out.
                append(key.getResourceType().name()).append(" ").
                append(key.getResourceName()).append("\n");

            for (PermissionInfo permissionInfo : group) {
                out.
                    append("\t").
                    append(permissionInfo.getKafkaPrincipal().toString()).append(" ").
                    append(permissionInfo.getPermissionType().name()).append(" ").
                    append(permissionInfo.getOperation().name()).append(" ").
                    append(permissionInfo.getHost()).append(" ").
                    append(permissionInfo.getMetadata().map(Metadata::getDescription).orElse("")).append("\n");
            }

            out.
                append("\n");
        }
    }

    protected Map<GroupKey, List<PermissionInfo>> groupByResource(Collection<PermissionInfo> permissionInfos) {
        Map<GroupKey, List<PermissionInfo>> groupedByResource = new TreeMap<>();

        permissionInfos.forEach(permissionInfo -> {
            GroupKey key = new GroupKey(
                    permissionInfo.getResourceType(),
                    permissionInfo.getResourceName());

            List<PermissionInfo> group = groupedByResource.computeIfAbsent(key, k -> new ArrayList<>());

            group.add(permissionInfo);
        });

        groupedByResource.values().forEach(Collections::sort);

        return groupedByResource;
    }

    protected static class GroupKey implements Comparable<GroupKey> {

        private final ResourceType resourceType;
        private final String resourceName;

        public GroupKey(ResourceType resourceType, String resourceName) {
            this.resourceType = resourceType;
            this.resourceName = resourceName;
        }

        public ResourceType getResourceType() {
            return resourceType;
        }
        public String getResourceName() {
            return resourceName;
        }

        @Override
        public int hashCode() {
            return Objects.hash(resourceType, resourceName);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass()) {
                return false;
            }
            if (this == obj) {
                return true;
            }
            GroupKey that = (GroupKey)obj;
            return
                    Objects.equals(this.resourceType, that.resourceType) &&
                    Objects.equals(this.resourceName, that.resourceName);
        }

        @Override
        public int compareTo(GroupKey that) {
            int result = ObjectUtils.compare(this.resourceType, that.resourceType);
            if (result == 0) {
                result = ObjectUtils.compare(this.resourceName, that.resourceName);
            }
            return result;
        }

    }

}
