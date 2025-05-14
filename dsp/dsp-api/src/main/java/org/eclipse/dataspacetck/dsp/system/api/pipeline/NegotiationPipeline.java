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

package org.eclipse.dataspacetck.dsp.system.api.pipeline;

import org.eclipse.dataspacetck.core.api.pipeline.AsyncPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State;

import java.util.Map;
import java.util.function.Function;

/**
 * Constructs a contract negotiation with a connector under test.
 */
public interface NegotiationPipeline<P extends NegotiationPipeline<P>> extends AsyncPipeline<P> {

    /**
     * Waits for the transition to the given state.
     */
    P thenWaitForState(State state);


    /**
     * Sends a termination message to the connector under test.
     */
    default P sendTermination() {
        return sendTermination(false);
    }

    /**
     * Sends a termination message to the connector under test.
     */
    P sendTermination(boolean expectError);

    /**
     * Expects a transfer termination message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    P expectTerminationMessage(Function<Map<String, Object>, Map<String, Object>> action);
}
