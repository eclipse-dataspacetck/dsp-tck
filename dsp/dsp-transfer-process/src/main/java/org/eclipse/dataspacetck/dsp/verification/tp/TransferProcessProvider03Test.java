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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("TP_03: Transfer request provider negative scenarios")
public class TransferProcessProvider03Test extends AbstractTransferProcessProviderTest {

    @MandatoryTest
    @DisplayName("TP:03-01: Verify transfer request, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 4xx ERROR
            
            """)
    public void tp_03_01() {

        transferProcessPipeline
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyProviderState(REQUESTED)
                .sendCompletion(true)
                .thenWaitForState(REQUESTED)
                .thenVerifyProviderState(REQUESTED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:03-02: Verify transfer request, consumer suspended")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 4xx ERROR
            
            """)
    public void tp_03_02() {

        transferProcessPipeline
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyProviderState(REQUESTED)
                .sendSuspension(true)
                .thenWaitForState(REQUESTED)
                .thenVerifyProviderState(REQUESTED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:03-03: Verify transfer request, provider started, consumer suspended, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_03_03() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendSuspension()
                .thenWaitForState(SUSPENDED)
                .thenVerifyProviderState(SUSPENDED)
                .sendCompletion(true)
                .thenWaitForState(SUSPENDED)
                .thenVerifyProviderState(SUSPENDED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:03-04: Verify transfer request, provider started, consumer terminated, consumer started")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_03_04() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .sendStarted(true)
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:03-05: Verify transfer request, provider started, consumer terminated, consumer suspended")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_03_05() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .sendSuspension(true)
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:03-06: Verify transfer request, provider started, consumer terminated, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_03_06() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .sendCompletion(true)
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }
}
