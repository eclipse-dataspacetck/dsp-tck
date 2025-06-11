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

package org.eclipse.dataspacetck.dsp.system.client.metadata.local;

import org.eclipse.dataspacetck.dsp.system.api.client.metadata.MetadataClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;

import java.util.Map;

public class LocalMetadataClient implements MetadataClient {
    private final Connector connector;

    public LocalMetadataClient(Connector connector) {
        this.connector = connector;
    }

    @Override
    public Map<String, Object> getMetadata() {
        return connector.getMetadata();
    }
}
