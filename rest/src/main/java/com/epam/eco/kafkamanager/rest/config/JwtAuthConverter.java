/*******************************************************************************
 *  Copyright 2023 EPAM Systems
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
package com.epam.eco.kafkamanager.rest.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

/**
 *  @author Mikhail_Vershkov
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";

    @Value("${spring.security.oauth2.client-id}")
    private String clientId;

    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        Collection<GrantedAuthority> roles = extractAuthorities(jwt);
        return new JwtAuthenticationToken(jwt,roles);
    }
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<GrantedAuthority> roles = new ArrayList<>();
        if(nonNull(jwt.getClaim(RESOURCE_ACCESS_CLAIM))) {
            Map<String,Object> realmAccess = jwt.getClaim(RESOURCE_ACCESS_CLAIM);
            if(!realmAccess.containsKey(clientId)) {
                return roles;
            }
            Map<String,Object> rolesForAllClients = (Map<String,Object>)realmAccess.get(clientId);
            if(!rolesForAllClients.containsKey(ROLES_CLAIM)) {
                return roles;
            }
            List<String> keycloackRoles = (List<String>)rolesForAllClients.get(ROLES_CLAIM);
            roles = keycloackRoles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return roles;
        }
        return roles;
    }
}
