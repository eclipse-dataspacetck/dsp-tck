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

package org.eclipse.dataspacetck.dsp.system.client.tp;

import java.util.Map;

/**
 * Proxy to the provider connector being verified for transfer process.
 */
public interface ProviderTransferProcessClient {

    /**
     * Sends a transfer request to the provider connector.
     */
    Map<String, Object> transferRequest(Map<String, Object> message, String counterPartyId, boolean expectError);


    /**
     * Retrieves the transfer process from the consumer connector.
     */
    Map<String, Object> getTransferProcess(String providerId);


    /**
     * Sends a transfer start message to the consumer connector.
     *
     * @param consumerId         the ID of the consumer
     * @param terminationMessage the transfer termination message
     * @param callbackAddress    the callback address
     * @param expectError        whether to expect an error
     */
    void terminateTransfer(String consumerId, Map<String, Object> terminationMessage, String callbackAddress, boolean expectError);
}
