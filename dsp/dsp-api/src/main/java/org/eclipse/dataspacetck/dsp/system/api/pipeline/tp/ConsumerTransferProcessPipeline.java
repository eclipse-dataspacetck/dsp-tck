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

import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Constructs a transfer process with a consumer connector under test. Uses a TCK provider connector to interact
 * with the consumer connector being verified.
 */
public interface ConsumerTransferProcessPipeline extends TransferProcessPipeline<ConsumerTransferProcessPipeline> {

    /**
     * Initiates a transfer request with the given agreement ID and format.
     *
     * @param agreementId the ID of the agreement
     * @param format      the format of the transfer
     * @return this pipeline instance
     */
    ConsumerTransferProcessPipeline initiateTransferRequest(String agreementId, String format);

    /**
     * Expects a transfer request and executes the given action.
     */
    ConsumerTransferProcessPipeline expectTransferRequest(BiFunction<Map<String, Object>, String, Map<String, Object>> action);
    
    /**
     * Verifies the active consumer transfer process is in the given state.
     */
    ConsumerTransferProcessPipeline thenVerifyConsumerState(State state);

}
