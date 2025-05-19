/*
 *  Copyright (c) 2025 Metaform Systems, Inc.
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

package org.eclipse.dataspacetck.dsp.system.client.catalog.local;

import org.eclipse.dataspacetck.dsp.system.api.client.catalog.CatalogClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.message.catalog.CatalogFunctions.createCatalogErrorResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.catalog.CatalogFunctions.createCatalogResponse;
import static org.eclipse.dataspacetck.dsp.system.api.message.catalog.CatalogFunctions.createDatasetResponse;

/**
 * LocalCatalogClient is a client implementation for interacting with a local catalog.
 * It provides methods to retrieve the catalog and specific datasets.
 */
public class LocalCatalogClient implements CatalogClient {
    private final Connector connector;

    public LocalCatalogClient(Connector connector) {
        this.connector = connector;
    }

    @Override
    public Map<String, Object> getCatalog(Map<String, Object> message) {
        var catalog = connector.getCatalogManager().getCatalog();
        return processJsonLd(createCatalogResponse(catalog));
    }

    @Override
    public Map<String, Object> getDataset(String datasetId, boolean expectError) {
        var dataset = connector.getCatalogManager().getDataset(datasetId);
        if (dataset != null && !expectError) {
            return processJsonLd(createDatasetResponse(dataset));

        }
        return processJsonLd(createCatalogErrorResponse("401"));
    }
}
