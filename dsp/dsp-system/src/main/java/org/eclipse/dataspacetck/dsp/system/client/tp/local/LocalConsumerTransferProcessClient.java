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

package org.eclipse.dataspacetck.dsp.system.client.tp.local;

import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.client.tp.ConsumerTransferProcessClient;

import java.util.Map;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;

/**
 * Implementation of {@link ConsumerTransferProcessClient} when running with local connector
 */
public class LocalConsumerTransferProcessClient implements ConsumerTransferProcessClient {

    private final Connector systemConnector;

    public LocalConsumerTransferProcessClient(Connector systemConnector) {
        this.systemConnector = systemConnector;
    }

    @Override
    public void initiateTransferRequest(String agreementId, String format) {
        systemConnector.getConsumerTransferProcessManager().createTransferProcess(agreementId, format, null, null);
    }

    @Override
    public void terminateTransfer(String counterPartyPid, Map<String, Object> terminationMessage, String callbackAddress, boolean expectError) {
        systemConnector.getConsumerTransferProcessManager().handleTermination(processJsonLd(terminationMessage));
    }

    @Override
    public void completeTransfer(String counterPartyPid, Map<String, Object> completionMessage, String callbackAddress, boolean expectError) {
        systemConnector.getConsumerTransferProcessManager().handleCompletion(processJsonLd(completionMessage));
    }

    @Override
    public void suspendTransfer(String counterPartyPid, Map<String, Object> suspensionMessage, String callbackAddress, boolean expectError) {
        systemConnector.getConsumerTransferProcessManager().handleSuspension(processJsonLd(suspensionMessage));
    }

    @Override
    public void startTransfer(String counterPartiPid, Map<String, Object> startMessage, String callbackAddress, boolean expectError) {
        systemConnector.getConsumerTransferProcessManager().handleStart(processJsonLd(startMessage));
    }

    @Override
    public Map<String, Object> getTransferProcess(String counterPartyPid, String callbackAddress) {
        var tp = systemConnector.getConsumerTransferProcessManager().findById(counterPartyPid);
        return processJsonLd(createTransferResponse(tp.providerPid(), tp.consumerPid(), tp.getState().toString()));
    }

}
