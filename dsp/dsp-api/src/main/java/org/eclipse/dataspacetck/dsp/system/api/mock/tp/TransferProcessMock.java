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

import java.util.function.Consumer;


/**
 * Mock service for recording connector actions on transfer processes.
 */
public interface TransferProcessMock {

    /**
     * Verifies all actions have been executed.
     */
    void verify();

    /**
     * Returns true if all actions have been executed.
     */
    boolean completed();

    /**
     * Resets the mock and all recorded actions.
     */
    void reset();

    /**
     * An action to be executed by the mock.
     */
    interface Action extends Consumer<TransferProcess> {
    }

}
