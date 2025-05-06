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

package org.eclipse.dataspacetck.dsp.system.api.connector.tp;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Manages transfer processes. Subclasses implement specific behavior for consumer and provider state transitions.
 */
public interface TransferProcessManager {

    /**
     * Returns a transfer process by id or throws an {@code IllegalArgumentException} if not found.
     */
    @NotNull
    TransferProcess findById(String id);

    /**
     * Returns a transfer process by correlation id or null if not found.
     */
    @Nullable
    TransferProcess findByCorrelationId(String id);
    
    /**
     * Registers a listener.
     */
    void registerListener(TransferProcessListener listener);

    /**
     * Removes a listener.
     */
    void deregisterListener(TransferProcessListener listener);
}
