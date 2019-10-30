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
package com.epam.eco.kafkamanager.ui.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.epam.eco.kafkamanager.Metadata;
import com.epam.eco.kafkamanager.utils.MapperUtils;

/**
 * @author Andrei_Tytsik
 */
public class MetadataWrapper {

    private final Metadata metadata;

    private String attributesJson;

    public MetadataWrapper(Metadata metadata) {
        Validate.notNull(metadata, "Entity metadata is null");

        this.metadata = metadata;
    }

    public static MetadataWrapper wrap(Metadata metadata) {
        return new MetadataWrapper(metadata);
    }

    public String getDescription() {
        return metadata.getDescription();
    }
    public Map<String, Object> getAttributes() {
        return metadata.getAttributes();
    }
    public String getAttributesJson() {
        if (metadata.getAttributes() != null && attributesJson == null) {
            attributesJson = MapperUtils.toPrettyJson(metadata.getAttributes());
        }
        return attributesJson;
    }
    public LocalDateTime getUpdated() {
        if (metadata.getUpdatedAt() == null) {
            return null;
        }
        return LocalDateTime.ofInstant(
                metadata.getUpdatedAt().toInstant(),
                ZoneId.systemDefault());
    }
    public String getUpdatedBy() {
        return metadata.getUpdatedBy();
    }

}
