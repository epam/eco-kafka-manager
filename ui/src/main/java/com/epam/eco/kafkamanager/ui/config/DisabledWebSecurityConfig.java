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
package com.epam.eco.kafkamanager.ui.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

/**
 * @author Andrei_Tytsik
 */
@Profile(WebSecurityProfiles.DISABLED)
@Configuration
@EnableWebSecurity
@Order(1)
public class DisabledWebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        HttpSecurityConfigurer.
            configureLogout(http).
            logout().
                logoutSuccessUrl("/"). // just to avoid redirect to default "/login?logout"
                and().
            csrf().
                // https://github.com/thymeleaf/thymeleaf-spring/issues/110
                // https://github.com/spring-projects/spring-security/issues/3906
                csrfTokenRepository(new HttpSessionCsrfTokenRepository()).
                and().
            authorizeRequests().
                anyRequest().permitAll();
    }

}
