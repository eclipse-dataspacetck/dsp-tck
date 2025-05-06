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

import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.STARTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.pause;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.postStartTransfer;
import static org.eclipse.dataspacetck.dsp.verification.tp.ProviderActions.postTerminate;

@Tag("base-compliance")
@DisplayName("TP_01: Transfer request scenarios")
public class TransferProcessProvider01Test extends AbstractTransferProcessProviderTest {

    @MandatoryTest
    @DisplayName("TP:01-01: Verify transfer request, provider started, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: TransferRequestMessage
            CUT-->>TCK: TransferProcess
            
            CUT->>TCK: TransferStartMessage
            TCK-->>CUT: 200 OK
            
            CUT->>TCK: TransferTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void tp_01_01() {

        transferProcessMock.recordTransferRequestedAction(transferProcess -> {
            postStartTransfer(transferProcess);
            pause();
            postTerminate(transferProcess);
        });

        transferProcessPipeline
                .expectStartMessage(start -> consumerConnector.getConsumerTransferProcessManager().handleStart(start))
                .sendTransferRequest(agreementId, format)
                .thenWaitForState(STARTED)
                .expectTerminationMessage(terminated -> consumerConnector.getConsumerTransferProcessManager().handleTermination(terminated))
                .thenWaitForState(TERMINATED)
                .thenVerifyProviderState(TERMINATED)
                .execute();

        transferProcessMock.verify();
    }


}
