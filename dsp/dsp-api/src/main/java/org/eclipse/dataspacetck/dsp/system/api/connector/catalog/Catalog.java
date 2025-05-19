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

package org.eclipse.dataspacetck.dsp.system.api.connector.catalog;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a catalog of datasets for a specific participant.
 * <p>
 * This class is used to manage datasets and their associated offers and distributions.
 * </p>
 */
public class Catalog {

    private final String participantId;
    private final Map<String, Dataset> datasets = new HashMap<>();

    public Catalog(String participantId) {
        this.participantId = participantId;
    }

    public String getParticipantId() {
        return participantId;
    }

    public Map<String, Dataset> getDatasets() {
        return datasets;
    }

    public void addDataset(Dataset dataset) {
        datasets.put(dataset.getId(), dataset);
    }

}
