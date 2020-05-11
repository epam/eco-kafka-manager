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
package com.epam.eco.kafkamanager.ui.permissions;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.epam.eco.kafkamanager.KafkaManager;
import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataDeleteParams;
import com.epam.eco.kafkamanager.PermissionMetadataUpdateParams;
import com.epam.eco.kafkamanager.ResourcePermissionDeleteParams;
import com.epam.eco.kafkamanager.ui.utils.MetadataWrapper;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class ResourcePermissionController {

    public static final String VIEW = "resource_permissions";
    public static final String METADATA_VIEW = "resource_permissions_metadata";

    public static final String ATTR_RESOURCE_PERMISSIONS = "resourcePermissions";
    public static final String ATTR_PRINCIPAL_PERMISSIONS = "principalPermissions";
    public static final String ATTR_RESOURCE_TYPE = "resourceType";
    public static final String ATTR_RESOURCE_NAME = "resourceName";
    public static final String ATTR_KAFKA_PRINCIPAL = "kafkaPrincipal";
    public static final String ATTR_METADATA = "metadata";

    public static final String MAPPING = PermissionController.MAPPING + "/resource";
    public static final String MAPPING_METADATA = PermissionController.MAPPING + "/resource_metadata";
    public static final String MAPPING_RESOURCE_PERMISSION = MAPPING + "/{resourceType}/{resourceName}/{principal}";
    public static final String MAPPING_DELETE_RESOURCE_PERMISSIONS = MAPPING_RESOURCE_PERMISSION + "/delete";

    @Autowired
    private KafkaManager kafkaManager;

    @RequestMapping(value=MAPPING, method=RequestMethod.GET)
    public String permissions(
            ResourceType resourceType,
            String resourceName,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            Model model) {
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);

        List<PermissionInfo> resourcePermissions = getResourcePermissions(resourceType, resourceName);
        checkPrincipalPresent(resourcePermissions, kafkaPrincipal);
        model.addAttribute(ATTR_RESOURCE_PERMISSIONS, wrap(resourcePermissions));

        List<PermissionInfo> principalPermissions = getPrincipalPermissions(
                kafkaPrincipal,
                resourcePermissions);
        model.addAttribute(ATTR_PRINCIPAL_PERMISSIONS, wrap(principalPermissions));

        model.addAttribute(ATTR_RESOURCE_TYPE, resourceType);
        model.addAttribute(ATTR_RESOURCE_NAME, resourceName);
        model.addAttribute(ATTR_KAFKA_PRINCIPAL, kafkaPrincipalString);

        getPermissionMetadata(
                resourceType,
                resourceName,
                kafkaPrincipal).ifPresent(metadata -> {
                    model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(metadata));
                });

        return VIEW;
    }

    @RequestMapping(value = MAPPING_DELETE_RESOURCE_PERMISSIONS, method = RequestMethod.POST)
    public String deletePermissions(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("principal") String principal) {
        ResourcePermissionDeleteParams params = ResourcePermissionDeleteParams.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .principal(principal)
                .build();

        kafkaManager.deletePermissions(params);

        return "redirect:" + PermissionController.MAPPING;
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.GET)
    public String metadata(ResourceType resourceType,
            String resourceName,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            Model model) {
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);

        model.addAttribute(ATTR_RESOURCE_TYPE, resourceType);
        model.addAttribute(ATTR_RESOURCE_NAME, resourceName);
        model.addAttribute(ATTR_KAFKA_PRINCIPAL, kafkaPrincipalString);

        getPermissionMetadata(
                resourceType,
                resourceName,
                kafkaPrincipal).ifPresent(metadata -> {
                    model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(metadata));
                });

        return METADATA_VIEW;
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.POST)
    public String metadata(
            ResourceType resourceType,
            String resourceName,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            String description,
            String attributes) {
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        kafkaManager.updatePermission(
                PermissionMetadataUpdateParams.builder().
                resourceType(resourceType).
                resourceName(resourceName).
                principal(kafkaPrincipal).
                description(description).
                attributes(!StringUtils.isBlank(attributes) ? MapperUtils.jsonToMap(attributes) : null).
                build());
        return "redirect:" + buildResourcePermissionUrl(resourceType, resourceName, kafkaPrincipal);
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.DELETE)
    public String metadata(
            ResourceType resourceType,
            String resourceName,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString) {
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        kafkaManager.updatePermission(
                PermissionMetadataDeleteParams.builder().
                resourceType(resourceType).
                resourceName(resourceName).
                principal(kafkaPrincipal).
                build());
        return "redirect:" + buildResourcePermissionUrl(resourceType, resourceName, kafkaPrincipal);
    }

    private Optional<Metadata> getPermissionMetadata(
            ResourceType resourceType,
            String resourceName,
            KafkaPrincipal kafkaPrincipal) {
        return kafkaManager.getAllPermissions().stream().
                filter(
                        permission ->
                            permission.getKafkaPrincipal().equals(kafkaPrincipal) &&
                            permission.getResourceType() == resourceType &&
                            permission.getResourceName().equals(resourceName)).
                findFirst().
                map(PermissionInfo::getMetadata).get();
    }

    private void checkPrincipalPresent(List<PermissionInfo> permissions, KafkaPrincipal principal) {
        if (permissions.isEmpty()) {
            return;
        }

        for (PermissionInfo permission : permissions) {
            if (permission.getKafkaPrincipal().equals(principal)) {
                return;
            }
        }

        throw new RuntimeException(
                String.format(
                        "Principal %s has no permissions for resource %s (%s)",
                        principal.getName(), permissions.get(0).getResourceName(), permissions.get(0).getResourceType()));
    }

    private List<PermissionInfo> getResourcePermissions(ResourceType resourceType, String resourceName) {
        return kafkaManager.getAllPermissions().stream().
                filter(
                        permission ->
                            permission.getResourceType() == resourceType &&
                            permission.getResourceName().equals(resourceName)).
                collect(Collectors.toList());
    }

    private List<PermissionInfo> getPrincipalPermissions(
            KafkaPrincipal kafkaPrincipal, Collection<PermissionInfo> permissionsToExclude) {
        Set<PermissionInfo> permissionsToExcludeSet =
                permissionsToExclude != null ?
                new HashSet<>(permissionsToExclude) :
                Collections.emptySet();
        return kafkaManager.getAllPermissions().stream().
                filter(
                        permission ->
                            permission.getKafkaPrincipal().equals(kafkaPrincipal) &&
                            !permissionsToExcludeSet.contains(permission)).
                collect(Collectors.toList());
    }

    private List<PermissionInfoWrapper> wrap(List<PermissionInfo> permissions) {
        return permissions.
                stream().
                map(permission -> PermissionInfoWrapper.wrap(permission)).
                collect(Collectors.toList());
    }

    public static String buildResourcePermissionUrl(
            ResourceType resourceType,
            String resourceName,
            KafkaPrincipal kafkaPrincipal) {
        return
                MAPPING +
                "?resourceType=" + resourceType +
                "&resourceName=" + resourceName +
                "&kafkaPrincipal=" + kafkaPrincipal;
    }

}
