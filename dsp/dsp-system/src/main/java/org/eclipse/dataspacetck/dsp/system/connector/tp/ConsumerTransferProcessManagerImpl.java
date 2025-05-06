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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_DATA_ADDRESS_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_PROPERTIES_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_PROPERTY_NAME_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_PROPERTY_VALUE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_TYPE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.mapProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;

/**
 * Manages transfer processes on a consumer.
 */
public class ConsumerTransferProcessManagerImpl extends AbstractTransferProcessManager implements ConsumerTransferProcessManager {

    private final Monitor monitor;

    public ConsumerTransferProcessManagerImpl(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public TransferProcess createTransferProcess(String agreementId, String format, TransferProcess.DataAddress dataAddress) {
        var transferProcess = TransferProcess.Builder.newInstance()
                .agreementId(agreementId)
                .format(format)
                .dataAddress(dataAddress)
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
    public Map<String, Object> handleStart(Map<String, Object> startMessage, Predicate<TransferProcess.DataAddress> test) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, startMessage);
        monitor.debug("Received start message: " + providerId);

        var dataAddress = toDataAddress(mapProperty(DSPACE_PROPERTY_DATA_ADDRESS_EXPANDED, startMessage, true));

        if (!test.test(dataAddress)) {
            monitor.debug("Data address predicate failed");
            throw new AssertionError("Data address predicate failed");
        }
        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, startMessage);
        var transfer = findById(consumerId);
        transfer.setDataAddress(dataAddress);
        transfer.transition(STARTED, p -> listeners.forEach(l -> l.started(transfer)));
        return createTransferResponse(transfer.getCorrelationId(), transfer.getId(), STARTED.toString());
    }

    @Override
    public Map<String, Object> handleTermination(Map<String, Object> terminatedMessage) {
        var providerId = stringIdProperty(DSPACE_PROPERTY_PROVIDER_PID_EXPANDED, terminatedMessage);
        monitor.debug("Received terminated message: " + providerId);

        var consumerId = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, terminatedMessage);
        var transfer = findById(consumerId);
        transfer.transition(TERMINATED, p -> listeners.forEach(l -> l.terminated(transfer)));
        return createTransferResponse(transfer.getCorrelationId(), transfer.getId(), TERMINATED.toString());
    }

    private TransferProcess.DataAddress toDataAddress(Map<String, Object> dataAddress) {
        if (dataAddress == null) {
            return null;
        }

        var endpointType = stringIdProperty(DSPACE_PROPERTY_ENDPOINT_TYPE_EXPANDED, dataAddress);
        var endpoint = stringProperty(DSPACE_PROPERTY_ENDPOINT_EXPANDED, dataAddress);
        var endpointProperties = toEndpointProperties(dataAddress.get(DSPACE_PROPERTY_ENDPOINT_PROPERTIES_EXPANDED));
        return new TransferProcess.DataAddress(endpointType, endpoint, endpointProperties);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> toEndpointProperties(Object endpointProperties) {
        if (endpointProperties == null) {
            return Map.of();
        }
        var properties = new HashMap<String, String>();
        if (endpointProperties instanceof List<?>) {
            for (var property : (List<?>) endpointProperties) {
                if (property instanceof Map<?, ?>) {
                    var map = (Map<String, Object>) property;
                    var name = stringProperty(DSPACE_PROPERTY_ENDPOINT_PROPERTY_NAME_EXPANDED, map);
                    var value = stringProperty(DSPACE_PROPERTY_ENDPOINT_PROPERTY_VALUE_EXPANDED, map);
                    properties.put(name, value);
                }
            }
        }
        return properties;
    }
}
