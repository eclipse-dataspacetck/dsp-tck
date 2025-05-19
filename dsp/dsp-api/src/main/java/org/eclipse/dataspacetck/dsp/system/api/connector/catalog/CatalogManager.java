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

/**
 * CatalogManager is responsible for managing datasets and their associated offers and distributions.
 * <p>
 * This interface provides methods to add datasets, retrieve the catalog, and get a specific dataset by its ID.
 * </p>
 */
public interface CatalogManager {

    /**
     * Adds a dataset to the catalog.
     *
     * @param dataset the dataset to add
     */
    void addDataset(Dataset dataset);

    /**
     * Retrieves the catalog.
     *
     * @return the catalog
     */
    Catalog getCatalog();

    /**
     * Retrieves a specific dataset by its ID.
     *
     * @param datasetId the ID of the dataset to retrieve
     * @return the dataset with the specified ID
     */
    Dataset getDataset(String datasetId);
}
