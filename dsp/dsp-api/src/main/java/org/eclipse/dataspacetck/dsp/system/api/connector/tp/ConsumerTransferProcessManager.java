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

package org.eclipse.dataspacetck.dsp.system.api.connector.tp;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;

/**
 * The ConsumerTransferProcessManager is responsible for managing transfer processes on the consumer side.
 * It handles the creation and management of transfer processes, including the initiation of transfers and
 * the handling of transfer requests from providers.
 */
public interface ConsumerTransferProcessManager extends TransferProcessManager {

    /**
     * Creates a transfer process for the given agreement id and format.
     *
     * @param agreementId     the id of the agreement
     * @param format          the format of the data
     * @param providerBaseUrl the base URL of the provider
     * @param dataAddress     the data address of the data
     * @return the created transfer process
     */
    TransferProcess createTransferProcess(String agreementId, String format, String providerBaseUrl, TransferProcess.DataAddress dataAddress);

    /**
     * Called after a transfer has been requested and the transfer id is returned by the provider. The provider transfer process id
     * will be set as the correlation id on the consumer.
     */
    void transferRequested(String consumerId, String providerId);

}
