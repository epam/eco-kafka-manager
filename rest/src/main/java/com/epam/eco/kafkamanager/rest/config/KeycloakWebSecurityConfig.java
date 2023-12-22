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
package com.epam.eco.kafkamanager.rest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

/**
 * @author Mikhail_Vershkov
 */
@Profile({WebSecurityProfiles.KEYCLOAK})
@Configuration
@EnableWebSecurity(debug = true)
@EnableMethodSecurity
@Order(2)
public class KeycloakWebSecurityConfig {
    public static final String ADMIN_ROLE = "ECO-KM-ADMIN";

    @Autowired
    private JwtAuthConverter jwtAuthConverter;

    @Autowired
    private AuthenticationLogFilter authenticationLogFilter;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.authorizeHttpRequests(request->request
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.PUT,  "/api/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.POST, "/api/**").hasRole(ADMIN_ROLE)
                        .requestMatchers(HttpMethod.DELETE, "/api/**").hasRole(ADMIN_ROLE)
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session->session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(server->server.jwt(jwt->jwt.jwtAuthenticationConverter(jwtAuthConverter)))
                .addFilterAfter(authenticationLogFilter, AnonymousAuthenticationFilter.class)
               .build();
    }



}
