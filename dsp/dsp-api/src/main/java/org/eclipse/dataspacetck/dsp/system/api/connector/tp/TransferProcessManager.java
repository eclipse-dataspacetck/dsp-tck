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

import org.eclipse.dataspacetck.dsp.system.api.service.Result;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Predicate;

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

    /**
     * Transition to the "terminated" state.
     */
    void terminated(String providerId);

    /**
     * Transition to the "completed" state.
     */
    void completed(String providerId);

    /**
     * Transition to the "suspended" state.
     */
    void suspended(String providerId);

    /**
     * Transition to the "started" state.
     */
    void started(String providerId);

    /**
     * Processes a transfer completion message received from the counter-party.
     */
    Result<Map<String, Object>, Map<String, Object>> handleCompletion(Map<String, Object> completionMessage);

    /**
     * Processes a transfer termination message received from the counter-party.
     */
    Map<String, Object> handleTermination(Map<String, Object> terminatedMessage);

    /**
     * Processes a transfer suspension message received from the counter-party.
     */
    Result<Map<String, Object>, Map<String, Object>> handleSuspension(Map<String, Object> suspensionMessage);

    /**
     * Processes a transfer request received from the counter-party.
     */
    Result<Map<String, Object>, Map<String, Object>> handleStart(Map<String, Object> startMessage, Predicate<TransferProcess.DataAddress> dataAddressPredicate);

    /**
     * Processes a transfer request received from the counter-party.
     */
    default Result<Map<String, Object>, Map<String, Object>> handleStart(Map<String, Object> start) {
        return handleStart(start, dataAddress -> true);
    }
}
