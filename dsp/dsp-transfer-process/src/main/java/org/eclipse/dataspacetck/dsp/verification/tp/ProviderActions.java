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
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createCompletion;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTermination;

/**
 * Actions taken by a provider that execute after receiving a message from the consumer.
 */
public class ProviderActions {
    private static final String TRANSFER_START_PATH = "%s/transfers/%s/start";
    private static final String TRANSFER_TERMINATION_PATH = "%s/transfers/%s/termination";
    private static final String TRANSFER_COMPLETION_PATH = "%s/transfers/%s/completion";
    private static final String TRANSFER_SUSPENSION_PATH = "%s/transfers/%s/suspension";


    public static void postStartTransfer(TransferProcess transferProcess) {
        postStartTransfer(transferProcess, false);
    }

    public static void postStartTransfer(TransferProcess transferProcess, boolean expectError) {
        var message = createStartRequest(transferProcess.providerPid(), transferProcess.consumerPid(), null);

        if (!expectError) {
            transferProcess.transition(TransferProcess.State.STARTED);
        }
        var url = format(TRANSFER_START_PATH, transferProcess.getCallbackAddress(), transferProcess.getCorrelationId());
        try (var response = postJson(url, message, expectError)) {
            checkResponse(response, expectError);
        }
    }

    public static void postTerminate(TransferProcess transferProcess) {
        var termination = createTermination(transferProcess.providerPid(), transferProcess.consumerPid(), "1");
        transferProcess.transition(TransferProcess.State.TERMINATED);
        try (var response = postJson(format(TRANSFER_TERMINATION_PATH, transferProcess.getCallbackAddress(), transferProcess.getCorrelationId()), termination)) {
            checkResponse(response, false);
        }
    }

    public static void postComplete(TransferProcess transferProcess) {
        postComplete(transferProcess, false);
    }

    public static void postComplete(TransferProcess transferProcess, boolean expectError) {
        var completion = createCompletion(transferProcess.providerPid(), transferProcess.consumerPid());
        if (!expectError) {
            transferProcess.transition(TransferProcess.State.COMPLETED);
        }
        try (var response = postJson(format(TRANSFER_COMPLETION_PATH, transferProcess.getCallbackAddress(), transferProcess.getCorrelationId()), completion, expectError)) {
            checkResponse(response, expectError);
        }
    }

    public static void postSuspend(TransferProcess transferProcess) {
        postSuspend(transferProcess, false);
    }

    public static void postSuspend(TransferProcess transferProcess, boolean expectError) {
        var suspension = createTermination(transferProcess.providerPid(), transferProcess.consumerPid(), "1");

        if (!expectError) {
            transferProcess.transition(TransferProcess.State.SUSPENDED);
        }
        try (var response = postJson(format(TRANSFER_SUSPENSION_PATH, transferProcess.getCallbackAddress(), transferProcess.getCorrelationId()), suspension, expectError)) {
            checkResponse(response, expectError);
        }
    }

    private static void checkResponse(Response response, boolean expectError) {
        if (expectError && response.isSuccessful()) {
            throw new AssertionError("Expected error response but got: " + response.code());
        } else if (!expectError && !response.isSuccessful()) {
            throw new AssertionError("Unexpected response code: " + response.code());
        }
    }

    public static void pause() {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
