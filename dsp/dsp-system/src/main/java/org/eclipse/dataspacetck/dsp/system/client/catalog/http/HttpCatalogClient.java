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

package org.eclipse.dataspacetck.dsp.system.client.catalog.http;

import okhttp3.Interceptor;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.client.catalog.CatalogClient;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer.expandAndDeserialize;

/**
 * HTTP client for the catalog.
 */
public class HttpCatalogClient implements CatalogClient {

    private static final String CATALOG_REQUEST_PATH = "/catalog/request";
    private static final String DATASET_REQUEST_PATH = "/catalog/datasets/%s";
    private final String connectorUnderTestUrl;
    private final Monitor monitor;
    private final Interceptor interceptor;

    public HttpCatalogClient(String connectorUnderTestUrl, Monitor monitor) {
        this(connectorUnderTestUrl, monitor, null);
    }

    public HttpCatalogClient(String connectorUnderTestUrl, Monitor monitor, Interceptor interceptor) {
        this.connectorUnderTestUrl = connectorUnderTestUrl;
        this.monitor = monitor;
        this.interceptor = interceptor;
    }

    @Override
    public Map<String, Object> getCatalog(Map<String, Object> message, boolean expectError) {
        try (var response = postJson(connectorUnderTestUrl + CATALOG_REQUEST_PATH, message, expectError, interceptor)) {
            monitor.debug("Received catalog request response");
            return expandAndDeserialize(response.body().byteStream());
        }
    }

    @Override
    public Map<String, Object> getDataset(String datasetId, boolean expectError) {
        try (var response = getJson(connectorUnderTestUrl + format(DATASET_REQUEST_PATH, datasetId), expectError, interceptor)) {
            monitor.debug("Received dataset request response");
            return expandAndDeserialize(response.body().byteStream());
        }
    }
}
