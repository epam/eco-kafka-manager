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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.utils.SecurityUtils;
import org.jetbrains.annotations.NotNull;
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
import com.epam.eco.kafkamanager.PrincipalPermissionsDeleteParams;
import com.epam.eco.kafkamanager.PrincipalPermissionsDeleteParams.ResourceExcludes;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;
import com.epam.eco.kafkamanager.ResourcePermissionsDeleteParams;
import com.epam.eco.kafkamanager.ui.utils.MetadataWrapper;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
@Controller
public class ResourcePermissionController {

    public static final String RESOURCE_VIEW = "resource_permissions";
    public static final String METADATA_VIEW = "resource_permissions_metadata";

    public static final String ATTR_RESOURCE_PERMISSIONS = "resourcePermissions";
    public static final String ATTR_PRINCIPAL_PERMISSIONS = "principalPermissions";
    public static final String ATTR_RESOURCE_TYPE = "resourceType";
    public static final String ATTR_RESOURCE_NAME = "resourceName";
    public static final String ATTR_PATTERN_TYPE = "patternType";
    public static final String ATTR_KAFKA_PRINCIPAL = "kafkaPrincipal";
    public static final String ATTR_KAFKA_PRINCIPAL_PRESENT = "kafkaPrincipalPresent";
    public static final String ATTR_METADATA = "metadata";

    public static final String MAPPING_RESOURCE =
            PermissionController.MAPPING_PERMISSIONS + "/resource/{resourceType}/{resourceName}/{patternType}";

    public static final String MAPPING_DELETE_BY_PRINCIPAL =
            PermissionController.MAPPING_PERMISSIONS + "/resource/{resourceType}/{resourceName}/{patternType}/principal/{principal}";

    public static final String MAPPING_METADATA = MAPPING_RESOURCE + "/metadata";

    @Autowired
    private KafkaManager kafkaManager;

    @RequestMapping(value=MAPPING_RESOURCE, method=RequestMethod.GET)
    public String resource(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            Model model) {
        // resource permissions
        List<PermissionInfo> resourcePermissions = getResourcePermissions(resourceType, resourceName, patternType);
        model.addAttribute(ATTR_RESOURCE_PERMISSIONS, wrap(resourcePermissions));
        model.addAttribute(ATTR_RESOURCE_TYPE, resourceType);
        model.addAttribute(ATTR_RESOURCE_NAME, resourceName);
        model.addAttribute(ATTR_PATTERN_TYPE, patternType);

        // principal permissions
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        List<PermissionInfo> principalPermissions = getPrincipalPermissions(
                kafkaPrincipal,
                resourcePermissions);
        model.addAttribute(ATTR_PRINCIPAL_PERMISSIONS, wrap(principalPermissions));
        model.addAttribute(ATTR_KAFKA_PRINCIPAL, kafkaPrincipalString);

        boolean isPrincipalPresent = isPrincipalPresent(resourcePermissions, kafkaPrincipal);
        model.addAttribute(ATTR_KAFKA_PRINCIPAL_PRESENT, isPrincipalPresent);
        if (isPrincipalPresent) {
            getPermissionMetadata(
                    resourcePermissions,
                    kafkaPrincipal).ifPresent(metadata -> {
                        model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(metadata));
                    });
        }

