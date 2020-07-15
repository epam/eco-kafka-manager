/*
 * Copyright 2020 EPAM Systems
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
package com.epam.eco.kafkamanager;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Naira_Tamrazyan
 */
public class ResourcePermissionsDeleteParams {

    private final ResourcePermissionFilter filter;

    @JsonCreator
    public ResourcePermissionsDeleteParams(
            @JsonProperty("filter") ResourcePermissionFilter filter) {
        Validate.notNull(filter, "Filter can't be null");

        this.filter = filter;
    }

    public ResourcePermissionFilter getFilter() {
        return filter;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        ResourcePermissionsDeleteParams that = (ResourcePermissionsDeleteParams) obj;
        return
                Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filter);
    }

    @Override
    public String toString() {
        return
                "{filter: " + filter +
                "}";
    }

}
