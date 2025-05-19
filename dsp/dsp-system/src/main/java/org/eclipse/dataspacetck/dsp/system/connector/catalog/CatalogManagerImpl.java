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

package org.eclipse.dataspacetck.dsp.system.connector.catalog;

import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.Catalog;
import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.CatalogManager;
import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.Dataset;

import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TCK_PARTICIPANT_ID;

public class CatalogManagerImpl implements CatalogManager {
    private final Monitor monitor;
    private final Catalog catalog = new Catalog(TCK_PARTICIPANT_ID);

    public CatalogManagerImpl(Monitor monitor) {
        this.monitor = monitor;
    }

    @Override
    public void addDataset(Dataset dataset) {
        catalog.addDataset(dataset);
    }

    @Override
    public Catalog getCatalog() {
        return catalog;
    }

    @Override
    public Dataset getDataset(String datasetId) {
        return catalog.getDatasets().get(datasetId);
    }
}
