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
package com.epam.eco.kafkamanager;

import java.util.Objects;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;
import org.apache.kafka.common.security.auth.SecurityProtocol;

import com.fasterxml.jackson.annotation.JsonProperty;

import kafka.cluster.EndPoint;

/**
 * @author Andrei_Tytsik
 */
public class EndPointInfo implements Comparable<EndPointInfo> {

    private final SecurityProtocol protocol;
    private final String host;
    private final int port;

    public EndPointInfo(
            @JsonProperty("protocol") SecurityProtocol protocol,
            @JsonProperty("host") String host,
            @JsonProperty("port") int port) {
        Validate.notNull(protocol, "Protocol is null");

        this.protocol = protocol;
        this.host = host;
        this.port = port;
    }

    public SecurityProtocol getProtocol() {
        return protocol;
    }
    public String getHost() {
        return host;
    }
    public int getPort() {
        return port;
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, host, port);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }
        EndPointInfo that = (EndPointInfo)obj;
        return
                Objects.equals(this.protocol, that.protocol) &&
                Objects.equals(this.host, that.host) &&
                Objects.equals(this.port, that.port);
    }

    @Override
    public String toString() {
        return String.format("%s://%s:%d", protocol, host != null ? host : "", port);
    }

    @Override
    public int compareTo(EndPointInfo that) {
        int result = ObjectUtils.compare(this.protocol, that.protocol);
        if (result == 0) {
            result = ObjectUtils.compare(this.host, that.host);
        }
        if (result == 0) {
            result = ObjectUtils.compare(this.port, that.port);
        }
        return result;
    }

    public static EndPointInfo with(SecurityProtocol protocol, String host, int port) {
        return new EndPointInfo(protocol, host, port);
    }

    public static EndPointInfo from(EndPoint endPoint) {
        Validate.notNull(endPoint, "EndPoint is null");

        return new EndPointInfo(endPoint.securityProtocol(), endPoint.host(), endPoint.port());
    }

}
