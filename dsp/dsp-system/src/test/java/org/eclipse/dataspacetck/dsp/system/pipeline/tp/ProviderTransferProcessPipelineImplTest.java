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

package org.eclipse.dataspacetck.dsp.system.pipeline.tp;

import org.eclipse.dataspacetck.core.system.ConsoleMonitor;
import org.eclipse.dataspacetck.core.system.DefaultCallbackEndpoint;
import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.eclipse.dataspacetck.dsp.system.client.tp.ProviderTransferProcessClient;
import org.eclipse.dataspacetck.dsp.system.connector.TckConnector;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.stringIdProperty;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer.expandAndDeserialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createStartRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.createTransferResponse;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;

class ProviderTransferProcessPipelineImplTest {

    private static final String PROVIDER_PID = "provider-pid";
    private static final long WAIT_SECONDS = 2;

    private final ConsoleMonitor monitor = new ConsoleMonitor(false, false);
    private final TckConnector consumerConnector = new TckConnector(monitor);
    private final DefaultCallbackEndpoint endpoint = DefaultCallbackEndpoint.Builder.newInstance().address("http://localhost:8083/tck").build();

    /**
     * The provider sends the start message right after accepting the transfer request, before the TCK has
     * processed the transfer request response (see eclipse-dataspacetck/dsp-tck#338).
     */
    @Test
    void shouldAcceptStartMessage_whenItArrivesBeforeTransferRequestResponse() {
        var startResponseCodes = new ArrayList<Integer>();
        var consumerPids = new ArrayList<String>();
        var client = new StubProviderClient() {
            @Override
            public Map<String, Object> transferRequest(Map<String, Object> message, String counterPartyId, boolean expectError) {
                var consumerPid = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, expandAndDeserialize(message));
                consumerPids.add(consumerPid);
                var start = serialize(createStartRequest(PROVIDER_PID, consumerPid, null));
                var response = endpoint.apply("/transfers/" + consumerPid + "/start", emptyMap(), new ByteArrayInputStream(start.getBytes(UTF_8)));
                startResponseCodes.add(response.code());
                return expandAndDeserialize(createTransferResponse(PROVIDER_PID, consumerPid, "REQUESTED"));
            }
        };

        var pipeline = new ProviderTransferProcessPipelineImpl(client, endpoint, consumerConnector, "http://provider", "provider", monitor, WAIT_SECONDS);
        pipeline.expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest("agreement-id", "HttpData-PULL")
                .thenWaitForState(STARTED)
                .execute();

        assertThat(startResponseCodes).containsExactly(200);
        var transfer = consumerTransfer(consumerPids.get(0));
        assertThat(transfer.getState()).isEqualTo(STARTED);
        assertThat(transfer.getCorrelationId()).isEqualTo(PROVIDER_PID);
    }

    @Test
    void shouldStart_whenStartMessageArrivesAfterTransferRequestResponse() {
        var consumerPids = new ArrayList<String>();
        var client = new StubProviderClient() {
            @Override
            public Map<String, Object> transferRequest(Map<String, Object> message, String counterPartyId, boolean expectError) {
                var consumerPid = stringIdProperty(DSPACE_PROPERTY_CONSUMER_PID_EXPANDED, expandAndDeserialize(message));
                consumerPids.add(consumerPid);
                return expandAndDeserialize(createTransferResponse(PROVIDER_PID, consumerPid, "REQUESTED"));
            }
        };

        var pipeline = new ProviderTransferProcessPipelineImpl(client, endpoint, consumerConnector, "http://provider", "provider", monitor, WAIT_SECONDS);
        pipeline.expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest("agreement-id", "HttpData-PULL")
                .execute();

        var start = serialize(createStartRequest(PROVIDER_PID, consumerPids.get(0), null));
        var response = endpoint.apply("/transfers/" + consumerPids.get(0) + "/start", emptyMap(), new ByteArrayInputStream(start.getBytes(UTF_8)));

        assertThat(response.code()).isEqualTo(200);
        assertThat(consumerTransfer(consumerPids.get(0)).getState()).isEqualTo(STARTED);
    }

    private TransferProcess consumerTransfer(String consumerPid) {
        return consumerConnector.getConsumerTransferProcessManager().findById(consumerPid);
    }

    private abstract static class StubProviderClient implements ProviderTransferProcessClient {

        @Override
        public void terminateTransfer(String counterPartyPid, Map<String, Object> terminationMessage, String callbackAddress, boolean expectError) {
        }

        @Override
        public void completeTransfer(String counterPartyPid, Map<String, Object> completionMessage, String callbackAddress, boolean expectError) {
        }

        @Override
        public void suspendTransfer(String counterPartyPid, Map<String, Object> suspensionMessage, String callbackAddress, boolean expectError) {
        }

        @Override
        public void startTransfer(String counterPartiPid, Map<String, Object> startMessage, String callbackAddress, boolean expectError) {
        }

        @Override
        public Map<String, Object> getTransferProcess(String counterPartyPid, String callbackAddress) {
            return Map.of();
        }
    }
}
