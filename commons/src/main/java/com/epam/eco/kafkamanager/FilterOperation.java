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
package com.epam.eco.kafkamanager;

import com.fasterxml.jackson.annotation.JsonGetter;

/**
 * @author Mikhail_Vershkov
 */

public class FilterOperation {
    private String id;
    private String label;
    private String placeholder;
    private boolean required;

    public FilterOperation(String id, String label, String placeholder, boolean required) {
        this.id = id;
        this.label = label;
        this.placeholder = placeholder;
        this.required = required;
    }

    @JsonGetter("id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonGetter("label")
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonGetter("placeholder")
    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @JsonGetter("required")
    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean equals(FilterOperation filterOperation) {
        return this.id.equals(filterOperation.getId());
    }

}