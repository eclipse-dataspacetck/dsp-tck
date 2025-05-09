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

import java.util.Map;

/**
 * This interface is used by the provider connector to manage transfer processes.
 * It extends the TransferProcessManager interface to include additional methods
 * specific to the provider's role in the transfer process.
 */
public interface ProviderTransferProcessManager extends TransferProcessManager {

    /**
     * Handles a transfer request received from the consumer.
     */
    Map<String, Object> handleTransferRequest(Map<String, Object> transferRequest, String counterPartyId);
    
}
