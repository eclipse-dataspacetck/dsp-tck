/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package org.eclipse.dataspacetck.dsp.system;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.core.spi.system.ServiceConfiguration;
import org.eclipse.dataspacetck.core.spi.system.ServiceResolver;
import org.eclipse.dataspacetck.core.spi.system.SystemConfiguration;
import org.eclipse.dataspacetck.core.spi.system.SystemLauncher;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Consumer;
import org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions;
import org.eclipse.dataspacetck.dsp.system.api.mock.ConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.mock.ProviderNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.api.mock.tp.ConsumerTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.mock.tp.ProviderTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ConsumerNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.ProviderNegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.ConsumerTransferProcessPipeline;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.ProviderTransferProcessPipeline;
import org.eclipse.dataspacetck.dsp.system.client.cn.ConsumerNegotiationClient;
import org.eclipse.dataspacetck.dsp.system.client.cn.ProviderNegotiationClient;
import org.eclipse.dataspacetck.dsp.system.client.cn.http.HttpConsumerNegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.client.cn.http.HttpProviderNegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.client.cn.local.LocalConsumerNegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.client.cn.local.LocalProviderNegotiationClientImpl;
import org.eclipse.dataspacetck.dsp.system.client.tp.ConsumerTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.client.tp.ProviderTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.client.tp.http.HttpConsumerTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.client.tp.http.HttpProviderTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.client.tp.local.LocalConsumerTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.client.tp.local.LocalProviderTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.connector.TckConnector;
import org.eclipse.dataspacetck.dsp.system.mock.ConsumerNegotiationMockImpl;
import org.eclipse.dataspacetck.dsp.system.mock.NoOpConsumerNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.mock.NoOpProviderNegotiationMock;
import org.eclipse.dataspacetck.dsp.system.mock.ProviderNegotiationMockImpl;
import org.eclipse.dataspacetck.dsp.system.mock.tp.ConsumerTransferProcessMockImpl;
import org.eclipse.dataspacetck.dsp.system.mock.tp.NoOpConsumerTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.mock.tp.NoOpProviderTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.mock.tp.ProviderTransferProcessMockImpl;
import org.eclipse.dataspacetck.dsp.system.pipeline.ConsumerNegotiationPipelineImpl;
import org.eclipse.dataspacetck.dsp.system.pipeline.ProviderNegotiationPipelineImpl;
import org.eclipse.dataspacetck.dsp.system.pipeline.tp.ConsumerTransferProcessPipelineImpl;
import org.eclipse.dataspacetck.dsp.system.pipeline.tp.ProviderTransferProcessPipelineImpl;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.concurrent.Executors.newFixedThreadPool;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_PREFIX;

/**
 * Instantiates and bootstraps a DSP test fixture.
 */
