/*******************************************************************************
 *  Copyright 2025 EPAM Systems
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
package com.epam.eco.kafkamanager.core.permission.repo.kafka;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.common.acl.AccessControlEntryFilter;
import org.apache.kafka.common.acl.AclBinding;
import org.apache.kafka.common.acl.AclBindingFilter;
import org.apache.kafka.common.acl.AclOperation;
import org.apache.kafka.common.acl.AclPermissionType;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourcePattern;
import org.apache.kafka.common.resource.ResourcePatternFilter;
import org.apache.kafka.common.resource.ResourceType;
import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.epam.eco.kafkamanager.KafkaAdminOperations;
import com.epam.eco.kafkamanager.MetadataRepo;
import com.epam.eco.kafkamanager.PermissionInfo;
import com.epam.eco.kafkamanager.PermissionMetadataKey;
import com.epam.eco.kafkamanager.PermissionRepo;
import com.epam.eco.kafkamanager.PermissionSearchCriteria;
import com.epam.eco.kafkamanager.ResourcePermissionFilter;
import com.epam.eco.kafkamanager.repo.AbstractValueRepo;
import com.epam.eco.kafkamanager.repo.CachedRepo;

import static com.epam.eco.kafkamanager.PermissionInfo.fromAclBinding;
import static com.epam.eco.kafkamanager.core.utils.WaitUtil.waitForCondition;
import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;


public class KafkaPermissionRepo extends AbstractValueRepo<PermissionInfo,
        PermissionSearchCriteria> implements PermissionRepo, CachedRepo<ResourcePattern> {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPermissionRepo.class);

    @Autowired
    private KafkaAdminOperations adminOperations;

    @Autowired
    private MetadataRepo metadataRepo;

    @Override
    public int size() {
        return adminOperations.describeAcl(AclBindingFilter.ANY).size();
    }

    @Override
    public List<PermissionInfo> values() {
        return adminOperations.describeAcl(AclBindingFilter.ANY).stream()
                .map(acl -> fromAclBinding(acl, metadataRepo.get(PermissionMetadataKey.with(
                                acl.entry().principal(),
                                acl.pattern().resourceType(),
                                acl.pattern().name(),
                                acl.pattern().patternType()
                        )
                )))
                .toList();
    }

    @Override
    public List<PermissionInfo> findMatchingOfResource(ResourcePermissionFilter filter) {
        ResourcePatternFilter resourceFilter = new ResourcePatternFilter(filter.getResourceType()
                , filter.getResourceName(), filter.getPatternType());

        AccessControlEntryFilter accessControlEntryFilter =
                new AccessControlEntryFilter(filter.getPrincipalFilter(), filter.getHostFilter(),
                        filter.getOperationFilter(), filter.getPermissionTypeFilter());

        AclBindingFilter aclBindingFilter = new AclBindingFilter(resourceFilter,
                accessControlEntryFilter);
        Set<PermissionInfo> permissions = getByResourcePattern(patternFromFilter(filter));
        if (isEmpty(permissions)) {
            return emptyList();
        }
        return permissions.stream()
                .filter(permission ->
                        isResourceMatch(aclBindingFilter, permission))
                .toList();
    }

    @Override
    public void create(
            ResourceType resourceType,
            String resourceName,
            PatternType patternType,
            KafkaPrincipal principal,
            AclPermissionType permissionType,
            AclOperation operation,
            String host
    ) {

        AclBinding aclBinding =
                PermissionInfo.builder()
                        .resourceType(resourceType)
                        .resourceName(resourceName)
                        .patternType(patternType)
                        .permissionType(permissionType)
                        .operation(operation)
                        .kafkaPrincipal(principal)
                        .host(host)
                        .build()
                        .toAclBinding();

        adminOperations.createAcl(aclBinding);

        waitForCondition(() -> isNotEmpty(adminOperations.describeAcl(aclBinding.toFilter())),
                String.format("Waiting for operation Create ACL: %s", aclBinding.pattern()));
    }

    @Override
    public void deleteOfResourceWithoutChecks(
            ResourcePermissionFilter filter,
            DeleteCallback deleteCallback
    ) {
        deletePermissions(filter, deleteCallback);
    }

    @Override
    public void deleteOfResource(
            ResourcePermissionFilter filter,
            DeleteCallback deleteCallback
    ) {
        deletePermissions(filter, deleteCallback);
    }

    @Override
    public void evict(ResourcePattern key) {
        // No-op since we're not caching anymore
    }

    private void deletePermissions(
            ResourcePermissionFilter filter,
            DeleteCallback deleteCallback
    ) {
        List<PermissionInfo> permissions = findMatchingOfResource(filter);

        if (deleteCallback != null) {
            deleteCallback.onBeforeDelete(permissions);
        }

        List<AclBindingFilter> aclBindingFilters =
                permissions.stream().map(permission -> permission.toAclBinding().toFilter()).collect(Collectors.toList());

        adminOperations.deleteAcls(aclBindingFilters);

        waitForCondition(() -> isEmpty(findMatchingOfResource(filter)),
                String.format("Waiting for operation deletePermissions: %s",
                        filter.toResourcePattern()));
    }

    private Set<PermissionInfo> getByResourcePattern(
            ResourcePattern pattern
    ) {
        ResourcePatternFilter resourceFilter = resourceFilterByPattern(pattern);
        AclBindingFilter aclBindingFilter = new AclBindingFilter(resourceFilter,
                AccessControlEntryFilter.ANY);
        Collection<AclBinding> aclBindings = adminOperations.describeAcl(aclBindingFilter);
        return convertToMap(aclBindings).getOrDefault(pattern, Set.of());
    }

    private ResourcePatternFilter resourceFilterByPattern(ResourcePattern pattern) {
        return new ResourcePatternFilter(pattern.resourceType(), pattern.name(),
                pattern.patternType());
    }

    private ResourcePattern patternFromFilter(ResourcePermissionFilter filter) {
        return new ResourcePattern(filter.getResourceType(), filter.getResourceName(),
                filter.getPatternType());
    }

    private boolean isResourceMatch(
            AclBindingFilter bindingFilter,
            PermissionInfo permission
    ) {
        return isPrincipalMatch(bindingFilter, permission)
                && isHostMatch(bindingFilter, permission)
                && isOperationMatch(bindingFilter, permission)
                && isPermissionTypeMatch(bindingFilter, permission);
    }

    private boolean isPermissionTypeMatch(
            AclBindingFilter bindingFilter,
            PermissionInfo permission
    ) {
        return bindingFilter.entryFilter().permissionType() == AclPermissionType.ANY || bindingFilter.entryFilter().permissionType() == permission.getPermissionType();
    }

    private boolean isOperationMatch(
            AclBindingFilter bindingFilter,
            PermissionInfo permission
    ) {
        return bindingFilter.entryFilter().operation() == AclOperation.ANY || bindingFilter.entryFilter().operation() == permission.getOperation();
    }

    private boolean isHostMatch(
            AclBindingFilter bindingFilter,
            PermissionInfo permission
    ) {
        return StringUtils.isBlank(bindingFilter.entryFilter().host()) || bindingFilter.entryFilter().host().equals(permission.getHost());
    }

    private boolean isPrincipalMatch(
            AclBindingFilter bindingFilter,
            PermissionInfo permission
    ) {
        return StringUtils.isBlank(bindingFilter.entryFilter().principal()) || bindingFilter.entryFilter().principal().equals(permission.getKafkaPrincipal().toString());
    }

    private Map<ResourcePattern, Set<PermissionInfo>> convertToMap(Collection<AclBinding> aclBindings) {
        return aclBindings.stream()
                .collect(Collectors.groupingBy(AclBinding::pattern,
                                Collectors.mapping(
                                        acl -> fromAclBinding(acl,
                                                metadataRepo.get(PermissionMetadataKey.with(
                                                                acl.entry().principal(),
                                                                acl.pattern().resourceType(),
                                                                acl.pattern().name(),
                                                                acl.pattern().patternType()
                                                        )
                                                )
                                        ),
                                        Collectors.toSet()
                                )
                        )
                );
    }

}
