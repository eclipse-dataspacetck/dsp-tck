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

import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessListener;
import org.eclipse.dataspacetck.dsp.system.api.mock.tp.TransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.mockito.internal.util.StringUtil.join;

/**
 * Base negotiation mock functionality.
 */
public abstract class AbstractTransferProcessMock implements TransferProcessMock, TransferProcessListener {
    protected static final Queue<Action> EMPTY_QUEUE = new ArrayDeque<>();
    protected Executor executor;
    protected Map<State, Queue<Action>> actions = new ConcurrentHashMap<>();

    public AbstractTransferProcessMock(Executor executor) {
        this.executor = executor;
    }

    public void verify() {
        if (!actions.isEmpty()) {
            var actions = this.actions.entrySet().stream()
                    .filter(e -> !e.getValue().isEmpty())
                    .map(e -> e.getKey().toString())
                    .collect(toList());
            if (!actions.isEmpty()) {
                throw new AssertionError(format("Request actions not executed.\n Actions: %s", join(", ", actions)));
            }
        }
    }

    @Override
    public boolean completed() {
        return actions.isEmpty();
    }

    @Override
    public void reset() {
        actions.clear();
    }

    protected void recordAction(State state, Action action) {
        actions.computeIfAbsent(state, k -> new ConcurrentLinkedQueue<>()).add(action);
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
