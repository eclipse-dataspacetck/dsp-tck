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

/**
 * Constructs a transfer process with a connector under test.
 */
public interface TransferProcessPipeline<P extends TransferProcessPipeline<P>> extends AsyncPipeline<P> {

    /**
     * Waits for the transition to the given state.
     */
    P thenWaitForState(State state);


}
