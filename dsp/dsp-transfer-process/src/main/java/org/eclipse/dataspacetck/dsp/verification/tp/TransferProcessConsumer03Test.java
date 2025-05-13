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
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.SUSPENDED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;

@Tag("base-compliance")
@DisplayName("TP_C_03: Transfer request consumer negative scenarios")
public class TransferProcessConsumer03Test extends AbstractTransferProcessConsumerTest {

    @MandatoryTest
    @DisplayName("TP_C:03-01: Verify transfer request, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferProcess
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_c_03_01() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .sendCompletion(true)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:03-02: Verify transfer request, provider suspended")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferProcess
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_c_03_02() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .sendSuspension(true)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:03-03: Verify transfer request, provider started, provider suspended, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferProcess
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_c_03_03() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendSuspension()
                .thenWaitForState(SUSPENDED)
                .thenVerifyConsumerState(SUSPENDED)
                .sendCompletion(true)
                .thenWaitForState(SUSPENDED)
                .thenVerifyConsumerState(SUSPENDED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:03-04: Verify transfer request, provider started, provider terminated, provider started")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferProcess
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_c_03_04() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .sendStarted(true)
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:03-05: Verify transfer request, provider started, provider terminated, provider suspended")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferProcess
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferSuspensionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_c_03_05() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .sendSuspension(true)
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

    @MandatoryTest
    @DisplayName("TP_C:03-06: Verify transfer request, provider started, provider terminated, provider completed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start transfer
            
            CUT->>TCK: TransferRequestMessage
            TCK-->>CUT: TransferProcess
            
            TCK->>CUT: TransferStartMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferTerminationMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: TransferCompletionMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void tp_c_03_06() {

        transferProcessMock.recordInitializedAction(ConsumerActions::postTransferRequest);

        transferProcessPipeline
                .expectTransferRequest((request, counterPartyId) -> providerConnector.getProviderTransferProcessManager().handleTransferRequest(request, counterPartyId))
                .initiateTransferRequest(agreementId, format)
                .thenWaitForState(REQUESTED)
                .thenVerifyConsumerState(REQUESTED)
                .sendStarted(dataAddress())
                .thenWaitForState(STARTED)
                .thenVerifyConsumerState(STARTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .sendCompletion(true)
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }

}
