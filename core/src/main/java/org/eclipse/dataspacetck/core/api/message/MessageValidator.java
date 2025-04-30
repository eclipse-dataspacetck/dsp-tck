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

package org.eclipse.dataspacetck.core.api.message;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;

/**
 * Interface for validating messages.
 * <p>
 * Implementations should provide validation logic for specific message types.
 */
public interface MessageValidator {
    List<String> validate(JsonNode message);
}
