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

import static org.eclipse.dataspacetck.dsp.system.api.message.tp.TransferFunctions.dataAddress;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.COMPLETED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("TP_C_01: Transfer request scenarios")
public class TransferProcessConsumer01Test extends AbstractTransferProcessConsumerTest {


    @MandatoryTest
    @DisplayName("TP_C:01-01: Verify transfer request, provider started, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_c_01_01() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:01-02: Verify transfer request, provider started, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_c_01_02() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendCompletion()
                .thenWaitForState(COMPLETED)
                .thenVerifyConsumerState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:01-03: Verify transfer request, provider started, provider suspended, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_c_01_03() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendSuspension()
                .thenWaitForState(SUSPENDED)
                .thenVerifyConsumerState(SUSPENDED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:01-04: Verify transfer request, provider started, provider suspended, provider started, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferStartedMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_c_01_04() {
        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendSuspension()
                .thenWaitForState(SUSPENDED)
                .thenVerifyConsumerState(SUSPENDED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendCompletion()
                .thenWaitForState(COMPLETED)
                .thenVerifyConsumerState(COMPLETED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:01-05: Verify transfer request, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferRequest
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void tp_c_01_05() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }
}
