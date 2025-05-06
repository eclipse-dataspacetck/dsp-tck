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

import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessListener;
import org.eclipse.dataspacetck.dsp.system.api.connector.tp.TransferProcessManager;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Base implementation.
 */
public abstract class AbstractTransferProcessManager implements TransferProcessManager {
    protected Map<String, TransferProcess> transferProcesses = new ConcurrentHashMap<>();
    protected Queue<TransferProcessListener> listeners = new ConcurrentLinkedQueue<>();


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

}
