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
package com.epam.eco.kafkamanager.ui.permissions.export;

import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.kafka.common.resource.PatternType;
import org.apache.kafka.common.resource.ResourceType;

/**
 * @author Andrei_Tytsik
 */
class GroupKey implements Comparable<GroupKey> {

    private final ResourceType resourceType;
    private final String resourceName;
    private final PatternType patternType;

    public GroupKey(ResourceType resourceType, String resourceName, PatternType patternType) {
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.patternType = patternType;
    }

    public ResourceType getResourceType() {
        return resourceType;
    }
    public String getResourceName() {
        return resourceName;
    }
    public PatternType getPatternType() {
        return patternType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceType, resourceName, patternType);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        if (this == obj) {
            return true;
        }
        GroupKey that = (GroupKey)obj;
        return
                Objects.equals(this.resourceType, that.resourceType) &&
                Objects.equals(this.resourceName, that.resourceName) &&
                Objects.equals(this.patternType, that.patternType);
    }

    @Override
    public int compareTo(GroupKey that) {
        int result = ObjectUtils.compare(this.resourceType, that.resourceType);
        if (result == 0) {
            result = ObjectUtils.compare(this.resourceName, that.resourceName);
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.patternType, that.patternType);
        }
        return result;
    }

}
