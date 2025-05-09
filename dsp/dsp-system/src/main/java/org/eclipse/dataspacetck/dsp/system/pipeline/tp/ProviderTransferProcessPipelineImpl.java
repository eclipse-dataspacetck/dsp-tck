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

import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_NAMESPACE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferRequest;

public class ProviderTransferProcessPipelineImpl extends AbstractTransferProcessPipeline<ProviderTransferProcessPipeline> implements ProviderTransferProcessPipeline {

    private final ProviderTransferProcessClient transferProcessClient;
    private final Connector consumerConnector;
    private final String providerBaseUrl;
    private final String providerConnectorId;

    public ProviderTransferProcessPipelineImpl(ProviderTransferProcessClient transferProcessClient,
                                               CallbackEndpoint endpoint,
                                               Connector consumerConnector,
                                               String providerBaseUrl,
                                               String providerConnectorId,
                                               Monitor monitor,
                                               long waitTime) {
        super(transferProcessClient, endpoint, monitor, waitTime);
        this.transferProcessClient = transferProcessClient;
        this.consumerConnector = consumerConnector;
        this.providerBaseUrl = providerBaseUrl;
        this.providerConnectorId = providerConnectorId;
    }

    @Override
    public ProviderTransferProcessPipeline sendTransferRequest(String agreementId, String format, TransferProcess.DataAddress dataAddress) {
        stages.add(() -> {
            transferProcess = consumerConnector.getConsumerTransferProcessManager().createTransferProcess(agreementId, format, providerBaseUrl, dataAddress);
            var contractRequest = createTransferRequest(transferProcess.getId(), transferProcess.getAgreementId(), transferProcess.getFormat(), transferProcess.getDataAddress(), endpoint.getAddress());

            monitor.debug("Sending transfer request");
            var response = transferProcessClient.transferRequest(contractRequest, providerConnectorId, false);
            var correlationId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, response);
            consumerConnector.getConsumerTransferProcessManager().transferRequested(transferProcess.getId(), correlationId);
        });
        return this;
    }

    @Override
    protected ProviderTransferProcessPipeline self() {
        return this;
    }

    @Override
    public ProviderTransferProcessPipeline thenVerifyProviderState(TransferProcess.State state) {
        thenWait("for provider transfer process state to be " + state, () -> {
            var providerTransferProcess = transferProcessClient.getTransferProcess(transferProcess.getCorrelationId(), transferProcess.getCallbackAddress());
            var actual = stringIdProperty(DSPACE_PROPERTY_STATE_EXPANDED, providerTransferProcess);
            return (DSPACE_NAMESPACE + state.toString()).equals(actual);
        });
        return this;
    }

    @Override
    protected void suspended(String id) {
        consumerConnector.getConsumerTransferProcessManager().suspended(id);
    }

    @Override
    protected void completed(String id) {
        consumerConnector.getConsumerTransferProcessManager().completed(id);
    }

    @Override
    protected void terminated(String id) {
        consumerConnector.getConsumerTransferProcessManager().terminated(id);
    }

    @Override
    protected void started(String id) {
        consumerConnector.getConsumerTransferProcessManager().started(id);
    }
}
