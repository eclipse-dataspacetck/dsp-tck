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

import java.util.Map;
import java.util.function.Function;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.DataAddress;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;

/**
 * Constructs a transfer process with a provider connector under test. Uses a TCK consumer connector to interact
 * with the provider connector being verified.
 */
public interface ProviderTransferProcessPipeline extends TransferProcessPipeline<ProviderTransferProcessPipeline> {

    /**
     * Initiates a transfer request with the given agreement ID and format.
     *
     * @param agreementId the ID of the agreement
     * @param format      the format of the transfer
     * @return this pipeline instance
     */
    default ProviderTransferProcessPipeline sendTransferRequest(String agreementId, String format) {
        return sendTransferRequest(agreementId, format, null);
    }

    /**
     * Initiates a transfer request with the given agreement ID, format, and data address.
     *
     * @param agreementId the ID of the agreement
     * @param format      the format of the transfer
     * @param dataAddress the data address to send
     * @return this pipeline instance
     */
    ProviderTransferProcessPipeline sendTransferRequest(String agreementId, String format, DataAddress dataAddress);

    /**
     * Expects a transfer start message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    ProviderTransferProcessPipeline expectStartMessage(Function<Map<String, Object>, Map<String, Object>> action);

    /**
     * Expects a transfer termination message and executes the given action.
     *
     * @param action the action to execute
     * @return this pipeline instance
     */
    ProviderTransferProcessPipeline expectTerminationMessage(Function<Map<String, Object>, Map<String, Object>> action);

    /**
     * Verifies the active provider transfer process is in the given state.
     */
    ProviderTransferProcessPipeline thenVerifyProviderState(State state);

}
