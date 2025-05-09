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
public interface TransferProcessClient {

    /**
     * Sends a transfer terminate message to the counter-party connector.
     *
     * @param counterPartyPid    the counter-party ID of the transfer process
     * @param terminationMessage the transfer termination message
     * @param callbackAddress    the callback address
     * @param expectError        whether to expect an error
     */
    void terminateTransfer(String counterPartyPid, Map<String, Object> terminationMessage, String callbackAddress, boolean expectError);

    /**
     * Sends a transfer complete message to the counter-party connector.
     *
     * @param counterPartyPid   the counter-party ID of the transfer process
     * @param completionMessage the transfer termination message
     * @param callbackAddress   the callback address
     * @param expectError       whether to expect an error
     */
    void completeTransfer(String counterPartyPid, Map<String, Object> completionMessage, String callbackAddress, boolean expectError);


    /**
     * Sends a transfer suspension message to the counter-party connector.
     *
     * @param counterPartyPid   the counter-party ID of the transfer process
     * @param suspensionMessage the transfer termination message
     * @param callbackAddress   the callback address
     * @param expectError       whether to expect an error
     */
    void suspendTransfer(String counterPartyPid, Map<String, Object> suspensionMessage, String callbackAddress, boolean expectError);


    /**
     * Sends a transfer start message to the counter-party connector.
     *
     * @param counterPartiPid the counter-party ID of the transfer process
     * @param startMessage    the transfer start message
     * @param callbackAddress the callback address
     * @param expectError     whether to expect an error
     */
    void startTransfer(String counterPartiPid, Map<String, Object> startMessage, String callbackAddress, boolean expectError);

    /**
     * Retrieves the transfer process from the counter-party connector.
     */
    Map<String, Object> getTransferProcess(String counterPartyPid, String callbackAddress);
}
