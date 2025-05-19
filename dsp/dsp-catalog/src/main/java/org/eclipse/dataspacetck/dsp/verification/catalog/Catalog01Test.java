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

package org.eclipse.dataspacetck.dsp.verification.catalog;

import org.eclipse.dataspacetck.api.system.MandatoryTest;
import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
import org.eclipse.dataspacetck.core.api.system.ConfigParam;
import org.eclipse.dataspacetck.core.api.system.Inject;
import org.eclipse.dataspacetck.core.api.verification.AbstractVerificationTest;
import org.eclipse.dataspacetck.dsp.system.api.client.catalog.CatalogClient;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Provider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.registerValidator;
import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCAT_PROPERTY_DATASET_EXPANDED;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_CATALOG_ERROR;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.catalog.CatalogFunctions.createCatalogRequest;
import static org.eclipse.dataspacetck.dsp.system.api.message.catalog.CatalogFunctions.createDataset;

@Tag("dsp-cat")
@Tag("base-compliance")
@DisplayName("CAT_01: Catalog request scenarios")
public class Catalog01Test extends AbstractVerificationTest {

    @Inject
    @Provider
    protected Connector providerConnector;

    @ConfigParam
    protected String datasetId = randomUUID().toString();

    @Inject
    private CatalogClient catalogClient;

    @BeforeAll
    static void setUp() {
        registerValidator("CatalogRequestMessage", forSchema("/catalog/catalog-request-message-schema.json"));
        registerValidator("DatasetRequestMessage", forSchema("/catalog/dataset-request-message-schema.json"));
        registerValidator("Catalog", forSchema("/catalog/catalog-schema.json"));
        registerValidator("Dataset", forSchema("/catalog/dataset-schema.json"));
    }

    @SuppressWarnings("unchecked")
    @MandatoryTest
    @DisplayName("CAT:01-01: Verify catalog request")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: CatalogRequestMessage
            CUT-->>TCK: Catalog
            """)
    public void cat_01_01() {
        // seed the local connector
        providerConnector.getCatalogManager().addDataset(createDataset(datasetId));

        var catalog = catalogClient.getCatalog(createCatalogRequest());

        assertThat(catalog).isNotNull();
        assertThat(catalog.get(DCAT_PROPERTY_DATASET_EXPANDED)).isNotNull();

        List<Map<String, Object>> datasets = (List<Map<String, Object>>) catalog.get(DCAT_PROPERTY_DATASET_EXPANDED);

        assertThat(datasets).isNotEmpty()
                .anySatisfy(dataset -> assertDataset(datasetId, dataset));
    }


    private void assertDataset(String id, Map<String, Object> dataset) {
        assertThat(dataset.get(ID)).isEqualTo(id);
    }

    @MandatoryTest
    @DisplayName("CAT:01-02: Verify dataset request")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: CatalogDatasetMessage
            CUT-->>TCK: Dataset
            """)
    public void cat_01_02() {

        providerConnector.getCatalogManager().addDataset(createDataset(datasetId));

        var catalogDataset = catalogClient.getDataset(datasetId, false);

        assertThat(catalogDataset).isNotNull();
        assertDataset(datasetId, catalogDataset);
    }

    @MandatoryTest
    @DisplayName("CAT:01-03: Verify dataset request not found")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: CatalogDatasetMessage
            CUT-->>TCK: Dataset
            """)
    public void cat_01_03() {

        var error = catalogClient.getDataset(datasetId, true);

        assertThat(error).containsEntry(TYPE, List.of(DSPACE_CATALOG_ERROR));
    }
}
