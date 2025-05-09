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
import org.eclipse.dataspacetck.dsp.system.client.tp.ConsumerTransferProcessClient;

import java.util.Map;

import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;

/**
 * Implementation of {@link ConsumerTransferProcessClient} when running with remote connector
 */
public class HttpConsumerTransferProcessClient extends AbstractHttpTransferProcessClientImpl implements ConsumerTransferProcessClient {

    private final Monitor monitor;
    private final String providerConnectorBaseUrl;
    private final String consumerConnectorInitiateUrl;

    public HttpConsumerTransferProcessClient(String consumerConnectorInitiateUrl, String providerConnectorBaseUrl, Monitor monitor) {
        super(monitor);
        this.consumerConnectorInitiateUrl = consumerConnectorInitiateUrl;
        this.providerConnectorBaseUrl = providerConnectorBaseUrl;
        this.monitor = monitor;
    }

    @Override
    public void initiateTransferRequest(String agreementId, String format) {
        var request = Map.of("providerId", TCK_PARTICIPANT_ID, "agreementId", agreementId, "format", format, "connectorAddress", providerConnectorBaseUrl);
        try (var response = postJson(consumerConnectorInitiateUrl, request, false, true)) {
            monitor.debug("Received transfer request response");
        }
    }
}
