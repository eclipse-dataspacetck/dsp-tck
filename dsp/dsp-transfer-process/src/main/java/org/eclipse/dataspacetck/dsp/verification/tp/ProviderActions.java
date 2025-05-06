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
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTermination;

/**
 * Actions taken by a provider that execute after receiving a message from the consumer.
 */
public class ProviderActions {
    private static final String TRANSFER_START_PATH = "%s/transfers/%s/start";
    private static final String TRANSFER_TERMINATION_PATH = "%s/transfers/%s/termination";


    public static void postStartTransfer(TransferProcess transferProcess) {

        var message = createStartRequest(transferProcess.getId(), transferProcess.getCorrelationId(), null);

        transferProcess.transition(TransferProcess.State.STARTED);
        var url = format(TRANSFER_START_PATH, transferProcess.getCallbackAddress(), transferProcess.getCorrelationId());
        try (var response = postJson(url, message)) {
            checkResponse(response);
        }
    }

    public static void postTerminate(TransferProcess transferProcess) {
        var termination = createTermination(transferProcess.getId(), transferProcess.getCorrelationId(), "1");
        transferProcess.transition(TransferProcess.State.TERMINATED);
        try (var response = postJson(format(TRANSFER_TERMINATION_PATH, transferProcess.getCallbackAddress(), transferProcess.getCorrelationId()), termination)) {
            checkResponse(response);
        }
    }

    private static void checkResponse(Response response) {
        if (!response.isSuccessful()) {
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
