/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
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

package org.eclipse.dataspacetck.dsp.system.connector.tp;

import org.eclipse.dataspacetck.core.system.ConsoleMonitor;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer.expandAndDeserialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;

class ConsumerTransferProcessManagerImplTest {

    private final ConsumerTransferProcessManagerImpl manager = new ConsumerTransferProcessManagerImpl(new ConsoleMonitor(false, false));

    @Test
    void handleStart_shouldStartTransfer_whenRequestResponseNotYetProcessed() {
        var transfer = manager.createTransferProcess("agreement-id", "HttpData-PULL", "http://provider", null);

        var result = manager.handleStart(expandAndDeserialize(createStartRequest("provider-pid", transfer.getId(), null)));

        assertThat(result.succeeded()).isTrue();
        assertThat(transfer.getState()).isEqualTo(STARTED);
        assertThat(transfer.getCorrelationId()).isEqualTo("provider-pid");

        // the late transfer request response with the same provider pid is a no-op
        manager.transferRequested(transfer.getId(), "provider-pid");
        assertThat(transfer.getState()).isEqualTo(STARTED);
    }

    @Test
    void transferRequested_shouldFail_whenProviderPidDiffersFromEarlyStartMessage() {
        var transfer = manager.createTransferProcess("agreement-id", "HttpData-PULL", "http://provider", null);
        manager.handleStart(expandAndDeserialize(createStartRequest("provider-pid", transfer.getId(), null)));

        assertThatThrownBy(() -> manager.transferRequested(transfer.getId(), "other-provider-pid"))
                .isInstanceOf(IllegalStateException.class);
    }
}
