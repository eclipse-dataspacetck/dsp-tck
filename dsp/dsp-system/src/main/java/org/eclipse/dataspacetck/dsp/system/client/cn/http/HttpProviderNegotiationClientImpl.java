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

package org.eclipse.dataspacetck.dsp.system.client.cn.http;

import okhttp3.Response;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.client.cn.ProviderNegotiationClient;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.compactStringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;

/**
 * Implementation of a {@link ProviderNegotiationClient} that supports dispatch to a remote connector system via HTTP.
 */
public class HttpProviderNegotiationClientImpl extends AbstractHttpNegotiationClient implements ProviderNegotiationClient {
    private static final String GET_PATH = "negotiations/%s";
    private static final String REQUEST_PATH = "negotiations/request";
    private static final String TERMINATE_PATH = "negotiations/%s/termination";
    private static final String EVENT_PATH = "negotiations/%s/events";
    private static final String VERIFICATION_PATH = "negotiations/%s/agreement/verification";
    private final Monitor monitor;
    private final String providerConnectorBaseUrl;
    private Connector systemConnector;

    public HttpProviderNegotiationClientImpl(String connectorBaseUrl, Monitor monitor) {
        super(monitor);
        this.providerConnectorBaseUrl = connectorBaseUrl.endsWith("/") ? connectorBaseUrl : connectorBaseUrl + "/";
        this.monitor = monitor;
    }

    @Override
    public Map<String, Object> contractRequest(Map<String, Object> contractRequest, String counterPartyId, boolean expectError) {
        try (var response = postJson(providerConnectorBaseUrl + REQUEST_PATH, contractRequest, expectError)) {
            monitor.debug("Received contract request response");
            //noinspection DataFlowIssue
            return processJsonLd(response.body().byteStream());
        }
    }

    @Override
    public void accept(Map<String, Object> event) {
        var providerId = compactStringProperty(DSPACE_PROPERTY_PROVIDER_PID, event);
        try (var response = postJson(providerConnectorBaseUrl + format(EVENT_PATH, providerId), event)) {
            if (!response.isSuccessful()) {
                throw new AssertionError(format("Accept event failed with code %s: %s ", response.code(), providerId));
            }
            monitor.debug("Received accept response: " + providerId);
        }
    }

    @Override
    public void verify(Map<String, Object> event, boolean expectError) {
        var providerId = compactStringProperty(DSPACE_PROPERTY_PROVIDER_PID, event);
        try (var response = postJson(providerConnectorBaseUrl + format(VERIFICATION_PATH, providerId), event, expectError)) {
            validateResponse(response, providerId, expectError, "verify");
            monitor.debug("Received verification response: " + providerId);
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String providerPid) {
        try (var response = getJson(providerConnectorBaseUrl + format(GET_PATH, providerPid))) {
            //noinspection DataFlowIssue
            var jsonResponse = processJsonLd(response.body().byteStream());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse);
            var state = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, jsonResponse);
            monitor.debug(format("Received negotiation status response with state %s: %s", state, providerId));
            return jsonResponse;
        }
    }

    private void validateResponse(Response response, String providerId, boolean expectError, String type) {
        if (expectError) {
            if (response.isSuccessful()) {
                throw new AssertionError(format("Invalid %s did not fail: %s", type, providerId));
            }
        } else {
            if (!response.isSuccessful()) {
                throw new AssertionError(format("Request %s failed with code %s: %s", type, response.code(), providerId));
            }
        }
    }
}
