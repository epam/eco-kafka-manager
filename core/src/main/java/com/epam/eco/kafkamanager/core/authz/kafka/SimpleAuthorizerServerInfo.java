package com.epam.eco.kafkamanager.core.authz.kafka;

import java.util.Collection;
import java.util.List;

import org.apache.kafka.common.ClusterResource;
import org.apache.kafka.common.Endpoint;
import org.apache.kafka.server.authorizer.AuthorizerServerInfo;

public class SimpleAuthorizerServerInfo implements AuthorizerServerInfo {

    private final ClusterResource clusterResource;
    private final int brokerId;
    private final Collection<Endpoint> endpoints;
    private final Endpoint interBrokerEndpoint;
    private final Collection<String> earlyStartListeners;

    public SimpleAuthorizerServerInfo(
            ClusterResource clusterResource,
            int brokerId,
            Collection<Endpoint> endpoints,
            Endpoint interBrokerEndpoint,
            Collection<String> earlyStartListeners
    ) {
        this.clusterResource = clusterResource;
        this.brokerId = brokerId;
        this.endpoints = List.copyOf(endpoints);
        this.interBrokerEndpoint = interBrokerEndpoint;
        this.earlyStartListeners = List.copyOf(earlyStartListeners);
    }

    @Override
    public ClusterResource clusterResource() {
        return clusterResource;
    }

    @Override
    public int brokerId() {
        return brokerId;
    }

    @Override
    public Collection<Endpoint> endpoints() {
        return endpoints;
    }

    @Override
    public Endpoint interBrokerEndpoint() {
        return interBrokerEndpoint;
    }

    @Override
    public Collection<String> earlyStartListeners() {
        return earlyStartListeners;
    }

}
