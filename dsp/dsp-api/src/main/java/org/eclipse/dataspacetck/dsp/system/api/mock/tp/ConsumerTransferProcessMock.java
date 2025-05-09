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

package org.eclipse.dataspacetck.dsp.system.api.mock.tp;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

import java.util.function.BiConsumer;

/**
 * Mock service for recording consumer connector actions.
 */
public interface ConsumerTransferProcessMock extends TransferProcessMock {

    /**
     * Records an action to be executed when a transfer process is initialized.
     *
     * @param action the action to record
     */
    void recordInitializedAction(BiConsumer<String, TransferProcess> action);

    /**
     * Records an action to be executed when a transfer process is started.
     *
     * @param action the action to record
     */
    void recordStartedAction(BiConsumer<String, TransferProcess> action);
}
