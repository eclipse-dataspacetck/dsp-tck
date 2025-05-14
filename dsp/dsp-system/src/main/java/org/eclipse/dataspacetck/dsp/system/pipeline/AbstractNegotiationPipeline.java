/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */
package org.eclipse.dataspacetck.dsp.system.pipeline;

import org.eclipse.dataspacetck.core.api.pipeline.AbstractAsyncPipeline;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.NegotiationPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation;
import org.eclipse.dataspacetck.dsp.system.client.cn.NegotiationClient;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.NegotiationFunctions.createTermination;

/**
 * Base negotiation pipeline functionality.
 */
public abstract class AbstractNegotiationPipeline<P extends NegotiationPipeline<P>> extends AbstractAsyncPipeline<P> implements NegotiationPipeline<P> {
    private static final String NEGOTIATIONS_TERMINATION_PATH = "/negotiations/[^/]+/termination/";
    private final NegotiationClient negotiationClient;
    protected ContractNegotiation providerNegotiation;

    public AbstractNegotiationPipeline(NegotiationClient negotiationClient, CallbackEndpoint endpoint, Monitor monitor, long waitTime) {
        super(endpoint, monitor, waitTime);
        this.negotiationClient = negotiationClient;
    }

    public P thenWaitForState(ContractNegotiation.State state) {
        return thenWait("state to transition to " + state, () -> providerNegotiation != null && state == providerNegotiation.getState());
    }

    @Override
    public P expectTerminationMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        return expectResponse(NEGOTIATIONS_TERMINATION_PATH, action);
    }

    @NotNull
    private P expectResponse(String path, Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(path, event -> {
                    var expanded = processJsonLd(event);
                    var response = action.apply(expanded);
                    endpoint.deregisterHandler(path);
                    latch.countDown();
                    return serialize(processJsonLd(response));
                }));
        return self();
    }

    @Override
    public P sendTermination(boolean expectError) {
        stages.add(() -> {
            pause();
            var id = providerNegotiation.getId();
            var correlationId = providerNegotiation.getCorrelationId();
            var termination = createTermination(providerNegotiation.providerPid(), providerNegotiation.consumerPid(), "1");

            monitor.debug("Sending termination: " + correlationId);
            negotiationClient.terminate(correlationId, termination, providerNegotiation.getCallbackAddress(), expectError);
            if (!expectError) {
                terminated(id);
            }
        });
        return self();
    }

    protected abstract void terminated(String id);
    
    protected abstract P self();
}
