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

package org.eclipse.dataspacetck.dsp.verification.cn;

import org.eclipse.dataspacetck.api.system.MandatoryTest;
import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import static org.eclipse.dataspacetck.dsp.system.api.connector.IdGenerator.offerIdFromDatasetId;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.ACCEPTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.TERMINATED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.VERIFIED;
import static org.eclipse.dataspacetck.dsp.verification.cn.ConsumerActions.pause;
import static org.eclipse.dataspacetck.dsp.verification.cn.ConsumerActions.postRequest;
import static org.eclipse.dataspacetck.dsp.verification.cn.ConsumerActions.postTerminated;

@Tag("base-compliance")
@DisplayName("CN_C_02: Contract request consumer scenarios")
public class ContractNegotiationConsumer02Test extends AbstractContractNegotiationConsumerTest {

    @MandatoryTest
    @DisplayName("CN_C:02-01: Verify contract request, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractNegotiationTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void cn_c_02_01() {

        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:02-02: Verify contract request, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            CUT->>TCK: ContractNegotiationTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void cn_c_02_02() {

        negotiationMock.recordInitializedAction((url, cn) -> {
            postRequest(url, cn);
            pause();
            postTerminated(url, cn);
        });

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectTerminationMessage(msg -> providerConnector.getProviderNegotiationManager().handleTermination(msg))
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:02-03: Verify contract request, provider agreement, consumer terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractAgreementMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: ContractNegotiationTerminationMessage
            TCK-->>CUT: 200 OK
            """)
    public void cn_c_02_03() {

        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordAgreedAction(ConsumerActions::postTerminated);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectTerminationMessage(msg -> providerConnector.getProviderNegotiationManager().handleTermination(msg))
                .sendAgreementMessage()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:02-04: Verify contract request, offer received, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractOfferMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: ContractNegotiationTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void cn_c_02_04() {

        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .sendOfferMessage()
                .thenWaitForState(OFFERED)
                .thenVerifyConsumerState(OFFERED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:02-05: Verify contract request, offer received, consumer accepted, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractOfferMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: ContractNegotiationEventMessage:accepted
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractNegotiationTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void cn_c_02_05() {

        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordOfferedAction(ConsumerActions::postAccepted);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectAcceptedEvent(msg -> providerConnector.getProviderNegotiationManager().handleAccepted(msg))
                .sendOfferMessage()
                .thenWaitForState(ACCEPTED)
                .sendTermination()
                .thenWaitForState(TERMINATED)
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:02-06: Verify contract request, provider agreement, consumer verified, provider terminated")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractAgreementMessage
            CUT-->>TCK: 200 OK
            
            CUT->>TCK: ContractVerificationMessage
            TCK-->>CUT: 200 OK
            
            TCK->>CUT: ContractNegotiationTerminationMessage
            CUT-->>TCK: 200 OK
            """)
    public void cn_c_02_06() {

        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordAgreedAction(ConsumerActions::postVerification);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectVerifiedMessage(msg -> providerConnector.getProviderNegotiationManager().handleVerified(msg))
                .sendAgreementMessage()
                .thenWaitForState(VERIFIED)
                .sendTermination()
                .thenVerifyConsumerState(TERMINATED)
                .execute();

        negotiationMock.verify();
    }
}
