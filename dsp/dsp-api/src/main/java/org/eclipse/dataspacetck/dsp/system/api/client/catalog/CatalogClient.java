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

package org.eclipse.dataspacetck.dsp.system.api.client.catalog;

import java.util.Map;

/**
 * CatalogClient is an interface that defines methods for interacting with a catalog service.
 * It provides methods to retrieve the catalog and specific datasets.
 */
public interface CatalogClient {

    /**
     * Retrieves the catalog.
     *
     * @param message a map containing parameters for the request
     * @return a map representing the catalog
     */
    Map<String, Object> getCatalog(Map<String, Object> message);

    /**
     * Retrieves a specific dataset by its ID.
     *
     * @param datasetId   the ID of the dataset to retrieve
     * @param expectError whether to expect an error in the response
     * @return a map representing the dataset
     */
    Map<String, Object> getDataset(String datasetId, boolean expectError);
}
