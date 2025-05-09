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

import org.eclipse.dataspacetck.core.api.pipeline.AbstractAsyncPipeline;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.TransferProcessPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.eclipse.dataspacetck.dsp.system.client.tp.TransferProcessClient;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createCompletion;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createSuspension;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTermination;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;

public abstract class AbstractTransferProcessPipeline<P extends TransferProcessPipeline<P>> extends AbstractAsyncPipeline<P> implements TransferProcessPipeline<P> {
    private static final String TRANSFER_START_PATH = "/transfers/[^/]+/start";
    private static final String TRANSFER_TERMINATION_PATH = "/transfers/[^/]+/termination";
    private static final String TRANSFER_COMPLETION_PATH = "/transfers/[^/]+/completion";
    private static final String TRANSFER_SUSPENSION_PATH = "/transfers/[^/]+/suspension";

    protected final TransferProcessClient transferProcessClient;
    protected TransferProcess transferProcess;

    public AbstractTransferProcessPipeline(TransferProcessClient transferProcessClient, CallbackEndpoint endpoint, Monitor monitor, long waitTime) {
        super(endpoint, monitor, waitTime);
        this.transferProcessClient = transferProcessClient;
    }

    public P thenWaitForState(State state) {
        return thenWait("state to transition to " + state, () -> transferProcess != null && state == transferProcess.getState());
    }

    @Override
    public P thenPause() {
        stages.add(this::pause);
        return self();
    }

    @Override
    public P sendTermination(boolean expectError) {
        stages.add(() -> {
            var id = transferProcess.getId();
            var correlationId = transferProcess.getCorrelationId();
            var terminationMessage = createTermination(transferProcess.providerPid(), transferProcess.consumerPid(), "1");
            monitor.debug("Sending transfer termination");
            var consumerAddress = transferProcess.getCallbackAddress();
            transferProcessClient.terminateTransfer(correlationId, terminationMessage, consumerAddress, expectError);
            terminated(id);
        });
        return self();
    }

    @Override
    public P sendCompletion(boolean expectError) {
        stages.add(() -> {
            var id = transferProcess.getId();
            var correlationId = transferProcess.getCorrelationId();
            var completion = createCompletion(transferProcess.providerPid(), transferProcess.consumerPid());
            monitor.debug("Sending transfer completion");
            var consumerAddress = transferProcess.getCallbackAddress();
            transferProcessClient.completeTransfer(correlationId, completion, consumerAddress, expectError);
            completed(id);
        });
        return self();
    }

    @Override
    public P sendSuspension(boolean expectError) {
        stages.add(() -> {
            var id = transferProcess.getId();
            var correlationId = transferProcess.getCorrelationId();
            var suspension = createSuspension(transferProcess.providerPid(), transferProcess.consumerPid(), "1");
            monitor.debug("Sending transfer suspension");
            var consumerAddress = transferProcess.getCallbackAddress();
            transferProcessClient.suspendTransfer(correlationId, suspension, consumerAddress, expectError);
            suspended(id);
        });
        return self();
    }

    @Override
    public P sendStarted(Map<String, Object> dataAddress) {
        stages.add(() -> {
            var id = transferProcess.getId();
            var correlationId = transferProcess.getCorrelationId();
            var startMessage = createStartRequest(transferProcess.providerPid(), transferProcess.consumerPid(), dataAddress);
            monitor.debug("Sending transfer start");
            var consumerAddress = transferProcess.getCallbackAddress();
            transferProcessClient.startTransfer(correlationId, startMessage, consumerAddress, false);
            started(id);
        });
        return self();
    }

    @Override
    public P expectStartMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(TRANSFER_START_PATH, msg -> {
                    var transfer = action.apply((processJsonLd(msg)));
                    endpoint.deregisterHandler(TRANSFER_START_PATH);
                    latch.countDown();
                    return serialize(transfer);
                }));
        return self();
    }

    @Override
    public P expectTerminationMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(TRANSFER_TERMINATION_PATH, offer -> {
                    var transfer = action.apply((processJsonLd(offer)));
                    endpoint.deregisterHandler(TRANSFER_TERMINATION_PATH);
                    latch.countDown();
                    return serialize(transfer);
                }));

        return self();
    }

    @Override
    public P expectCompletionMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(TRANSFER_COMPLETION_PATH, offer -> {
                    var transfer = action.apply((processJsonLd(offer)));
                    endpoint.deregisterHandler(TRANSFER_COMPLETION_PATH);
                    latch.countDown();
                    return serialize(transfer);
                }));

        return self();
    }

    @Override
    public P expectSuspensionMessage(Function<Map<String, Object>, Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(TRANSFER_SUSPENSION_PATH, offer -> {
                    var transfer = action.apply((processJsonLd(offer)));
                    endpoint.deregisterHandler(TRANSFER_SUSPENSION_PATH);
                    latch.countDown();
                    return serialize(transfer);
                }));

        return self();
    }

    protected abstract void started(String id);

    protected abstract void completed(String id);

    protected abstract void terminated(String id);

    protected abstract void suspended(String id);

    protected abstract P self();

}
