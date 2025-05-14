/*
 *  Copyright (c) 2024 Metaform Systems, Inc.
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

package org.eclipse.dataspacetck.dsp.system.connector;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationListener;
import org.eclipse.dataspacetck.dsp.system.api.connector.NegotiationManager;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.dsp.system.api.message.NegotiationFunctions.createNegotiationResponse;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;

/**
 * Base implementation.
 */
public abstract class AbstractNegotiationManager implements NegotiationManager {
    private final Monitor monitor;
    protected Map<String, ContractNegotiation> negotiations = new ConcurrentHashMap<>();
    protected Queue<NegotiationListener> listeners = new ConcurrentLinkedQueue<>();

    protected AbstractNegotiationManager(Monitor monitor) {
        this.monitor = monitor;
    }

    @NotNull
    @Override
    public ContractNegotiation findById(String id) {
        var negotiation = negotiations.get(id);
        if (negotiation == null) {
            throw new IllegalArgumentException("Contract negotiation not found for id: " + id);
        }
        return negotiation;
    }

    @Nullable
    @Override
    public ContractNegotiation findByCorrelationId(String id) {
        return negotiations.values().stream()
                .filter(n -> id.equals(n.getCorrelationId()))
                .findAny().orElse(null);
    }

    @Override
    public Map<String, ContractNegotiation> getNegotiations() {
        return negotiations;
    }

    @Override
    public void registerListener(NegotiationListener listener) {
        listeners.add(listener);
    }

    @Override
    public void deregisterListener(NegotiationListener listener) {
        listeners.remove(listener);
    }


    @Override
    public Map<String, Object> handleTermination(Map<String, Object> terminatedMessage) {
        var ids = parseId(terminatedMessage);
        monitor.debug(format("Received terminated message: %s with correlation id %s", ids.id, ids.correlationId));
        var negotiation = findById(ids.id);
        negotiation.transition(TERMINATED, n -> listeners.forEach(l -> l.terminated(negotiation)));
        return createNegotiationResponse(negotiation.getCorrelationId(), negotiation.getId(), TERMINATED.toString());
    }

    protected abstract NegotiationId parseId(Map<String, Object> message);

    @Override
    public void terminated(String id) {
        var cn = findById(id);
        cn.transition(TERMINATED, n -> listeners.forEach(l -> l.terminated(n)));
    }

    protected record NegotiationId(String id, String correlationId) {
    }
}
