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
package com.epam.eco.kafkamanager;

import org.springframework.security.access.prepost.PreAuthorize;

import com.epam.eco.kafkamanager.repo.KeyValueRepo;

/**
 * @author Andrei_Tytsik
 */
public interface MetadataRepo extends KeyValueRepo<MetadataKey, Metadata, MetadataSearchCriteria> {

    @PreAuthorize("@authorizer.isPermitted(#key.entityType, #key.entityId, 'ALTER_METADATA')")
    void createOrReplace(MetadataKey key, Metadata metadata);

    @PreAuthorize("@authorizer.isPermitted(#key.entityType, #key.entityId, 'DELETE_METADATA')")
    void delete(MetadataKey key);

    void registerUpdateListener(MetadataUpdateListener listener);

}
