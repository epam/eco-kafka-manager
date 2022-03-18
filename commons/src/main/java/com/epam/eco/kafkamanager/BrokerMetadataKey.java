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
package com.epam.eco.kafkamanager;

import java.util.Objects;

import org.apache.commons.lang3.Validate;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Andrei_Tytsik
 */
public class BrokerMetadataKey extends MetadataKey {

    private final Integer brokerId;

    public BrokerMetadataKey(
            @JsonProperty("brokerId") Integer brokerId) {
        super(EntityType.BROKER);

        Validate.notNull(brokerId, "Broker id can't be null");
        Validate.isTrue(brokerId >= 0, "Broker id is invalid");

        this.brokerId = brokerId;
    }

    public Integer getBrokerId() {
        return brokerId;
    }

    @Override
    public Object getEntityId() {
        return brokerId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), brokerId);
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        BrokerMetadataKey that = (BrokerMetadataKey)obj;
        return
                Objects.equals(this.brokerId, that.brokerId);
    }

    @Override
    public String toString() {
        return
                "{entityType: " + entityType +
                ", brokerId: " + brokerId +
                "}";
    }

    public static final BrokerMetadataKey with(Integer brokerId) {
        return new BrokerMetadataKey(brokerId);
    }

}
