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
import org.eclipse.dataspacetck.dsp.system.api.service.Result;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.Map;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.INITIALIZED;
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
        synchronized (transfer) {
            if (transfer.getState() != INITIALIZED) {
                // the provider's start message arrived before the transfer request response was processed and
                // already moved the transfer to REQUESTED; only verify that both messages agree on the provider pid
                if (!providerId.equals(transfer.getCorrelationId())) {
                    throw new IllegalStateException(format("Provider pid %s in the transfer request response does not match provider pid %s of the start message",
                            providerId, transfer.getCorrelationId()));
                }
                return;
            }
            requested(transfer, providerId);
        }
    }

    /**
     * Handles a start message. DSP allows the provider to send the start message as soon as it has accepted the
     * transfer request, so it may arrive while the transfer request is still in flight (state {@code INITIALIZED}).
     * In that case the start message implies the request was accepted, and the transfer is moved to {@code REQUESTED}
     * using the provider pid it carries before it is started.
     */
    @Override
    public Result<Map<String, Object>, Map<String, Object>> handleStart(Map<String, Object> startMessage, Predicate<TransferProcess.DataAddress> test) {
        var ids = parseId(startMessage);
        var transfer = ids.id() != null ? findById(ids.id()) : null;
        if (transfer != null && ids.correlationId() != null) {
            synchronized (transfer) {
                if (transfer.getState() == INITIALIZED) {
                    monitor.debug(format("Received start message for transfer %s before the transfer request response, marking it as requested", ids.id()));
                    requested(transfer, ids.correlationId());
                }
            }
        }
        return super.handleStart(startMessage, test);
    }

    private void requested(TransferProcess transfer, String providerId) {
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
