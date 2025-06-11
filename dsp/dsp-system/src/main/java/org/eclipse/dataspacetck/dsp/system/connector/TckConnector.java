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

package org.eclipse.dataspacetck.dsp.system.connector;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.ConsumerNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.ProviderNegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.CatalogManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ConsumerTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ProviderTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.connector.catalog.CatalogManagerImpl;
import org.eclipse.dataspacetck.dsp.system.connector.tp.ConsumerTransferProcessManagerImpl;
import org.eclipse.dataspacetck.dsp.system.connector.tp.ProviderTransferProcessManagerImpl;

import java.util.List;
import java.util.Map;

/**
 * Implements a simple, in-memory connector that supports control-plane operations for testing.
 */
public class TckConnector implements Connector {
    private final ProviderNegotiationManager providerNegotiationManager;
    private final ConsumerNegotiationManager consumerNegotiationManager;
    private final ConsumerTransferProcessManager consumerTransferProcessManager;
    private final ProviderTransferProcessManager providerTransferProcessManager;
    private final CatalogManager catalogManager;

    public TckConnector(Monitor monitor) {
        consumerNegotiationManager = new ConsumerNegotiationManagerImpl(monitor);
        providerNegotiationManager = new ProviderNegotiationManagerImpl(monitor);
        consumerTransferProcessManager = new ConsumerTransferProcessManagerImpl(monitor);
        providerTransferProcessManager = new ProviderTransferProcessManagerImpl(monitor);
        catalogManager = new CatalogManagerImpl(monitor);
    }

    public ProviderNegotiationManager getProviderNegotiationManager() {
        return providerNegotiationManager;
    }

    public ConsumerNegotiationManager getConsumerNegotiationManager() {
        return consumerNegotiationManager;
    }

    public ConsumerTransferProcessManager getConsumerTransferProcessManager() {
        return consumerTransferProcessManager;
    }

    public CatalogManager getCatalogManager() {
        return catalogManager;
    }

    public ProviderTransferProcessManager getProviderTransferProcessManager() {
        return providerTransferProcessManager;
    }


    @Override
    public Map<String, Object> getMetadata() {
        return Map.of("protocolVersions", List.of(Map.of("version", "2025-1", "path", "http://example.com/dsp/2025-1")));
    }
}
