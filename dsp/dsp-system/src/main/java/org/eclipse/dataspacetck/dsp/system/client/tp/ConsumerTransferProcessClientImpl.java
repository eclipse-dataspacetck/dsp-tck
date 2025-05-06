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
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;

public class ConsumerTransferProcessClientImpl implements ConsumerTransferProcessClient {

    private static final String START_PATH = "%s/transfers/%s/start";
    private static final String TERMINATION_PATH = "%s/transfers/%s/termination";

    private static final String GET_PATH = "%s/transfers/%s";

    private final Monitor monitor;
    private Connector systemConnector;
    private String providerConnectorBaseUrl;
    private String consumerConnectorInitiateUrl;


    public ConsumerTransferProcessClientImpl(String consumerConnectorInitiateUrl, String providerConnectorBaseUrl, Monitor monitor) {
        this.consumerConnectorInitiateUrl = consumerConnectorInitiateUrl;
        this.providerConnectorBaseUrl = providerConnectorBaseUrl;
        this.monitor = monitor;
    }

    public ConsumerTransferProcessClientImpl(Connector systemConnector, Monitor monitor) {
        this.systemConnector = systemConnector;
        this.monitor = monitor;
    }

    @Override
    public void initiateTransferRequest(String agreementId, String format) {
        if (systemConnector != null) {
            systemConnector.getConsumerTransferProcessManager().createTransferProcess(agreementId, format, null);
        } else {
            var request = Map.of("providerId", TCK_PARTICIPANT_ID, "agreementId", agreementId, "format", format, "connectorAddress", providerConnectorBaseUrl);
            try (var response = postJson(consumerConnectorInitiateUrl, request, false, true)) {
                monitor.debug("Received transfer request response");
            }
        }
    }

    @Override
    public void startTransfer(String consumerId, Map<String, Object> offer, String callbackAddress, boolean expectError) {
        var compacted = processJsonLd(offer);
        if (systemConnector != null) {
            systemConnector.getConsumerTransferProcessManager().handleStart(compacted);
        } else {
            try (var response = postJson(format(START_PATH, callbackAddress, consumerId), offer, expectError)) {
                monitor.debug("Received start request response");
            }
        }
    }

    @Override
    public void terminateTransfer(String consumerId, Map<String, Object> terminationMessage, String callbackAddress, boolean expectError) {
        var compacted = processJsonLd(terminationMessage);
        if (systemConnector != null) {
            systemConnector.getConsumerTransferProcessManager().handleTermination(compacted);
        } else {
            try (var response = postJson(format(TERMINATION_PATH, callbackAddress, consumerId), terminationMessage, expectError)) {
                monitor.debug("Received termination request response");
            }
        }
    }

    @Override
    public Map<String, Object> getTransferProcess(String consumerId, String callbackAddress) {
        if (systemConnector != null) {
            var transferProcess = systemConnector.getConsumerTransferProcessManager().findById(consumerId);
            var consumerPid = transferProcess.getCorrelationId();
            return processJsonLd(createTransferResponse(consumerId, consumerPid, transferProcess.getState().toString()));
        } else {
            try (var response = getJson(format(GET_PATH, callbackAddress, consumerId))) {
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
