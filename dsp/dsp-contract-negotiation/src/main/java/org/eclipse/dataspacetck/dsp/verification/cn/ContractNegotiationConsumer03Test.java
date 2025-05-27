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
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.AGREED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.OFFERED;
import static org.eclipse.dataspacetck.dsp.system.api.statemachine.ContractNegotiation.State.REQUESTED;

@Tag("base-compliance")
@DisplayName("CN_C_03: Contract request consumer negative test scenarios")
public class ContractNegotiationConsumer03Test extends AbstractContractNegotiationConsumerTest {

    @MandatoryTest
    @DisplayName("CN_C:03-01: Verify contract request, invalid provider finalized")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractNegotiationEventMessage:finalized
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_c_03_01() {

        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .sendFinalizedEvent(true)
                .thenVerifyConsumerState(REQUESTED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:03-02: Verify contract request, offer received, invalid provider agreed")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractOfferMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: ContractAgreementMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_c_03_02() {
        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .sendOfferMessage()
                .thenWaitForState(OFFERED)
                .thenVerifyConsumerState(OFFERED)
                .sendAgreementMessage(true)
                .thenVerifyConsumerState(OFFERED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:03-03: Verify contract request, offer received, invalid provider finalized")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (provider)
            participant CUT as Connector Under Test (consumer)
            
            TCK->>CUT: Signal to start negotiation
            
            CUT->>TCK: ContractRequestMessage
            TCK-->>CUT: ContractNegotiation
            
            TCK->>CUT: ContractOfferMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: ContractNegotiationEventMessage:finalized
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_c_03_03() {
        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .sendOfferMessage()
                .thenWaitForState(OFFERED)
                .sendFinalizedEvent(true)
                .thenVerifyConsumerState(OFFERED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:03-04: Verify contract request, offer received, consumer accepted, invalid provider finalized")
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
            
            TCK->>CUT: ContractNegotiationEventMessage:finalized
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_c_03_04() {
        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordOfferedAction(ConsumerActions::postAccepted);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectAcceptedEvent(event -> providerConnector.getProviderNegotiationManager().handleAccepted(event))
                .sendOfferMessage()
                .thenWaitForState(ACCEPTED)
                .sendFinalizedEvent(true)
                .thenVerifyConsumerState(ACCEPTED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:03-05: Verify contract request, offer received, consumer accepted, invalid provider offer")
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
            
            TCK->>CUT: ContractOfferMessage
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_c_03_05() {
        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordOfferedAction(ConsumerActions::postAccepted);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectAcceptedEvent(event -> providerConnector.getProviderNegotiationManager().handleAccepted(event))
                .sendOfferMessage()
                .thenWaitForState(ACCEPTED)
                .sendOfferMessage(true)
                .thenVerifyConsumerState(ACCEPTED)
                .execute();

        negotiationMock.verify();
    }

    @MandatoryTest
    @DisplayName("CN_C:03-06: Verify contract request, offer received, consumer accepted, provider agreed, invalid provider finalized")
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
            
            TCK->>CUT: ContractAgreementMessage
            CUT-->>TCK: 200 OK
            
            TCK->>CUT: ContractNegotiationEventMessage:finalized
            CUT-->>TCK: 4xx ERROR
            """)
    public void cn_c_03_06() {
        negotiationMock.recordInitializedAction(ConsumerActions::postRequest);
        negotiationMock.recordOfferedAction(ConsumerActions::postAccepted);

        negotiationPipeline
                .expectInitialRequest((request, counterpartyId) -> providerConnector.getProviderNegotiationManager().handleContractRequest(request, counterpartyId))
                .initiateRequest(datasetId, offerIdFromDatasetId(datasetId))
                .thenWaitForState(REQUESTED)
                .expectAcceptedEvent(event -> providerConnector.getProviderNegotiationManager().handleAccepted(event))
                .sendOfferMessage()
                .thenWaitForState(ACCEPTED)
                .sendAgreementMessage()
                .thenWaitForState(AGREED)
                .sendFinalizedEvent(true)
                .thenVerifyConsumerState(AGREED)
                .execute();

        negotiationMock.verify();
    }

}
