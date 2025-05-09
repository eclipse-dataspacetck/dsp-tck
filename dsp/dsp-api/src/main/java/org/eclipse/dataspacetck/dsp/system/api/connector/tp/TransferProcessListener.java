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

import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

/**
 * A listener interface for receiving notifications about transfer process events.
 */
public interface TransferProcessListener {

    /**
     * Invoked when a transfer process has been initialized.
     */
    default void transferInitialized(TransferProcess transferProcess) {
    }

    /**
     * Invoked when a transfer process has been started.
     */
    default void started(TransferProcess transferProcess) {
    }

    /**
     * Invoked when a transfer process has been requested.
     */
    default void requested(TransferProcess transferProcess) {

    }

    /**
     * Invoked when a transfer process has been terminated.
     */
    default void terminated(TransferProcess transferProcess) {

    }

    /**
     * Invoked when a transfer process has been completed.
     */
    default void completed(TransferProcess transferProcess) {

    }

    /**
     * Invoked when a transfer process has been suspended.
     */
    default void suspended(TransferProcess transferProcess) {
        
    }
}
