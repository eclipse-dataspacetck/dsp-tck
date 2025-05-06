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

import java.util.Arrays;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.UUID.randomUUID;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.COMPLETED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;


/**
 * The transfer process entity.
 * <p>
 * This implementation is thread-safe.
 */
public class TransferProcess {

    private final LockManager lockManager = new LockManager();
    private State state = State.INITIALIZED;
    private DataAddress dataAddress;
    private String id;
    private String correlationId;
    private String agreementId;
    private String format;
    private String callbackAddress;

    public String getId() {
        return id;
    }

    public DataAddress getDataAddress() {
        return dataAddress;
    }

    public void setDataAddress(DataAddress dataAddress) {
        this.dataAddress = dataAddress;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public String getFormat() {
        return format;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public State getState() {
        return state;
    }

    public String getCallbackAddress() {
        return callbackAddress;
    }

    /**
     * Transitions to the new state and executes the work while holding a write-lock.
     */
    public void transition(State newState) {
        transition(newState, (tp) -> {
        });
    }

    /**
     * Transitions to the new state and executes the work while holding a write-lock.
     *
     * @param newState the new state
     * @param work     the work to execute
     */
    public void transition(State newState, Consumer<TransferProcess> work) throws IllegalStateException {
        lockManager.writeLock(() -> {
            switch (state) {
                case INITIALIZED -> {
                    assertStates(newState, REQUESTED);
                    verifyCorrelationId(newState);
                    state = newState;
                }
                case REQUESTED, SUSPENDED -> {
                    assertStates(newState, STARTED, TERMINATED);
                    state = newState;
                }
                case STARTED -> {
                    assertStates(newState, SUSPENDED, TERMINATED, TERMINATED, COMPLETED);
                    verifyCorrelationId(newState);
                    state = newState;
                }
                case COMPLETED -> throw new IllegalStateException(COMPLETED + " is a final state");
                case TERMINATED -> throw new IllegalStateException(TERMINATED + " is a final state");
                default -> throw new IllegalStateException("Unexpected value: " + state);
            }
            work.accept(this);
            return null;
        });
    }

    private void verifyCorrelationId(State newState) {
        if (newState == REQUESTED || newState == STARTED) {
            if (correlationId == null) {
                throw new IllegalStateException("Correlation id not set");
            }
        }
    }

    private void assertStates(State toState, State... states) {
        for (var state : states) {
            if (toState == state) {
                return;
            }
        }
        var legalStates = Arrays.stream(states).map(Enum::toString).collect(Collectors.joining(", "));
        throw new IllegalStateException(format("Illegal state transition from %s to %s. To state must be one of %s.", this.state, toState, legalStates));
    }

    public enum State {
        INITIALIZED,
        REQUESTED,
        STARTED,
        COMPLETED,
        SUSPENDED,
        TERMINATED
    }

    public record DataAddress(String endpointType, String endpoint, Map<String, String> endpointProperties) {

    }

    public static class Builder {
        private final TransferProcess process;

        private Builder() {
            this.process = new TransferProcess();
        }

        public static Builder newInstance() {
            return new Builder();
        }

        public Builder id(String id) {
            process.id = id;
            return this;
        }

        public Builder correlationId(String correlationId) {
            process.correlationId = correlationId;
            return this;
        }

        public Builder agreementId(String agreementId) {
            process.agreementId = agreementId;
            return this;
        }

        public Builder format(String format) {
            process.format = format;
            return this;
        }

        public Builder state(State state) {
            process.state = state;
            return this;
        }

        public Builder dataAddress(DataAddress dataAddress) {
            process.dataAddress = dataAddress;
            return this;
        }

        public Builder callbackAddress(String callbackAddress) {
            process.callbackAddress = callbackAddress;
            return this;
        }

        public TransferProcess build() {
            process.id = randomUUID().toString();
            return process;
        }
    }
}
