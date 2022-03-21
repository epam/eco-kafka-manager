/*
 *
 */
package com.epam.eco.kafkamanager.core.authz.kafka;

import java.net.InetAddress;

import org.apache.kafka.common.security.auth.KafkaPrincipal;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.server.authorizer.AuthorizableRequestContext;

/**
 * @author Andrei_Tytsik
 */
public class SimpleAuthorizableRequestContext implements AuthorizableRequestContext {

    private final String clientId;
    private final int requestType;
    private final String listenerName;
    private final InetAddress clientAddress;
    private final KafkaPrincipal principal;
    private final SecurityProtocol securityProtocol;
    private final int correlationId;
    private final int requestVersion;

    public SimpleAuthorizableRequestContext(
            InetAddress clientAddress,
            KafkaPrincipal principal) {
        this(
                "",
                -1,
                "",
                clientAddress,
                principal,
                null,
                -1,
                -1);
    }

    public SimpleAuthorizableRequestContext(
            String clientId,
            int requestType,
            String listenerName,
            InetAddress clientAddress,
            KafkaPrincipal principal,
            SecurityProtocol securityProtocol,
            int correlationId,
            int requestVersion) {
        this.clientId = clientId;
        this.requestType = requestType;
        this.listenerName = listenerName;
        this.clientAddress = clientAddress;
        this.principal = principal;
        this.securityProtocol = securityProtocol;
        this.correlationId = correlationId;
        this.requestVersion = requestVersion;
    }

    @Override
    public String listenerName() {
        return listenerName;
    }

    @Override
    public SecurityProtocol securityProtocol() {
        return securityProtocol;
    }

    @Override
    public KafkaPrincipal principal() {
        return principal;
    }

    @Override
    public InetAddress clientAddress() {
        return clientAddress;
    }

    @Override
    public int requestType() {
        return requestType;
    }

    @Override
    public int requestVersion() {
        return requestVersion;
    }

    @Override
    public String clientId() {
        return clientId;
    }

    @Override
    public int correlationId() {
        return correlationId;
    }

}
