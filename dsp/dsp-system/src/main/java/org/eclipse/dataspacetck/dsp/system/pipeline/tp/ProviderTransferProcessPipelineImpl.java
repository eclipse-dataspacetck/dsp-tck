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
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.ProviderTransferProcessPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.eclipse.dataspacetck.dsp.system.client.tp.ProviderTransferProcessClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferRequest;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProviderTransferProcessPipelineImpl extends AbstractTransferProcessPipeline<ProviderTransferProcessPipeline> implements ProviderTransferProcessPipeline {

    private static final String TRANSFER_START_PATH = "/transfers/[^/]+/start";
    private static final String TRANSFER_TERMINATION_PATH = "/transfers/[^/]+/termination";


    private final ProviderTransferProcessClient transferProcessClient;
    private final Connector consumerConnector;
    private final String providerConnectorId;

    public ProviderTransferProcessPipelineImpl(ProviderTransferProcessClient transferProcessClient,
                                               CallbackEndpoint endpoint,
                                               Connector consumerConnector,
                                               String providerConnectorId,
                                               Monitor monitor,
                                               long waitTime) {
        super(endpoint, monitor, waitTime);
        this.transferProcessClient = transferProcessClient;
        this.consumerConnector = consumerConnector;
        this.providerConnectorId = providerConnectorId;
    }

    @Override
    public ProviderTransferProcessPipeline sendTransferRequest(String agreementId, String format, TransferProcess.DataAddress dataAddress) {
        stages.add(() -> {
            transferProcess = consumerConnector.getConsumerTransferProcessManager().createTransferProcess(agreementId, format, dataAddress);
            var contractRequest = createTransferRequest(transferProcess.getId(), transferProcess.getAgreementId(), transferProcess.getFormat(), transferProcess.getDataAddress(), endpoint.getAddress());

            monitor.debug("Sending transfer request");
            var response = transferProcessClient.transferRequest(contractRequest, providerConnectorId, false);
            var correlationId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, response);
            consumerConnector.getConsumerTransferProcessManager().transferRequested(transferProcess.getId(), correlationId);
        });
        return this;
    }

    @Override
    public ProviderTransferProcessPipeline expectStartMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(TRANSFER_START_PATH, offer -> {
                    var transfer = action.apply((processJsonLd(offer)));
                    endpoint.deregisterHandler(TRANSFER_START_PATH);
                    latch.countDown();
                    return serialize(transfer);
                }));
        return this;
    }

    @Override
    public ProviderTransferProcessPipeline expectTerminationMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(TRANSFER_TERMINATION_PATH, offer -> {
                    var transfer = action.apply((processJsonLd(offer)));
                    endpoint.deregisterHandler(TRANSFER_TERMINATION_PATH);
                    latch.countDown();
                    return serialize(transfer);
                }));

        return this;
    }

    @Override
    public ProviderTransferProcessPipeline thenVerifyProviderState(TransferProcess.State state) {
        stages.add(() -> {
            pause();
            var providerTransferProcess = transferProcessClient.getTransferProcess(transferProcess.getCorrelationId());
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, providerTransferProcess);
            assertEquals(DSPACE_NAMESPACE + state.toString(), actual);
        });
        return this;
    }
}