@SuppressWarnings("unused")
public class DspSystemLauncher implements SystemLauncher {
    private static final String LOCAL_CONNECTOR_CONFIG = TCK_PREFIX + ".dsp.local.connector";
    private static final String CONNECTOR_AGENT_ID_CONFIG = TCK_PREFIX + ".dsp.connector.agent.id";
    private static final String CONNECTOR_BASE_URL_CONFIG = TCK_PREFIX + ".dsp.connector.http.url";
    private static final String CONNECTOR_BASE_AUTHORIZATION_HEADER_CONFIG = TCK_PREFIX + ".dsp.connector.http.headers.authorization";
    private static final String CONNECTOR_INITIATE_URL_CONFIG = TCK_PREFIX + ".dsp.connector.negotiation.initiate.url";
    private static final String CONNECTOR_TRANSFER_INITIATE_URL_CONFIG = TCK_PREFIX + ".dsp.connector.transfer.initiate.url";
    private static final String THREAD_POOL_CONFIG = TCK_PREFIX + ".dsp.thread.pool";
    private static final String DEFAULT_WAIT_CONFIG = TCK_PREFIX + ".dsp.default.wait";
    private static final int DEFAULT_WAIT_SECONDS = 15;
    private final Map<String, Connector> consumerConnectors = new ConcurrentHashMap<>();
    private final Map<String, Connector> providerConnectors = new ConcurrentHashMap<>();
    private final Map<String, ProviderNegotiationMock> negotiationMocks = new ConcurrentHashMap<>();
    private final Map<String, ProviderNegotiationClient> negotiationClients = new ConcurrentHashMap<>();
    private final Map<String, ProviderTransferProcessClient> providerTransferClients = new ConcurrentHashMap<>();
    private final Map<String, ConsumerTransferProcessClient> consumerTransferClients = new ConcurrentHashMap<>();
    private final Map<String, ConsumerTransferProcessMock> consumerTransferMocks = new ConcurrentHashMap<>();
    private final Map<String, ProviderTransferProcessMock> providerTransferMocks = new ConcurrentHashMap<>();
    private final Map<String, ConsumerNegotiationMock> consumerNegotiationMocks = new ConcurrentHashMap<>();
    private final Map<String, ConsumerNegotiationClient> consumerNegotiationClients = new ConcurrentHashMap<>();
    private Monitor monitor;
    private ExecutorService executor;
    private String connectorUnderTestId = "ANONYMOUS";
    private String baseConnectorUrl;
    private String baseAuthorizationHeader;
    private String connectorInitiateUrl;
    private String connectorTransferInitiateUrl;
    private boolean useLocalConnector;
    private long waitTime = DEFAULT_WAIT_SECONDS;

    @Override
    public void start(SystemConfiguration configuration) {
        this.monitor = configuration.getMonitor();
        waitTime = configuration.getPropertyAsLong(DEFAULT_WAIT_CONFIG, DEFAULT_WAIT_SECONDS);
        executor = newFixedThreadPool(configuration.getPropertyAsInt(THREAD_POOL_CONFIG, 10));
        useLocalConnector = configuration.getPropertyAsBoolean(LOCAL_CONNECTOR_CONFIG, false);
        if (!useLocalConnector) {
            baseConnectorUrl = configuration.getPropertyAsString(CONNECTOR_BASE_URL_CONFIG, null);
            if (baseConnectorUrl == null) {
                throw new RuntimeException("Required configuration not set: " + CONNECTOR_BASE_URL_CONFIG);
            }
            baseAuthorizationHeader = configuration.getPropertyAsString(CONNECTOR_BASE_AUTHORIZATION_HEADER_CONFIG, null);
            if (baseAuthorizationHeader != null) {
                HttpFunctions.registerAuthorizationInterceptor(baseAuthorizationHeader);
            }
            connectorInitiateUrl = configuration.getPropertyAsString(CONNECTOR_INITIATE_URL_CONFIG, null);
            if (connectorInitiateUrl == null) {
                throw new RuntimeException("Required configuration not set: " + CONNECTOR_INITIATE_URL_CONFIG);
            }
            connectorTransferInitiateUrl = configuration.getPropertyAsString(CONNECTOR_TRANSFER_INITIATE_URL_CONFIG, null);
            if (connectorTransferInitiateUrl == null) {
                throw new RuntimeException("Required configuration not set: " + CONNECTOR_TRANSFER_INITIATE_URL_CONFIG);
            }
            connectorUnderTestId = configuration.getPropertyAsString(CONNECTOR_AGENT_ID_CONFIG, null);
            if (connectorUnderTestId == null) {
                throw new RuntimeException("Required configuration not set: " + CONNECTOR_AGENT_ID_CONFIG);
            }
        }
    }

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    @Override
    public <T> boolean providesService(Class<T> type) {
        return type.equals(ProviderNegotiationClient.class) ||
                type.equals(Connector.class) ||
                type.equals(ProviderNegotiationMock.class) ||
                type.equals(ConsumerNegotiationMock.class) ||
                type.equals(ConsumerNegotiationPipeline.class) ||
                type.equals(ProviderNegotiationPipeline.class) ||
                type.equals(ConsumerTransferProcessPipeline.class) ||
                type.equals(ProviderTransferProcessPipeline.class) ||
                type.equals(ConsumerTransferProcessMock.class) ||
                type.equals(ProviderTransferProcessMock.class);
    }

