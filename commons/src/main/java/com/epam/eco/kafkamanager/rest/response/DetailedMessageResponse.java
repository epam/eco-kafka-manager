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
package com.epam.eco.kafkamanager.rest.response;

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Raman_Babich
 */
public class DetailedMessageResponse<T> extends MessageResponse {

    private final T details;

    public DetailedMessageResponse(String message) {
        super(message);

        this.details = null;
    }

    public DetailedMessageResponse(
            @JsonProperty("message") String message,
            @JsonProperty("details") T details) {
        super(message);

        this.details = details;
    }

    public T getDetails() {
        return details;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DetailedMessageResponse<?> that = (DetailedMessageResponse<?>) o;
        return Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), details);
    }

    @Override
    public String toString() {
        return "DetailedMessageResponse{" +
                "details=" + details +
                "} " + super.toString();
    }

    public static <T> DetailedMessageResponse<T> with(String message, T details) {
        return new DetailedMessageResponse<>(message, details);
    }
}
