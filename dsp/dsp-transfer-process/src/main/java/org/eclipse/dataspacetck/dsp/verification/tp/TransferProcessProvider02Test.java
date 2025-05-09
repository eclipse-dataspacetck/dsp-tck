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

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.COMPLETED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("TP_02: Transfer request provider scenarios")
public class TransferProcessProvider02Test extends AbstractTransferProcessProviderTest {

    @MandatoryTest
    @DisplayName("TP:02-01: Verify transfer request, provider started, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_02_01() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-02: Verify transfer request, provider started, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_02_02() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendCompletion()
                .thenWaitForState(COMPLETED)
                .thenVerifyProviderState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-03: Verify transfer request, provider started, consumer suspended, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_02_03() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendSuspension()
                .thenWaitForState(SUSPENDED)
                .thenVerifyProviderState(SUSPENDED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-04: Verify transfer request, provider started, consumer suspended, consumer started, consumer completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferStartedMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 200 OK
            
            """)
    public void tp_02_04() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postStartTransfer);

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendSuspension()
                .thenWaitForState(SUSPENDED)
                .thenPause()
                .thenVerifyProviderState(SUSPENDED)
                .sendStarted()
                .thenWaitForState(STARTED)
                .thenVerifyProviderState(STARTED)
                .sendCompletion()
                .thenWaitForState(COMPLETED)
                .thenVerifyProviderState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-05: Verify transfer request, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            TCK->>CUT: TransferTerminatedMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_02_05() {

        transferProcessPipeline
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyProviderState(REQUESTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

}