    @Nullable
    @Override
    public <T> T getService(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        if (ProviderNegotiationPipeline.class.equals(type)) {
            return createProviderPipeline(type, configuration, resolver);
        } else if (ConsumerNegotiationPipeline.class.equals(type)) {
            return createConsumerPipeline(type, configuration, resolver);
        } else if (Connector.class.equals(type)) {
            return createConnector(type, configuration);
        } else if (ProviderNegotiationMock.class.equals(type)) {
            return createProviderNegotiationMock(type, configuration.getScopeId());
        } else if (ConsumerNegotiationMock.class.equals(type)) {
            return createConsumerNegotiationMock(type, configuration.getScopeId(), configuration, resolver);
        } else if (ProviderNegotiationClient.class.equals(type)) {
            return type.cast(createNegotiationClient(configuration.getScopeId()));
        } else if (ConsumerNegotiationClient.class.equals(type)) {
            return type.cast(createConsumerNegotiationClient(configuration.getScopeId(), configuration, resolver));
        } else if (ProviderTransferProcessPipeline.class.equals(type)) {
            return type.cast(createProviderTransferProcessPipeline(configuration, resolver));
        } else if (ConsumerTransferProcessPipeline.class.equals(type)) {
            return type.cast(createConsumerTransferProcessPipeline(configuration, resolver));
        } else if (ConsumerTransferProcessMock.class.equals(type)) {
            return type.cast(createConsumerTransferProcessMock(type, configuration.getScopeId(), configuration, resolver));
        } else if (ProviderTransferProcessMock.class.equals(type)) {
            return type.cast(createProviderTransferProcessMock(type, configuration.getScopeId(), configuration, resolver));
        }
        return null;
    }

    private <T> T createProviderPipeline(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createNegotiationClient(scopeId);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var consumerConnector = consumerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        var pipeline = new ProviderNegotiationPipelineImpl(negotiationClient,
                callbackEndpoint,
                consumerConnector,
                baseConnectorUrl,
                connectorUnderTestId,
                monitor,
                waitTime);
        return type.cast(pipeline);
    }

    private <T> T createConsumerPipeline(Class<T> type, ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createConsumerNegotiationClient(scopeId, configuration, resolver);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var providerConnector = providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        var pipeline = new ConsumerNegotiationPipelineImpl(negotiationClient,
                callbackEndpoint,
                providerConnector,
                connectorUnderTestId,
                monitor,
                waitTime);
        return type.cast(pipeline);
    }

