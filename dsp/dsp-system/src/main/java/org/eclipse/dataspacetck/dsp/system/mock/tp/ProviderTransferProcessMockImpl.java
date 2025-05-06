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

package org.eclipse.dataspacetck.dsp.system.mock.tp;

import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ProviderTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessListener;
import org.eclipse.dataspacetck.dsp.system.api.mock.tp.ProviderTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;

/**
 * Default mock consumer implementation.
 */
public class ProviderTransferProcessMockImpl extends AbstractTransferProcessMock implements ProviderTransferProcessMock, TransferProcessListener {
    private final ProviderTransferProcessManager manager;
    private final String baseAddress;

    public ProviderTransferProcessMockImpl(ProviderTransferProcessManager manager, Executor executor, String baseAddress) {
        super(executor);
        this.manager = manager;
        this.baseAddress = baseAddress;
        manager.registerListener(this);
    }

    @Override
    public void verify() {
        super.verify();
        manager.deregisterListener(this);
    }

    @Override
    public void recordTransferRequestedAction(Action action) {
        recordAction(REQUESTED, action);
    }

    protected void recordAction(State state, Action action) {
        actions.computeIfAbsent(state, k -> new ConcurrentLinkedQueue<>()).add(action);
    }

    @Override
    public void requested(TransferProcess transferProcess) {
        var action = actions.getOrDefault(REQUESTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(transferProcess));
    }

    @Override
    public void started(TransferProcess transferProcess) {
        var action = actions.getOrDefault(STARTED, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(transferProcess));
    }
}
