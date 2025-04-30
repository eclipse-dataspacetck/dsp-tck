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

import org.eclipse.dataspacetck.core.api.verification.AbstractVerificationTest;
import org.junit.jupiter.api.BeforeAll;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.registerValidator;

public class AbstractContractNegotiationTest extends AbstractVerificationTest {


    @BeforeAll
    static void setUp() {
        registerValidator("ContractRequestMessage", forSchema("/negotiation/contract-request-message-schema.json"));
        registerValidator("ContractOfferMessage", forSchema("/negotiation/contract-offer-message-schema.json"));
        registerValidator("ContractAgreementMessage", forSchema("/negotiation/contract-agreement-message-schema.json"));
        registerValidator("ContractAgreementVerificationMessage", forSchema("/negotiation/contract-agreement-verification-message-schema.json"));
        registerValidator("ContractNegotiationEventMessage", forSchema("/negotiation/contract-negotiation-event-message-schema.json"));
        registerValidator("ContractNegotiationTerminationMessage", forSchema("/negotiation/contract-negotiation-termination-message-schema.json"));
        registerValidator("ContractNegotiation", forSchema("/negotiation/contract-negotiation-schema.json"));
        registerValidator("ContractNegotiationError", forSchema("/negotiation/contract-negotiation-error-schema.json"));
    }
}