    private <T> T createProviderNegotiationMock(Class<T> type, String scopeId) {
        return type.cast(negotiationMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = providerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                return new ProviderNegotiationMockImpl(connector.getProviderNegotiationManager(), executor);
            } else {
                return new NoOpProviderNegotiationMock();
            }
        }));
    }

    private <T> T createConsumerNegotiationMock(Class<T> type, String scopeId, ServiceConfiguration configuration, ServiceResolver resolver) {
        return type.cast(consumerNegotiationMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = consumerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                var negotiationManager = connector.getConsumerNegotiationManager();
                var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
                @SuppressWarnings("DataFlowIssue") var address = callbackEndpoint.getAddress();
                return new ConsumerNegotiationMockImpl(negotiationManager, executor, address);
            } else {
                return new NoOpConsumerNegotiationMock();
            }
        }));
    }

    private <T> T createConnector(Class<T> type, ServiceConfiguration configuration) {
        var scopeId = configuration.getScopeId();
        if (configuration.getAnnotations().stream().anyMatch(a -> a.annotationType().equals(Consumer.class))) {
            return type.cast(consumerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor)));
        }
        return type.cast(providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor)));
    }

    private ProviderNegotiationClient createNegotiationClient(String scopeId) {
        return negotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                return new LocalProviderNegotiationClientImpl(providerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor)));
            }
            return new HttpProviderNegotiationClientImpl(baseConnectorUrl, monitor);
        });
    }

    private ConsumerNegotiationClient createConsumerNegotiationClient(String scopeId,
                                                                      ServiceConfiguration configuration,
                                                                      ServiceResolver resolver) {
        return consumerNegotiationClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var consumerConnector = consumerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                return new LocalConsumerNegotiationClientImpl(consumerConnector);
            }
            var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
            assert callbackEndpoint != null;
            return new HttpConsumerNegotiationClientImpl(
                    connectorInitiateUrl,
                    callbackEndpoint.getAddress(),
                    monitor);
        });
    }

    private ConsumerTransferProcessPipeline createConsumerTransferProcessPipeline(ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var transferClient = createConsumerTransferProcessClientClient(scopeId, configuration, resolver);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var providerConnector = providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        return new ConsumerTransferProcessPipelineImpl(transferClient,
                callbackEndpoint,
                providerConnector,
                connectorUnderTestId,
                monitor,
                waitTime);
    }

    private ProviderTransferProcessPipeline createProviderTransferProcessPipeline(ServiceConfiguration configuration, ServiceResolver resolver) {
        var scopeId = configuration.getScopeId();
        var negotiationClient = createProviderTransferProcessClientClient(scopeId, configuration, resolver);
        var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
        var consumerConnector = consumerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        return new ProviderTransferProcessPipelineImpl(negotiationClient,
                callbackEndpoint,
                consumerConnector,
                baseConnectorUrl,
                connectorUnderTestId,
                monitor,
                waitTime);
    }

    private ConsumerTransferProcessClient createConsumerTransferProcessClientClient(String scopeId,
                                                                                    ServiceConfiguration configuration,
                                                                                    ServiceResolver resolver) {
        var providerConnector = providerConnectors.computeIfAbsent(scopeId, k -> new TckConnector(monitor));
        return consumerTransferClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var consumerConnector = consumerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
                return new LocalConsumerTransferProcessClient(consumerConnector);
            }
            var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
            assert callbackEndpoint != null;
            return new HttpConsumerTransferProcessClient(
                    connectorTransferInitiateUrl,
                    callbackEndpoint.getAddress(),
                    monitor);
        });
    }

    private ProviderTransferProcessClient createProviderTransferProcessClientClient(String scopeId,
                                                                                    ServiceConfiguration configuration, ServiceResolver resolver) {
        return providerTransferClients.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                return new LocalProviderTransferProcessClient(providerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor)));
            }
            return new HttpProviderTransferProcessClient(baseConnectorUrl, monitor);
        });
    }

    private <T> T createConsumerTransferProcessMock(Class<T> type, String scopeId, ServiceConfiguration configuration, ServiceResolver resolver) {
        return type.cast(consumerTransferMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = consumerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                var manager = connector.getConsumerTransferProcessManager();
                var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
                @SuppressWarnings("DataFlowIssue") var address = callbackEndpoint.getAddress();
                return new ConsumerTransferProcessMockImpl(manager, executor, address);
            } else {
                return new NoOpConsumerTransferProcessMock();
            }
        }));
    }

    private <T> T createProviderTransferProcessMock(Class<T> type, String scopeId, ServiceConfiguration configuration, ServiceResolver resolver) {
        return type.cast(providerTransferMocks.computeIfAbsent(scopeId, k -> {
            if (useLocalConnector) {
                var connector = providerConnectors.computeIfAbsent(scopeId, k2 -> new TckConnector(monitor));
                var manager = connector.getProviderTransferProcessManager();
                var callbackEndpoint = (CallbackEndpoint) resolver.resolve(CallbackEndpoint.class, configuration);
                @SuppressWarnings("DataFlowIssue") var address = callbackEndpoint.getAddress();
                return new ProviderTransferProcessMockImpl(manager, executor, address);
            } else {
                return new NoOpProviderTransferProcessMock();
            }
        }));
    }
}
