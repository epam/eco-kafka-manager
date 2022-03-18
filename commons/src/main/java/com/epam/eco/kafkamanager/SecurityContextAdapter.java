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

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;

/**
 * @author Andrei_Tytsik
 */
public interface SecurityContextAdapter extends SecurityContext {

    default String getIdentity() {
        return getName();
    }

    default String getName() {
        return getAuthentication().getName();
    }

    default Principal getPrincipal() {
        return getAuthentication();
    }

    default Set<String> getRoles() {
        return getAuthentication().getAuthorities().stream().
                map(GrantedAuthority::getAuthority).
                collect(Collectors.toSet());
    }

}
