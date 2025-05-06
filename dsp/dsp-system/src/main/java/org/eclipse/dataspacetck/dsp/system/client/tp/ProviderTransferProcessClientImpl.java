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

package org.eclipse.dataspacetck.dsp.system.client.tp;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;

public class ProviderTransferProcessClientImpl implements ProviderTransferProcessClient {

    private static final String REQUEST_PATH = "transfers/request";
    private static final String GET_PATH = "transfers/%s";

    private final Monitor monitor;
    private Connector systemConnector;
    private String providerConnectorBaseUrl;


    public ProviderTransferProcessClientImpl(String connectorBaseUrl, Monitor monitor) {
        this.providerConnectorBaseUrl = connectorBaseUrl.endsWith("/") ? connectorBaseUrl : connectorBaseUrl + "/";
        this.monitor = monitor;
    }

    public ProviderTransferProcessClientImpl(Connector systemConnector, Monitor monitor) {
        this.systemConnector = systemConnector;
        this.monitor = monitor;
    }

    @Override
    public Map<String, Object> transferRequest(Map<String, Object> transferRequest, String counterPartyId, boolean expectError) {
        if (systemConnector != null) {
            try {
                var compacted = processJsonLd(transferRequest);
                var transferProcess = systemConnector.getProviderTransferProcessManager().handleTransferRequest(compacted, counterPartyId);
                if (expectError) {
                    throw new AssertionError("Expected to throw an error on transfer request");
                }
                return processJsonLd(transferProcess);
            } catch (IllegalStateException e) {
                // if the error is expected, swallow exception
                if (!expectError) {
                    throw e;
                }
                return Map.of();
            }
        } else {
            try (var response = postJson(providerConnectorBaseUrl + REQUEST_PATH, transferRequest, expectError)) {
                monitor.debug("Received transfer request response");
                //noinspection DataFlowIssue
                return processJsonLd(response.body().byteStream());
            }
        }
    }

    @Override
    public Map<String, Object> getTransferProcess(String providerPid) {
        if (systemConnector != null) {
            var transferProcess = systemConnector.getProviderTransferProcessManager().findById(providerPid);
            var consumerPid = transferProcess.getCorrelationId();
            return processJsonLd(createTransferResponse(providerPid, consumerPid, transferProcess.getState().toString()));
        } else {
            try (var response = getJson(providerConnectorBaseUrl + format(GET_PATH, providerPid))) {
                //noinspection DataFlowIssue
                var jsonResponse = processJsonLd(response.body().byteStream());
                var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse);
                var state = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, jsonResponse);
                monitor.debug(format("Received transfer status response with state %s: %s", state, providerId));
                return jsonResponse;
            }
        }
    }
}
