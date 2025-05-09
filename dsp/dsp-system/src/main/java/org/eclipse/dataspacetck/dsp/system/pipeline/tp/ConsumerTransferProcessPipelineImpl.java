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
        super(transferProcessClient, endpoint, monitor, waitTime);
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
    protected ConsumerTransferProcessPipeline self() {
        return this;
    }

    @Override
    protected void suspended(String id) {
        providerConnector.getProviderTransferProcessManager().suspended(id);
    }

    @Override
    protected void completed(String id) {
        providerConnector.getProviderTransferProcessManager().completed(id);
    }

    @Override
    protected void terminated(String id) {
        providerConnector.getProviderTransferProcessManager().terminated(id);
    }

    @Override
    protected void started(String id) {
        providerConnector.getProviderTransferProcessManager().started(id);
    }

    @Override
    public ConsumerTransferProcessPipeline thenVerifyConsumerState(TransferProcess.State state) {
        thenWait("for consumer transfer process state to be " + state, () -> {
            var callbackAddress = transferProcess.getCallbackAddress();
            var processId = this.transferProcess.getCorrelationId();
            var tp = transferProcessClient.getTransferProcess(processId, callbackAddress);
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, tp);
            return (DSPACE_NAMESPACE + state.toString()).equals(actual);
        });
        return this;
    }

}