        return RESOURCE_VIEW;
    }

    @RequestMapping(value = MAPPING_DELETE_BY_PRINCIPAL, method = RequestMethod.DELETE)
    public String deletePrincipalPermissions(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @PathVariable("principal") String kafkaPrincipalString,
            @RequestParam Map<String, Object> paramsMap ) {

        KafkaPrincipal principal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        kafkaManager.deletePermissions(PrincipalPermissionsDeleteParams.builder()
                                                    .principal(principal)
                                                    .resourceType(ResourceType.TOPIC)
                                                    .excludes(Set.of(new ResourceExcludes(resourceName, resourceType)))
                                                    .build());

        return getRedirectUrl(resourceType, resourceName, patternType, kafkaPrincipalString);
    }

    @RequestMapping(value = MAPPING_RESOURCE, method = RequestMethod.DELETE)
    public String deletePermissions(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam(value = "kafkaPrincipalFilter", required = false) String kafkaPrincipalFilterString,
            @RequestParam(value = "permissionTypeFilter", required = false) AclPermissionType permissionTypeFilter,
            @RequestParam(value = "operationFilter", required = false) AclOperation operationFilter,
            @RequestParam(value = "hostFilter", required = false) String hostFilter,
            @RequestParam(value = "resourceTypeRedirect", required = false) ResourceType resourceTypeRedirect,
            @RequestParam(value = "resourceNameRedirect", required = false) String resourceNameRedirect,
            @RequestParam(value = "patternTypeRedirect", required = false) PatternType patternTypeRedirect,
            @RequestParam(value = "kafkaPrincipalRedirect", required = false) String kafkaPrincipalRedirect) {
        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                                                                  .resourceType(resourceType)
                                                                  .resourceName(resourceName)
                                                                  .patternType(patternType)
                                                                  .principalFilter(kafkaPrincipalFilterString)
                                                                  .permissionTypeFilterOrElseDefault(permissionTypeFilter)
                                                                  .operationFilterOrElseDefault(operationFilter)
                                                                  .hostFilter(hostFilter)
                                                                  .build();

        kafkaManager.deletePermissions(new ResourcePermissionsDeleteParams(filter));

        resourceType = resourceTypeRedirect != null ? resourceTypeRedirect : resourceType;
        resourceName = !StringUtils.isBlank(resourceNameRedirect) ? resourceNameRedirect : resourceName;
        patternType = patternTypeRedirect != null ? patternTypeRedirect : patternType;
        kafkaPrincipalFilterString =
                !StringUtils.isBlank(kafkaPrincipalRedirect) ? kafkaPrincipalRedirect : kafkaPrincipalFilterString;

        return getRedirectUrl(resourceType, resourceName, patternType, kafkaPrincipalFilterString);
    }

    @NotNull
    private String getRedirectUrl(ResourceType resourceType, String resourceName, PatternType patternType, String kafkaPrincipalFilterString) {
        ResourcePermissionFilter filter;
        filter = ResourcePermissionFilter.builder()
                                         .resourceType(resourceType)
                                         .resourceName(resourceName)
                                         .patternType(patternType)
                                         .build();

        List<PermissionInfo> resourcePermissions = kafkaManager.getPermissionsOfResource(filter);
        if (!CollectionUtils.isEmpty(resourcePermissions)) {
            return "redirect:" + buildResourceUrl(resourceType, resourceName, patternType, kafkaPrincipalFilterString);
        } else {
            return "redirect:" + PermissionController.MAPPING_PERMISSIONS;
        }
    }


    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.GET)
    public String metadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            Model model) {
        model.addAttribute(ATTR_RESOURCE_TYPE, resourceType);
        model.addAttribute(ATTR_RESOURCE_NAME, resourceName);
        model.addAttribute(ATTR_PATTERN_TYPE, patternType);
        model.addAttribute(ATTR_KAFKA_PRINCIPAL, kafkaPrincipalString);

        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        getPermissionMetadata(
                resourceType,
                resourceName,
                patternType,
                kafkaPrincipal).ifPresent(metadata -> {
                    model.addAttribute(ATTR_METADATA, MetadataWrapper.wrap(metadata));
                });

        return METADATA_VIEW;
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.POST)
    public String metadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString,
            @RequestParam("description") String description,
            @RequestParam("attributes") String attributes) {
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        validatePrincipalPresent(resourceType, resourceName, patternType, kafkaPrincipal);

        kafkaManager.updatePermission(
                PermissionMetadataUpdateParams.builder().
                resourceType(resourceType).
                resourceName(resourceName).
                patternType(patternType).
                principal(kafkaPrincipal).
                description(description).
                attributes(!StringUtils.isBlank(attributes) ? MapperUtils.jsonToMap(attributes) : null).
                build());

        return "redirect:" + buildResourceUrl(resourceType, resourceName, patternType, kafkaPrincipal);
    }

    @RequestMapping(value = MAPPING_METADATA, method = RequestMethod.DELETE)
    public String metadata(
            @PathVariable("resourceType") ResourceType resourceType,
            @PathVariable("resourceName") String resourceName,
            @PathVariable("patternType") PatternType patternType,
            @RequestParam("kafkaPrincipal") String kafkaPrincipalString) {
        KafkaPrincipal kafkaPrincipal = SecurityUtils.parseKafkaPrincipal(kafkaPrincipalString);
        validatePrincipalPresent(resourceType, resourceName, patternType, kafkaPrincipal);

        kafkaManager.updatePermission(
                PermissionMetadataDeleteParams.builder().
                resourceType(resourceType).
                resourceName(resourceName).
                patternType(patternType).
                principal(kafkaPrincipal).
                build());

        return "redirect:" + buildResourceUrl(resourceType, resourceName, patternType, kafkaPrincipal);
    }

    private List<PermissionInfo> getResourcePermissions(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType) {
        ResourcePermissionFilter filter = ResourcePermissionFilter.builder()
                .resourceType(resourceType)
                .resourceName(resourceName)
                .patternType(patternType)
                .build();

        List<PermissionInfo> resourcePermissions = kafkaManager.getPermissionsOfResource(filter);
        if (CollectionUtils.isEmpty(resourcePermissions)) {
            throw new RuntimeException(
                    String.format(
                            "Resource %s %s %s has no permissions",
                            resourceType,
                            resourceName,
                            patternType));
        }

        return resourcePermissions;
    }

    private Optional<Metadata> getPermissionMetadata(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal kafkaPrincipal) {
        List<PermissionInfo> permissions = getResourcePermissions(resourceType, resourceName, patternType);
        return getPermissionMetadata(permissions, kafkaPrincipal);
    }

    private Optional<Metadata> getPermissionMetadata(List<PermissionInfo> permissions, KafkaPrincipal kafkaPrincipal) {
        return permissions.stream().
                filter(permission -> permission.getKafkaPrincipal().equals(kafkaPrincipal)).
                findAny().
                orElseThrow(() -> new RuntimeException(
                        String.format(
                                "Principal %s has no permissions for resource %s %s %s",
                                kafkaPrincipal,
                                permissions.get(0).getResourceType(),
                                permissions.get(0).getResourceName(),
                                permissions.get(0).getPatternType()))).
                getMetadata();
    }

    private List<PermissionInfo> getPrincipalPermissions(
            KafkaPrincipal kafkaPrincipal,
            List<PermissionInfo> permissionsToExclude) {
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

    private void validatePrincipalPresent(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal principal) {
        List<PermissionInfo> permissions = getResourcePermissions(resourceType, resourceName, patternType);
        validatePrincipalPresent(permissions, principal);
    }

    private void validatePrincipalPresent(List<PermissionInfo> permissions, KafkaPrincipal principal) {
        if (!isPrincipalPresent(permissions, principal)) {
            throw new RuntimeException(
                    String.format(
                            "Principal %s has no permissions for resource %s %s %s",
                            principal,
                            permissions.get(0).getResourceType(),
                            permissions.get(0).getResourceName(),
                            permissions.get(0).getPatternType()));
        }
    }

    private boolean isPrincipalPresent(List<PermissionInfo> permissions, KafkaPrincipal principal) {
        if (CollectionUtils.isEmpty(permissions)) {
            return false;
        }

        for (PermissionInfo permission : permissions) {
            if (permission.getKafkaPrincipal().equals(principal)) {
                return true;
            }
        }

        return false;
    }

    private List<PermissionInfoWrapper> wrap(List<PermissionInfo> permissions) {
        return permissions.
                stream().
                map(permission -> PermissionInfoWrapper.wrap(permission)).
                collect(Collectors.toList());
    }

    public static String buildResourceUrl(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal kafkaPrincipal) {
        return buildResourceUrl(resourceType, resourceName, patternType, kafkaPrincipal.toString());
    }

    public static String buildResourceUrl(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            String kafkaPrincipalString) {
        String url = MAPPING_RESOURCE;
        url = url.replace("{resourceType}", resourceType.name());
        url = url.replace("{resourceName}", resourceName);
        url = url.replace("{patternType}", patternType.name());
        url += "?kafkaPrincipal=" + kafkaPrincipalString;
        return url;
    }

}
