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

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.client.cn.ConsumerNegotiationClient;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;

/**
 * Implementation of a {@link ConsumerNegotiationClient} that supports dispatch to a remote connector system via HTTP.
 */
public class HttpConsumerNegotiationClientImpl extends AbstractHttpNegotiationClient implements ConsumerNegotiationClient {
    private static final String GET_PATH = "%s/negotiations/%s";
    private static final String TERMINATE_PATH = "%s/negotiations/%s/termination";
    private static final String OFFERS_PATH = "%s/negotiations/%s/offers";
    private static final String AGREEMENTS_PATH = "%s/negotiations/%s/agreement";
    private static final String FINALIZE_PATH = "%s/negotiations/%s/events";
    private final Monitor monitor;
    private final String consumerConnectorInitiateUrl;
    private final String providerConnectorBaseUrl;

    public HttpConsumerNegotiationClientImpl(String consumerConnectorInitiateUrl,
                                             String providerConnectorBaseUrl,
                                             Monitor monitor) {
        super(monitor);
        this.consumerConnectorInitiateUrl = consumerConnectorInitiateUrl;
        this.providerConnectorBaseUrl = providerConnectorBaseUrl;
        this.monitor = monitor;
    }

    @Override
    public void initiateRequest(String datasetId, String offerId) {

        var request = Map.of("providerId", TCK_PARTICIPANT_ID, "offerId", offerId, "datasetId", datasetId, "connectorAddress", providerConnectorBaseUrl);
        try (var response = postJson(consumerConnectorInitiateUrl, request, false, true)) {
            monitor.debug("Received contract request response");
        }

    }

    @Override
    public void contractOffer(String consumerId, Map<String, Object> offer, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(OFFERS_PATH, callbackAddress, consumerId), offer, expectError)) {
            monitor.debug("Received contract request response");
            // TODO Validate response
            // processJsonLd(response.body().byteStream(), createDspContext());
        }
    }

    @Override
    public void contractAgreement(String consumerId, Map<String, Object> agreement, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(AGREEMENTS_PATH, callbackAddress, consumerId), agreement, expectError)) {
            monitor.debug("Received contract agreement response");
            // TODO Validate response
            // processJsonLd(response.body().byteStream(), createDspContext());
        }
    }

    @Override
    public void finalize(String consumerId, Map<String, Object> event, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(FINALIZE_PATH, callbackAddress, consumerId), event, expectError)) {
            monitor.debug("Received contract finalize response");
            // TODO Validate response
            // processJsonLd(response.body().byteStream(), createDspContext());
        }
    }

    @Override
    public Map<String, Object> getNegotiation(String consumerId, String callbackAddress) {
        try (var response = getJson(format(GET_PATH, callbackAddress, consumerId))) {
            //noinspection DataFlowIssue
            var jsonResponse = processJsonLd(response.body().byteStream());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse);
            var state = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, jsonResponse);
            monitor.debug(format("Received negotiation status response with state %s: %s", state, providerId));
            return jsonResponse;
        }
    }

}
