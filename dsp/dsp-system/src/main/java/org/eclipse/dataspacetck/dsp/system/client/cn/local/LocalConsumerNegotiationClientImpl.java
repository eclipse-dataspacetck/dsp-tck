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
import org.eclipse.dataspacetck.dsp.system.client.cn.ConsumerNegotiationClient;

import java.util.Map;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;
import static org.eclipse.dataspacetck.dsp.system.api.message.NegotiationFunctions.createNegotiationResponse;

/**
 * Implementation of a {@link ConsumerNegotiationClient} that supports dispatch to a local, in-memory test connector.
 */
public class LocalConsumerNegotiationClientImpl extends AbstractLocalNegotiationClient implements ConsumerNegotiationClient {
    private final Connector systemConsumerConnector;

    public LocalConsumerNegotiationClientImpl(Connector consumerConnector) {
        this.systemConsumerConnector = consumerConnector;
    }

    @Override
    public void initiateRequest(String datasetId, String offerId) {
        systemConsumerConnector.getConsumerNegotiationManager().createNegotiation(datasetId, offerId, null);

    }

    @Override
    public void contractOffer(String consumerId, Map<String, Object> offer, String callbackAddress, boolean expectError) {
        execute("offer", offer, expectError, (compacted) -> {
            systemConsumerConnector.getConsumerNegotiationManager().handleOffer(compacted);
            return null;
        });
    }

    @Override
    public void contractAgreement(String consumerId, Map<String, Object> agreement, String callbackAddress, boolean expectError) {
        execute("agreement", agreement, expectError, (compacted) -> {
            systemConsumerConnector.getConsumerNegotiationManager().handleAgreement(compacted);
            return null;
        });
    }

    @Override
    public void finalize(String consumerId, Map<String, Object> event, String callbackAddress, boolean expectError) {
        execute("finalize", event, expectError, (compacted) -> {
            systemConsumerConnector.getConsumerNegotiationManager().handleFinalized(compacted);
            return null;
        });
    }

    @Override
    public Map<String, Object> getNegotiation(String consumerId, String callbackAddress) {
        var negotiation = systemConsumerConnector.getConsumerNegotiationManager().findById(consumerId);
        var consumerPid = negotiation.getCorrelationId();
        return processJsonLd(createNegotiationResponse(consumerId, consumerPid, negotiation.getState().toString()));
    }

    @Override
    public void terminate(String counterPartyId, Map<String, Object> termination, String callbackAddress, boolean expectError) {
        execute("termination", termination, expectError, (compacted) -> {
            systemConsumerConnector.getConsumerNegotiationManager().handleTermination(compacted);
            return null;
        });
    }
}
