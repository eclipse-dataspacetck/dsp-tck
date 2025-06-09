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

package org.eclipse.dataspacetck.dsp.verification.tp;

import org.eclipse.dataspacetck.api.system.MandatoryTest;
import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.dataAddress;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.COMPLETED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.verification.tp.ConsumerActions.postComplete;
import static org.eclipse.dataspacetck.dsp.verification.tp.ConsumerActions.postStartTransfer;
import static org.eclipse.dataspacetck.dsp.verification.tp.ConsumerActions.postSuspend;
import static org.eclipse.dataspacetck.dsp.verification.tp.ConsumerActions.postTerminate;
import static org.eclipse.dataspacetck.dsp.verification.tp.ConsumerActions.postTransferRequest;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.pause;

@Tag("base-compliance")
@DisplayName("TP_C_02: Transfer request consumer scenarios")
public class TransferProcessConsumer02Test extends AbstractTransferProcessConsumerTest {


    @MandatoryTest
    @DisplayName("TP_C:02-01: Verify transfer request, provider started, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: TransferTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_c_02_01() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);
        transferProcessMock.recordStartedAction(ConsumerActions::postTerminate);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .expectTerminationMessage(msg -> providerConnector.getProviderTransferProcessManager().handleTermination(msg))
                .sendStarted(dataAddress())
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:02-02: Verify transfer request, provider started, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: TransferCompletionMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_c_02_02() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);
        transferProcessMock.recordStartedAction(ConsumerActions::postComplete);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .expectCompletionMessage(msg -> providerConnector.getProviderTransferProcessManager().handleCompletion(msg))
                .sendStarted(dataAddress())
                .thenWaitForState(COMPLETED)
                .thenVerifyConsumerState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:02-03: Verify transfer request, provider started, consumer suspended, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: TransferSuspensionMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_c_02_03() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);
        transferProcessMock.recordStartedAction((url, tp) -> {
            postSuspend(url, tp);
            pause();
            postTerminate(url, tp);
        });

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .expectSuspensionMessage(msg -> providerConnector.getProviderTransferProcessManager().handleSuspension(msg))
                .sendStarted(dataAddress())
                .thenWaitForState(SUSPENDED)
                .expectTerminationMessage(msg -> providerConnector.getProviderTransferProcessManager().handleTermination(msg))
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @Disabled
    @MandatoryTest
    @DisplayName("TP_C:02-04: Verify transfer request, provider started, consumer suspended, consumer started, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: TransferSuspensionMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferCompletionMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_c_02_04() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);
        transferProcessMock.recordStartedAction((url, tp) -> {
            postSuspend(url, tp);
            pause();
            postStartTransfer(url, tp);
            pause();
            postComplete(url, tp);
        });

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .expectSuspensionMessage(msg -> providerConnector.getProviderTransferProcessManager().handleSuspension(msg))
                .sendStarted()
                .thenWaitForState(SUSPENDED)
                .expectStartMessage(msg -> providerConnector.getProviderTransferProcessManager().handleStart(msg))
                .thenWaitForState(STARTED)
                .expectCompletionMessage(msg -> providerConnector.getProviderTransferProcessManager().handleCompletion(msg))
                .thenWaitForState(COMPLETED)
                .thenVerifyConsumerState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:02-05: Verify transfer request, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            CUT->>TCK: TransferTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_c_02_05() {

        transferProcessMock.recordInitializedAction((url, tp) -> {
            postTransferRequest(url, tp);
            pause();
            postTerminate(url, tp);
        });

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .expectTerminationMessage(msg -> providerConnector.getProviderTransferProcessManager().handleTermination(msg))
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }
}
