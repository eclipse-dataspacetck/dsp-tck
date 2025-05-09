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
import org.eclipse.dataspacetck.dsp.system.client.tp.TransferProcessClient;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.getJson;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;

/**
 * Base Proxy methods to the  connector being verified for transfer process.
 */
public abstract class AbstractHttpTransferProcessClientImpl implements TransferProcessClient {

    private static final String GET_PATH = "/transfers/%s";
    private static final String START_PATH = "%s/transfers/%s/start";
    private static final String COMPLETION_PATH = "%s/transfers/%s/completion";
    private static final String SUSPENSION_PATH = "%s/transfers/%s/suspension";
    private static final String TERMINATION_PATH = "%s/transfers/%s/termination";
    protected final Monitor monitor;

    protected AbstractHttpTransferProcessClientImpl(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void completeTransfer(String counterPartyPid, Map<String, Object> completionMessage, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(COMPLETION_PATH, callbackAddress, counterPartyPid), completionMessage, expectError)) {
            monitor.debug("Received completion request response");
        }
    }

    @Override
    public void suspendTransfer(String counterPartyPid, Map<String, Object> suspensionMessage, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(SUSPENSION_PATH, callbackAddress, counterPartyPid), suspensionMessage, expectError)) {
            monitor.debug("Received suspension request response");
        }
    }

    @Override
    public void startTransfer(String counterPartiPid, Map<String, Object> offer, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(START_PATH, callbackAddress, counterPartiPid), offer, expectError)) {
            monitor.debug("Received start request response");
        }
    }

    @Override
    public void terminateTransfer(String counterPartyPid, Map<String, Object> terminationMessage, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(TERMINATION_PATH, callbackAddress, counterPartyPid), terminationMessage, expectError)) {
            monitor.debug("Received termination request response");
        }
    }

    @Override
    public Map<String, Object> getTransferProcess(String counterPartyPid, String callbackAddress) {
        try (var response = getJson(callbackAddress + format(GET_PATH, counterPartyPid))) {
            //noinspection DataFlowIssue
            var jsonResponse = processJsonLd(response.body().byteStream());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse);
            var state = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, jsonResponse);
            monitor.debug(format("Received transfer status response with state %s: %s", state, providerId));
            return jsonResponse;
        }
    }

}
