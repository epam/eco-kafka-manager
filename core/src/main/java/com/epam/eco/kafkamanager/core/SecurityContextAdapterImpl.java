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
package com.epam.eco.kafkamanager.core;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.epam.eco.kafkamanager.SecurityContextAdapter;

/**
 * @author Andrei_Tytsik
 */
public class SecurityContextAdapterImpl implements SecurityContextAdapter {

    private static final long serialVersionUID = 1L;

    @Override
    public Authentication getAuthentication() {
        Authentication authentication = getSecurityContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException(
                    "No Authentication associated to the current thread");
        }
        return authentication;
    }

    @Override
    public void setAuthentication(Authentication authentication) {
        getSecurityContext().setAuthentication(authentication);
    }

    private static SecurityContext getSecurityContext() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            throw new RuntimeException(
                    "No Security Context associated to the current thread");
        }
        return context;
    }

}
