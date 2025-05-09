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

package org.eclipse.dataspacetck.dsp.system.client.tp.http;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.client.tp.ProviderTransferProcessClient;

import java.util.Map;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;

/**
 * Implementation of {@link ProviderTransferProcessClient} when running with remote connector
 */
public class HttpProviderTransferProcessClient extends AbstractHttpTransferProcessClientImpl implements ProviderTransferProcessClient {

    private static final String REQUEST_PATH = "/transfers/request";
    private final Monitor monitor;
    private final String connectorUnderTestUrl;
    
    public HttpProviderTransferProcessClient(String connectorUnderTestUrl, Monitor monitor) {
        super(monitor);
        this.connectorUnderTestUrl = connectorUnderTestUrl;
        this.monitor = monitor;
    }

    @Override
    public Map<String, Object> transferRequest(Map<String, Object> transferRequest, String counterPartyId, boolean expectError) {
        try (var response = postJson(connectorUnderTestUrl + REQUEST_PATH, transferRequest, expectError)) {
            monitor.debug("Received transfer request response");
            //noinspection DataFlowIssue
            return processJsonLd(response.body().byteStream());
        }
    }

}
