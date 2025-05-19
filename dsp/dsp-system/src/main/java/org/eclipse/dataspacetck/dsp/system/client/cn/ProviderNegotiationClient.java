/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.system.client.cn;

import java.util.Map;

/**
 * Proxy to the provider connector being verified for contract negotiation.
 */
public interface ProviderNegotiationClient extends NegotiationClient {

    /**
     * Sends the contract request to the provider. Used for initial requests.
     */
    Map<String, Object> contractRequest(Map<String, Object> message, String counterPartyId, boolean expectError);

    /**
     * Sends a subsequent contract request to the provider. Used client counter-offers.
     */
    void contractOfferRequest(Map<String, Object> message, String counterPartyId, boolean expectError);

    /**
     * Sends the accepted event to the provider connector.
     */
    void accept(Map<String, Object> event);

    /**
     * Sends the verified event to the provider connector.
     */
    void verify(Map<String, Object> event, boolean expectError);

    /**
     * Retrieves the negotiation from the provider.
     */
    Map<String, Object> getNegotiation(String processId);

}
