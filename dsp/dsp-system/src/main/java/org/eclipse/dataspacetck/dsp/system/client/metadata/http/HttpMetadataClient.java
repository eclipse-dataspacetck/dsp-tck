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

package org.eclipse.dataspacetck.dsp.system.client.metadata.http;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.client.metadata.MetadataClient;

import java.util.Map;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;

public class HttpMetadataClient implements MetadataClient {
    private static final String METADATA_REQUEST_PATH = "/.well-known/dspace-version";
    private final String baseConnectorUrl;
    private final Monitor monitor;


    public HttpMetadataClient(String baseConnectorUrl, Monitor monitor) {
        this.baseConnectorUrl = baseConnectorUrl;
        this.monitor = monitor;
    }

    @Override
    public Map<String, Object> getMetadata() {
        try (var response = getJson(baseConnectorUrl + METADATA_REQUEST_PATH)) {
            monitor.debug("Received metadata  response");
            //noinspection DataFlowIssue
            return processJson(response.body().byteStream());
        }
    }
}
