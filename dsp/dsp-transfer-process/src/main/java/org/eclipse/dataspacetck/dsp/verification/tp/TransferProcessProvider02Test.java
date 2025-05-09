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
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.pause;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.postComplete;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.postStartTransfer;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.postSuspension;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.postTerminate;

@Tag("base-compliance")
@DisplayName("TP_02: Transfer request scenarios")
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
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-02: Verify transfer request, provider started, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferCompletionMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_02_02() {

        transferProcessMock.recordTransferRequestedAction(transferProcess -> {
            postStartTransfer(transferProcess);
            pause();
            postComplete(transferProcess);
        });

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .expectCompletionMessage(terminated -> consumerConnector.getConsumerTransferProcessManager().handleCompletion(terminated))
                .thenWaitForState(COMPLETED)
                .thenVerifyProviderState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-03: Verify transfer request, provider started, provider suspended, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferSuspensionMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_02_03() {

        transferProcessMock.recordTransferRequestedAction(transferProcess -> {
            postStartTransfer(transferProcess);
            pause();
            postSuspension(transferProcess);
            pause();
            postTerminate(transferProcess);
        });

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .expectSuspensionMessage(terminated -> consumerConnector.getConsumerTransferProcessManager().handleSuspension(terminated))
                .thenWaitForState(SUSPENDED)
                .expectTerminationMessage(terminated -> consumerConnector.getConsumerTransferProcessManager().handleTermination(terminated))
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-04: Verify transfer request, provider started, provider suspended, provider started, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferSuspensionMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferStartedMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferCompletionMessage
            TCK-->>CUT: 200 OK
            
            """)
    public void tp_02_04() {

        transferProcessMock.recordTransferRequestedAction(transferProcess -> {
            postStartTransfer(transferProcess);
            pause();
            postSuspension(transferProcess);
            pause();
            postStartTransfer(transferProcess);
            pause();
            postComplete(transferProcess);
        });

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .expectSuspensionMessage(terminated -> consumerConnector.getConsumerTransferProcessManager().handleSuspension(terminated))
                .thenWaitForState(SUSPENDED)
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .thenWaitForState(STARTED)
                .expectCompletionMessage(terminated -> consumerConnector.getConsumerTransferProcessManager().handleCompletion(terminated))
                .thenWaitForState(COMPLETED)
                .thenVerifyProviderState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP:02-05: Verify transfer request, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferTerminatedMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_02_05() {

        transferProcessMock.recordTransferRequestedAction(ProviderActions::postTerminate);

        transferProcessPipeline
                .expectTerminationMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleTermination(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

}
