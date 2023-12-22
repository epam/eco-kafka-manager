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
package com.epam.eco.kafkamanager.ui.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;


/**
 * @author Mikhail_Vershkov
 */
@Profile(WebSecurityProfiles.KEYCLOAK)
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Order(2)
public class KeycloakBaseWebSecurityConfig extends BaseWebSecurityConfig {
    private static final String RESOURCE_ACCESS_CLAIM = "resource_access";
    private static final String ROLES_CLAIM = "roles";

    @Value("${spring.security.oauth2.client.provider.keycloak.logout-url}")
    private String logoutUrl;

    @Autowired
    private AuthenticationLogFilter authenticationLogFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return commonHttpConfig(http)
                .oauth2Login(login -> login.userInfoEndpoint(config -> config.userAuthoritiesMapper(userAuthoritiesMapperForKeycloak())))
                .addFilterAfter(authenticationLogFilter, AnonymousAuthenticationFilter.class)
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl(logoutUrl)
                        .permitAll())
                .build();
    }

    @Bean
    @SuppressWarnings("unchecked")
    public GrantedAuthoritiesMapper userAuthoritiesMapperForKeycloak() {
        return authorities -> {
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
            var authority = authorities.iterator().next();
            boolean isOidc = authority instanceof OidcUserAuthority;

            if (isOidc) {
                var oidcUserAuthority = (OidcUserAuthority) authority;
                if (nonNull(oidcUserAuthority.getUserInfo()) &&
                        nonNull(oidcUserAuthority.getUserInfo().getClaims()) &&
                        oidcUserAuthority.getUserInfo().getClaims().containsKey(RESOURCE_ACCESS_CLAIM)) {
                    Map<String, Object> resourceAccess = (Map<String, Object>) oidcUserAuthority.getUserInfo().getClaims().get(RESOURCE_ACCESS_CLAIM);
                    addExtractedRoles(resourceAccess, oidcUserAuthority, mappedAuthorities);
                } else if (oidcUserAuthority.getAttributes().containsKey(RESOURCE_ACCESS_CLAIM)) {
                    Map<String, Object> resourceAccess = (Map<String, Object>) oidcUserAuthority.getAttributes().get(RESOURCE_ACCESS_CLAIM);
                    addExtractedRoles(resourceAccess, oidcUserAuthority, mappedAuthorities);
                }
            } else {
                var oauth2UserAuthority = (OAuth2UserAuthority) authority;
                Map<String, Object> userAttributes = oauth2UserAuthority.getAttributes();
                if (userAttributes.containsKey(RESOURCE_ACCESS_CLAIM)) {
                    var realmAccess = (Map<String, Object>) userAttributes.get(RESOURCE_ACCESS_CLAIM);
                    var roles = (Collection<String>) realmAccess.get(ROLES_CLAIM);
                    mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
                }
            }
            return mappedAuthorities;
        };
    }

    private void addExtractedRoles(Map<String, Object> resourceAccess,
                                   OidcUserAuthority oidcUserAuthority,
                                   Set<GrantedAuthority> mappedAuthorities) {
        Collection<String> roles = resourceAccess.values().stream()
                .filter(x->(x instanceof Map))
                .flatMap(x -> ((Map<?,?>) x).entrySet().stream())
                .filter(x -> (x.getKey() instanceof String) && x.getKey().equals(ROLES_CLAIM))
                .filter(x -> x.getValue() instanceof Collection<?>)
                .flatMap(x -> ((Collection<String>)x.getValue()).stream())
                .collect(Collectors.toList());
        mappedAuthorities.addAll(generateAuthoritiesFromClaim(roles));
    }

    Collection<GrantedAuthority> generateAuthoritiesFromClaim(Collection<String> roles) {
        return roles.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

}