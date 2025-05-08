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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Receives protocol-level messages.
 */
public interface ProtocolHandler {

    HandlerResponse apply(Map<String, List<String>> headers, InputStream body);

    default HandlerResponse apply(String path, Map<String, List<String>> headers, InputStream body) {
        return apply(headers, body);
    }

}
