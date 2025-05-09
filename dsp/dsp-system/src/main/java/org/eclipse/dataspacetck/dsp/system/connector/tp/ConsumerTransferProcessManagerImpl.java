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
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ConsumerTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.Map;

import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.TransferKind.Consumer;

/**
 * Manages transfer processes on a consumer.
 */
public class ConsumerTransferProcessManagerImpl extends AbstractTransferProcessManager implements ConsumerTransferProcessManager {

    private final Monitor monitor;

    public ConsumerTransferProcessManagerImpl(Monitor monitor) {
        super(monitor);
        this.monitor = monitor;
    }

    @Override
    public TransferProcess createTransferProcess(String agreementId, String format, String providerBaseUrl, TransferProcess.DataAddress dataAddress) {
        var transferProcess = TransferProcess.Builder.newInstance()
                .agreementId(agreementId)
                .format(format)
                .callbackAddress(providerBaseUrl)
                .dataAddress(dataAddress)
                .transferKind(Consumer)
                .build();
        transferProcesses.put(transferProcess.getId(), transferProcess);
        listeners.forEach(l -> l.transferInitialized(transferProcess));

        return transferProcess;
    }

    @Override
    public void transferRequested(String consumerId, String providerId) {
        var transfer = findById(consumerId);
        transfer.setCorrelationId(providerId);
        transfer.transition(REQUESTED, p -> listeners.forEach(l -> l.requested(transfer)));
    }


    @Override
    protected TransferId parseId(Map<String, Object> message) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, message);
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, message);
        return new TransferId(consumerId, providerId);
    }
}
