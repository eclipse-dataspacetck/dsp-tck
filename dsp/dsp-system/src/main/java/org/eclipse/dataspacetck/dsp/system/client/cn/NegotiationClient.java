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

package org.eclipse.dataspacetck.dsp.system.client.cn;

import java.util.Map;

public interface NegotiationClient {

    /**
     * Terminates the negotiation with the provider.
     */
    void terminate(String counterPartyId, Map<String, Object> termination, String callbackAddress, boolean expectError);
}
