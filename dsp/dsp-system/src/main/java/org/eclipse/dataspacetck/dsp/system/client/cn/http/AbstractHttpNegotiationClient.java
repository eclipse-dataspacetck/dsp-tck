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

package org.eclipse.dataspacetck.dsp.system.client.cn.http;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.client.cn.NegotiationClient;

import java.util.Map;

import static java.lang.String.format;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;

/**
 * Base negotiation client functionality.
 */
public abstract class AbstractHttpNegotiationClient implements NegotiationClient {
    private static final String TERMINATE_PATH = "%s/negotiations/%s/termination";
    private final Monitor monitor;

    protected AbstractHttpNegotiationClient(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void terminate(String counterPartyId, Map<String, Object> termination, String callbackAddress, boolean expectError) {
        try (var response = postJson(format(TERMINATE_PATH, callbackAddress, counterPartyId), termination, expectError)) {
            monitor.debug("Received negotiation terminate response: " + counterPartyId);
        }
    }
}
