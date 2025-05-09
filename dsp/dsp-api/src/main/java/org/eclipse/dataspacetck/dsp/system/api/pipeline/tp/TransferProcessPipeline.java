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

package org.eclipse.dataspacetck.dsp.system.api.pipeline.tp;

import org.eclipse.dataspacetck.core.api.pipeline.AsyncPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;

import java.util.Map;
import java.util.function.Function;

/**
 * Constructs a transfer process with a connector under test.
 */
public interface TransferProcessPipeline<P extends TransferProcessPipeline<P>> extends AsyncPipeline<P> {

    /**
     * Waits for the transition to the given state.
     */
    P thenWaitForState(State state);

    P thenPause();

    /**
     * Sends a termination event to the counter-party connector being verified.
     * <p>
     * * @return this pipeline instance
     */
    default P sendTermination() {
        return sendTermination(false);
    }

    /**
     * Sends a termination event to the counter-party connector being verified.
     *
     * @param expectError whether to expect an error
     * @return this pipeline instance
     */
    P sendTermination(boolean expectError);


    /**
     * Sends a completion event to the counter-party connector being verified.
     *
     * @return this pipeline instance
     */
    P sendCompletion(boolean expectError);

    /**
     * Sends a completion event to the counter-party connector being verified.
     *
     * @return this pipeline instance
     */
    default P sendCompletion() {
        return sendCompletion(false);
    }

    /**
     * Sends a suspension event to the counter-party connector being verified.
     *
     * @param expectError whether to expect an error
     * @return this pipeline instance
     */
    P sendSuspension(boolean expectError);

    /**
     * Sends a suspension event to the counter-party connector being verified.
     *
     * @return this pipeline instance
     */
    default P sendSuspension() {
        return sendSuspension(false);
    }

    /**
     * Sends a transfer request to the counter-party connector being verified.
     *
     * @return this pipeline instance
     */
    default P sendStarted() {
        return sendStarted(null);
    }

    /**
     * Sends a started event to the counter-party connector being verified.
     *
     * @param dataAddress the data address to send
     * @return this pipeline instance
     */
    P sendStarted(Map<String, Object> dataAddress);


    /**
     * Expects a transfer start message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    P expectStartMessage(Function<Map<String, Object>, Map<String, Object>> action);

    /**
     * Expects a transfer termination message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    P expectTerminationMessage(Function<Map<String, Object>, Map<String, Object>> action);

    /**
     * Expects a transfer completion message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    P expectCompletionMessage(Function<Map<String, Object>, Map<String, Object>> action);

    /**
     * Expects a transfer suspension message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    P expectSuspensionMessage(Function<Map<String, Object>, Map<String, Object>> action);
}
