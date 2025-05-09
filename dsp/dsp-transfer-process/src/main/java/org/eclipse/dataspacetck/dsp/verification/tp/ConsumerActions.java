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

package org.eclipse.dataspacetck.dsp.verification.tp;

import okhttp3.Response;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createCompletion;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTermination;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferRequest;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;

/**
 * Actions taken by a consumer that execute after receiving a message from the provider.
 */
public class ConsumerActions {
    private static final String REQUEST_PATH = "%s/transfers/request";
    private static final String TRANSFER_TERMINATION_PATH = "%s/transfers/%s/termination";
    private static final String TRANSFER_COMPLETION_PATH = "%s/transfers/%s/completion";
    private static final String TRANSFER_SUSPENSION_PATH = "%s/transfers/%s/suspension";
    private static final String TRANSFER_START_PATH = "%s/transfers/%s/start";

    private ConsumerActions() {
    }

    public static void postTransferRequest(String baseUrl, TransferProcess transferProcess) {
        var url = format(REQUEST_PATH, baseUrl);
        var transferRequest = createTransferRequest(transferProcess.getId(), transferProcess.getAgreementId(), transferProcess.getFormat(), transferProcess.getDataAddress(), baseUrl);
        try (var response = postJson(url, transferRequest)) {
            // get the response and update the negotiation with the provider process id
            checkResponse(response);
            assert response.body() != null;
            var jsonResponse = processJsonLd(response.body().byteStream());
            var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, jsonResponse);
            transferProcess.setCorrelationId(providerId);
            transferProcess.transition(REQUESTED);
        }
    }

    public static void postTerminate(String baseUrl, TransferProcess transferProcess) {
        var termination = createTermination(transferProcess.providerPid(), transferProcess.consumerPid(), "1");
        transferProcess.transition(TransferProcess.State.TERMINATED);
        try (var response = postJson(format(TRANSFER_TERMINATION_PATH, baseUrl, transferProcess.getCorrelationId()), termination)) {
            checkResponse(response);
        }
    }

    public static void postComplete(String baseUrl, TransferProcess transferProcess) {
        var completion = createCompletion(transferProcess.providerPid(), transferProcess.consumerPid());
        transferProcess.transition(TransferProcess.State.COMPLETED);
        try (var response = postJson(format(TRANSFER_COMPLETION_PATH, baseUrl, transferProcess.getCorrelationId()), completion)) {
            checkResponse(response);
        }
    }

    public static void postSuspend(String baseUrl, TransferProcess transferProcess) {
        var suspension = createTermination(transferProcess.providerPid(), transferProcess.consumerPid(), "1");
        transferProcess.transition(TransferProcess.State.SUSPENDED);
        try (var response = postJson(format(TRANSFER_SUSPENSION_PATH, baseUrl, transferProcess.getCorrelationId()), suspension)) {
            checkResponse(response);
        }
    }

    public static void postStartTransfer(String baseUrl, TransferProcess transferProcess) {
        var message = createStartRequest(transferProcess.providerPid(), transferProcess.consumerPid(), null);
        transferProcess.transition(TransferProcess.State.STARTED);
        var url = format(TRANSFER_START_PATH, baseUrl, transferProcess.getCorrelationId());
        try (var response = postJson(url, message)) {
            checkResponse(response);
        }
    }

    private static void checkResponse(Response response) {
        if (!response.isSuccessful()) {
            throw new AssertionError("Unexpected response code: " + response.code());
        }
    }

}
