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
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessListener;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Predicate;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_DATA_ADDRESS_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_PROPERTIES_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_PROPERTY_NAME_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_PROPERTY_VALUE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_TYPE_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.mapProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.COMPLETED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;

/**
 * Base implementation.
 */
public abstract class AbstractTransferProcessManager implements TransferProcessManager {
    private final Monitor monitor;
    protected Map<String, TransferProcess> transferProcesses = new ConcurrentHashMap<>();
    protected Queue<TransferProcessListener> listeners = new ConcurrentLinkedQueue<>();

    protected AbstractTransferProcessManager(Monitor monitor) {
        this.monitor = monitor;
    }

    @NotNull
    @Override
    public TransferProcess findById(String id) {
        return transferProcesses.get(id);
    }

    @Nullable
    @Override
    public TransferProcess findByCorrelationId(String id) {
        return transferProcesses.values().stream()
                .filter(n -> id.equals(n.getCorrelationId()))
                .findAny().orElse(null);
    }


    @Override
    public void registerListener(TransferProcessListener listener) {
        listeners.add(listener);
    }

    @Override
    public void deregisterListener(TransferProcessListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void terminated(String providerId) {
        var transfer = findById(providerId);
        transfer.transition(TERMINATED);
    }

    @Override
    public void completed(String providerId) {
        var transfer = findById(providerId);
        transfer.transition(COMPLETED);
    }

    @Override
    public void suspended(String providerId) {
        var transfer = findById(providerId);
        transfer.transition(SUSPENDED);
    }

    @Override
    public void started(String providerId) {
        var transfer = findById(providerId);
        transfer.transition(STARTED);
    }

    @Override
    public Map<String, Object> handleCompletion(Map<String, Object> completionMessage) {
        var ids = parseId(completionMessage);
        monitor.debug(format("Received completion message: %s with correlation id %s", ids.id, ids.correlationId));

        var transfer = findById(ids.id);
        transfer.transition(COMPLETED, p -> listeners.forEach(l -> l.completed(transfer)));
        return createTransferResponse(transfer.getCorrelationId(), transfer.getId(), COMPLETED.toString());
    }

    @Override
    public Map<String, Object> handleTermination(Map<String, Object> terminatedMessage) {
        var ids = parseId(terminatedMessage);
        monitor.debug(format("Received terminated message: %s with correlation id %s", ids.id, ids.correlationId));
        var transfer = findById(ids.id);
        transfer.transition(TERMINATED, p -> listeners.forEach(l -> l.terminated(transfer)));
        return createTransferResponse(transfer.getCorrelationId(), transfer.getId(), TERMINATED.toString());
    }

    @Override
    public Map<String, Object> handleSuspension(Map<String, Object> suspensionMessage) {
        var ids = parseId(suspensionMessage);
        monitor.debug(format("Received suspension message: %s with correlation id %s", ids.id, ids.correlationId));
        var transfer = findById(ids.id);
        transfer.transition(SUSPENDED, p -> listeners.forEach(l -> l.suspended(transfer)));
        return createTransferResponse(transfer.getCorrelationId(), transfer.getId(), SUSPENDED.toString());
    }

    @Override
    public Map<String, Object> handleStart(Map<String, Object> startMessage, Predicate<TransferProcess.DataAddress> test) {
        var ids = parseId(startMessage);
        monitor.debug(format("Received start message: %s with correlation id %s", ids.id, ids.correlationId));

        var dataAddress = toDataAddress(mapProperty(DSPACE_PROPERTY_DATA_ADDRESS_EXPANDED, startMessage, true));

        if (!test.test(dataAddress)) {
            monitor.debug("Data address predicate failed");
            throw new AssertionError("Data address predicate failed");
        }
        var transfer = findById(ids.id);
        transfer.setDataAddress(dataAddress);
        transfer.transition(STARTED, p -> listeners.forEach(l -> l.started(transfer)));
        return createTransferResponse(transfer.getCorrelationId(), transfer.getId(), STARTED.toString());
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

    protected abstract TransferId parseId(Map<String, Object> message);

    protected record TransferId(String id, String correlationId) {
    }
}
