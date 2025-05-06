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

import org.eclipse.dataspacetck.dsp.system.api.connector.tp.ConsumerTransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessListener;
import org.eclipse.dataspacetck.dsp.system.api.mock.tp.ConsumerTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.concurrent.Executor;
import java.util.function.BiConsumer;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.INITIALIZED;

/**
 * Default mock consumer implementation.
 */
public class ConsumerTransferProcessMockImpl extends AbstractTransferProcessMock implements ConsumerTransferProcessMock, TransferProcessListener {
    private final ConsumerTransferProcessManager manager;
    private final String baseAddress;

    public ConsumerTransferProcessMockImpl(ConsumerTransferProcessManager manager, Executor executor, String baseAddress) {
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
    public void recordInitializedAction(BiConsumer<String, TransferProcess> action) {
        recordAction(INITIALIZED, cn -> {
            action.accept(baseAddress, cn);
        });
    }

    @Override
    public void transferInitialized(TransferProcess transferProcess) {
        received(INITIALIZED, transferProcess);
    }

    private void received(State state, TransferProcess transferProcess) {
        var action = actions.getOrDefault(state, EMPTY_QUEUE).poll();
        if (action == null) {
            return;
        }
        executor.execute(() -> action.accept(transferProcess));
    }
}
