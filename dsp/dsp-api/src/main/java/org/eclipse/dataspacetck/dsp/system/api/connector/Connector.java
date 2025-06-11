/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Metaform Systems, Inc. - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.system.api.connector;

import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.CatalogManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ConsumerTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ProviderTransferProcessManager;

import java.util.Map;

/**
 * The TCK Connector implementation.
 */
public interface Connector {

    /**
     * Returns the manager for provider-side negotiations.
     */
    ProviderNegotiationManager getProviderNegotiationManager();

    /**
     * Returns the manager for provider-side negotiations.
     */
    ConsumerNegotiationManager getConsumerNegotiationManager();

    /**
     * Returns the manager for provider-side transfer processes.
     */
    ProviderTransferProcessManager getProviderTransferProcessManager();

    /**
     * Returns the manager for consumer-side transfer processes.
     */
    ConsumerTransferProcessManager getConsumerTransferProcessManager();

    /**
     * Returns the manager for catalog operations.
     */
    CatalogManager getCatalogManager();

    /**
     * Returns metadata about the connector.
     *
     * @return a map containing metadata information
     */
    Map<String, Object> getMetadata();
}
