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

package org.eclipse.dataspacetck.dsp.system.pipeline.tp;

import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessListener;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.ConsumerTransferProcessPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.eclipse.dataspacetck.dsp.system.client.tp.ConsumerTransferProcessClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTermination;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConsumerTransferProcessPipelineImpl extends AbstractTransferProcessPipeline<ConsumerTransferProcessPipeline> implements ConsumerTransferProcessPipeline {

    private static final String REQUEST_PATH = "/transfers/request";

    private final ConsumerTransferProcessClient transferProcessClient;
    private final Connector providerConnector;
    private final String consumerConnectorId;

    public ConsumerTransferProcessPipelineImpl(ConsumerTransferProcessClient transferProcessClient,
                                               CallbackEndpoint endpoint,
                                               Connector providerConnector,
                                               String consumerConnectorId,
                                               Monitor monitor,
                                               long waitTime) {
        super(endpoint, monitor, waitTime);
        this.transferProcessClient = transferProcessClient;
        this.providerConnector = providerConnector;
        this.consumerConnectorId = consumerConnectorId;
    }

    @Override
    public ConsumerTransferProcessPipeline initiateTransferRequest(String agreementId, String format) {
        stages.add(() -> {
            // Register a listener to record the transfer Process that is created when the TCK connector receives the
            // transfer request from the consumer under test.
            providerConnector.getProviderTransferProcessManager().registerListener(new TransferProcessListener() {
                @Override
                public void requested(TransferProcess transferProcess) {
                    ConsumerTransferProcessPipelineImpl.this.transferProcess = transferProcess;
                    // Remove the listener
                    providerConnector.getProviderTransferProcessManager().deregisterListener(this);
                }
            });
            // call the consumer endpoint
            transferProcessClient.initiateTransferRequest(agreementId, format);
        });
        return this;
    }

    @Override
    public ConsumerTransferProcessPipeline expectTransferRequest(BiFunction<Map<String, Object>, String, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(REQUEST_PATH, event -> {
                    var expanded = processJsonLd(event);
                    var negotiation = action.apply(expanded, consumerConnectorId);
                    endpoint.deregisterHandler(REQUEST_PATH);
                    latch.countDown();
                    return serialize(processJsonLd(negotiation));
                }));
        return this;
    }

    @Override
    public ConsumerTransferProcessPipeline sendStarted(Map<String, Object> dataAddress) {
        stages.add(() -> {
            var providerId = transferProcess.getId();
            var consumerId = transferProcess.getCorrelationId();
            var startMessage = createStartRequest(providerId, consumerId, dataAddress);
            monitor.debug("Sending transfer start");
            var consumerAddress = transferProcess.getCallbackAddress();
            transferProcessClient.startTransfer(consumerId, startMessage, consumerAddress, false);
            providerConnector.getProviderTransferProcessManager().started(providerId);
        });
        return this;
    }

    @Override
    public ConsumerTransferProcessPipeline sendTermination(boolean expectError) {
        stages.add(() -> {
            var providerId = transferProcess.getId();
            var consumerId = transferProcess.getCorrelationId();
            var terminationMessage = createTermination(providerId, consumerId, "1");
            monitor.debug("Sending transfer termination");
            var consumerAddress = transferProcess.getCallbackAddress();
            transferProcessClient.terminateTransfer(consumerId, terminationMessage, consumerAddress, expectError);
            providerConnector.getProviderTransferProcessManager().terminated(providerId);
        });
        return this;
    }

    @Override
    public ConsumerTransferProcessPipeline thenVerifyConsumerState(TransferProcess.State state) {
        stages.add(() -> {
            pause();
            var callbackAddress = transferProcess.getCallbackAddress();
            var processId = this.transferProcess.getCorrelationId();
            var negotiation = transferProcessClient.getTransferProcess(processId, callbackAddress);
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, negotiation);
            assertEquals(DSPACE_NAMESPACE + state.toString(), actual);
        });
        return this;
    }
}
