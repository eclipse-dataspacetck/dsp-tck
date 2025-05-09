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

import org.eclipse.dataspacetck.dsp.system.api.mock.tp.ConsumerTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.function.BiConsumer;

/**
 * A no-op mock used when the local connector is disabled.
 */
public class NoOpConsumerTransferProcessMock implements ConsumerTransferProcessMock {

    @Override
    public void verify() {
    }

    @Override
    public boolean completed() {
        return false;
    }

    @Override
    public void reset() {
    }

    @Override
    public void recordInitializedAction(BiConsumer<String, TransferProcess> action) {

    }

    @Override
    public void recordStartedAction(BiConsumer<String, TransferProcess> action) {

    }
}
