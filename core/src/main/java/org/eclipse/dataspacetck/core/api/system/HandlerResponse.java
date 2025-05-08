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

package org.eclipse.dataspacetck.core.api.system;

import java.util.Map;

/**
 * A handler response.
 */
public record HandlerResponse(int code, String result, Map<String, String> headers) {
    public HandlerResponse(int code, String result) {
        this(code, result, Map.of());
    }
}
