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

package org.eclipse.dataspacetck.dsp.system.api.statemachine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.COMPLETED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransferProcessTest {

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"REQUESTED"})
    void verifyValidInitialStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString()).build();
        if (REQUESTED == state) {
            transferProcess.setCorrelationId(randomUUID().toString());
        }
        transferProcess.transition(state);
        assertEquals(state, transferProcess.getState());
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"REQUESTED", "STARTED", "TERMINATED"})
    void verifyInvalidValidCorrelationIdTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString()).build();
        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"INITIALIZED", "COMPLETED", "SUSPENDED", "TERMINATED"})
    void verifyInValidInitialStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString()).build();
        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }


    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"STARTED", "TERMINATED"})
    void verifyValidRequestedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(REQUESTED).build();

        transferProcess.transition(state);
        assertEquals(state, transferProcess.getState());
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"INITIALIZED", "REQUESTED", "SUSPENDED", "COMPLETED"})
    void verifyInValidRequestedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(REQUESTED).build();

        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"SUSPENDED", "TERMINATED", "COMPLETED"})
    void verifyValidStartedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(STARTED).build();

        transferProcess.transition(state);
        assertEquals(state, transferProcess.getState());
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"INITIALIZED", "REQUESTED"})
    void verifyInValidStartedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(STARTED).build();

        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"STARTED", "TERMINATED"})
    void verifyValidSuspendedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(SUSPENDED).build();

        transferProcess.transition(state);
        assertEquals(state, transferProcess.getState());
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"INITIALIZED", "REQUESTED", "COMPLETED"})
    void verifyInValidSuspendedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(SUSPENDED).build();

        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"INITIALIZED", "REQUESTED", "COMPLETED", "SUSPENDED", "STARTED", "TERMINATED"})
    void verifyInValidCompletedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(COMPLETED).build();

        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }

    @ParameterizedTest
    @EnumSource(value = TransferProcess.State.class, names = {"INITIALIZED", "REQUESTED", "COMPLETED", "SUSPENDED", "STARTED", "TERMINATED"})
    void verifyInValidTerminatedStateTransitions(TransferProcess.State state) {
        var transferProcess = TransferProcess.Builder.newInstance().agreementId(randomUUID().toString())
                .correlationId(randomUUID().toString()).state(TERMINATED).build();

        assertThrows(IllegalStateException.class, () -> transferProcess.transition(state));
    }
}
