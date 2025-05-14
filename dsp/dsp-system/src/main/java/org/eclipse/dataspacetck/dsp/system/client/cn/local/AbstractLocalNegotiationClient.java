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

import java.util.Map;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.processJsonLd;

/**
 * Base negotiation local client functionality.
 */
public abstract class AbstractLocalNegotiationClient {


    protected <T> T execute(String operation, Map<String, Object> payload, boolean expectError, Function<Map<String, Object>, T> work) {
        try {
            var compacted = processJsonLd(payload);
            var result = work.apply(compacted);
            if (expectError) {
                throw new AssertionError("Expected to throw an error on %s".formatted(operation));
            }
            return result;
        } catch (IllegalStateException e) {
            // if the error is expected, swallow exception
            if (!expectError) {
                throw e;
            }
            return null;
        }
    }
}
