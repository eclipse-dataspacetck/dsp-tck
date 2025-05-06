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

package org.eclipse.dataspacetck.dsp.system.pipeline.tp;

import org.eclipse.dataspacetck.core.api.pipeline.AbstractAsyncPipeline;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.TransferProcessPipeline;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State;

public abstract class AbstractTransferProcessPipeline<P extends TransferProcessPipeline<P>> extends AbstractAsyncPipeline<P> {
    protected TransferProcess transferProcess;

    public AbstractTransferProcessPipeline(CallbackEndpoint endpoint, Monitor monitor, long waitTime) {
        super(endpoint, monitor, waitTime);
    }

    public P thenWaitForState(State state) {
        return thenWait("state to transition to " + state, () -> transferProcess != null && state == transferProcess.getState());
    }
}
