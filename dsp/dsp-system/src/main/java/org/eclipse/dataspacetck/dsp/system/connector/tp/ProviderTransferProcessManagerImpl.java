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

package org.eclipse.dataspacetck.dsp.system.connector.tp;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ProviderTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.Map;

import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.TransferKind.Provider;

public class ProviderTransferProcessManagerImpl extends AbstractTransferProcessManager implements ProviderTransferProcessManager {
    private final Monitor monitor;

    public ProviderTransferProcessManagerImpl(Monitor monitor) {
        super(monitor);
        this.monitor = monitor;
    }

    @Override
    public Map<String, Object> handleTransferRequest(Map<String, Object> transferRequest, String counterPartyId) {
        var consumerPid = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, transferRequest);
        monitor.debug("Received transfer request message with consumer pid: " + consumerPid);

        var prevTransfer = findByCorrelationId(consumerPid);
        if (prevTransfer != null) {
            return createTransferResponse(prevTransfer.getId(), prevTransfer.getCorrelationId(), prevTransfer.getState().toString());
        }
        var callbackAddress = stringProperty(DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED, transferRequest);
        var transfer = TransferProcess.Builder.newInstance()
                .correlationId(consumerPid)
                .state(TransferProcess.State.REQUESTED)
                .callbackAddress(callbackAddress)
                .transferKind(Provider)
                .build();

        transferProcesses.put(transfer.getId(), transfer);
        listeners.forEach(l -> l.requested(transfer));

        return createTransferResponse(transfer.getId(), transfer.getCorrelationId(), transfer.getState().toString());

    }

    @Override
    protected TransferId parseId(Map<String, Object> message) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, message);
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, message);
        return new TransferId(providerId, consumerId);
    }
}
