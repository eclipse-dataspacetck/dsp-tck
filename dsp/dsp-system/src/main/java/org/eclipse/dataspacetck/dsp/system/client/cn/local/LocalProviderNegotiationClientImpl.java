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

package org.eclipse.dataspacetck.dsp.system.client.cn.local;

import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.client.cn.ProviderNegotiationClient;

import java.util.Map;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.message.NegotiationFunctions.createNegotiationResponse;

/**
 * Implementation of a {@link ProviderNegotiationClient} that supports dispatch to a local, in-memory test connector.
 */
public class LocalProviderNegotiationClientImpl extends AbstractLocalNegotiationClient implements ProviderNegotiationClient {
    private final Connector systemConnector;

    public LocalProviderNegotiationClientImpl(Connector systemConnector) {
        this.systemConnector = systemConnector;
    }

    @Override
    public Map<String, Object> contractRequest(Map<String, Object> contractRequest, String counterPartyId, boolean expectError) {
        return execute("contractRequest", contractRequest, expectError, (compacted) -> {
            var negotiation = systemConnector.getProviderNegotiationManager().handleContractRequest(compacted, counterPartyId);
            return processJsonLd(negotiation);
        });
    }

    @Override
    public void accept(Map<String, Object> event) {
        var compacted = processJsonLd(event);
        systemConnector.getProviderNegotiationManager().handleAccepted(compacted);
    }

    @Override
    public void verify(Map<String, Object> event, boolean expectError) {
        execute("verify", event, expectError, (compacted) -> {
            systemConnector.getProviderNegotiationManager().handleVerified(compacted);
            return null;
        });
    }

    @Override
    public void terminate(String counterPartyId, Map<String, Object> termination, String callbackAddress, boolean expectError) {
        execute("termination", termination, expectError, (compacted) -> {
            systemConnector.getProviderNegotiationManager().terminated(compacted);
            return null;
        });
    }

    @Override
    public Map<String, Object> getNegotiation(String providerPid) {
        var negotiation = systemConnector.getProviderNegotiationManager().findById(providerPid);
        var consumerPid = negotiation.getCorrelationId();
        return processJsonLd(createNegotiationResponse(providerPid, consumerPid, negotiation.getState().toString()));
    }

}
