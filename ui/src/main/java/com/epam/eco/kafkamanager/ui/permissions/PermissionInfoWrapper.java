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
package com.epam.eco.kafkamanager.ui.permissions;

import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PermissionInfo;

import static com.epam.eco.kafkamanager.ui.utils.UiUtils.getTruncatedDescription;
import static java.util.Objects.nonNull;

/**
 * @author Andrei_Tytsik
 */
public class PermissionInfoWrapper {

    private final PermissionInfo permissionInfo;

    public PermissionInfoWrapper(PermissionInfo permissionInfo) {
        Validate.notNull(permissionInfo, "Permission info is null");

        this.permissionInfo = permissionInfo;
    }

    public KafkaPrincipal getKafkaPrincipal() {
        return permissionInfo.getKafkaPrincipal();
    }

    public ResourceType getResourceType() {
        return permissionInfo.getResourceType();
    }

    public String getResourceName() {
        return permissionInfo.getResourceName();
    }

    public PatternType getPatternType() {
        return permissionInfo.getPatternType();
    }

    public AclPermissionType getPermissionType() {
        return permissionInfo.getPermissionType();
    }

    public AclOperation getOperation() {
        return permissionInfo.getOperation();
    }

    public String getHost() {
        return permissionInfo.getHost();
    }

    public String getMetadataDescription() {
        return permissionInfo.getMetadata().map(Metadata::getDescription).orElse(null);
    }
    public String getMetadataShortDescription() {
        return getTruncatedDescription(getMetadataDescription());
    }

    public static PermissionInfoWrapper wrap(PermissionInfo permissionInfo) {
        return new PermissionInfoWrapper(permissionInfo);
    }

}
