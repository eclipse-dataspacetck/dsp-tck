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

package org.eclipse.dataspacetck.dsp.system.api.message.catalog;

import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.Catalog;
import org.eclipse.dataspacetck.dsp.system.api.connector.catalog.Dataset;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCAT_PROPERTY_ACCESS_SERVICE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCAT_PROPERTY_DATASET;
import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCAT_PROPERTY_DISTRIBUTION;
import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCAT_PROPERTY_ENDPOINT_URL;
import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCAT_PROPERTY_HAS_POLICY;
import static org.eclipse.dataspacetck.dsp.system.api.message.DcatConstants.DCT_PROPERTY_FORMAT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.CONTEXT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CODE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PARTICIPANT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.createDspContext;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_PROPERTY_ACTION;
import static org.eclipse.dataspacetck.dsp.system.api.message.OdrlConstants.ODRL_PROPERTY_PERMISSION;

/**
 * Utility methods for creating catalog DSP messages.
 */
public class CatalogFunctions {

    private CatalogFunctions() {
    }

    public static Dataset createDataset(String datasetId) {
        var offer = createOffer(UUID.randomUUID().toString(), List.of(new Dataset.Permission("http://www.w3.org/ns/odrl/2/use")));
        var distribution = createDistribution("format", UUID.randomUUID().toString(), "https://example.com");
        return new Dataset(datasetId, List.of(offer), List.of(distribution));
    }

    public static Dataset.Offer createOffer(String id, List<Dataset.Permission> permissions) {
        return new Dataset.Offer(id, permissions);
    }

    public static Dataset.Distribution createDistribution(String format, String dataServiceId, String url) {
        return new Dataset.Distribution(format, new Dataset.DataService(dataServiceId, url));
    }

    public static Map<String, Object> createCatalogRequest() {
        var message = createBaseMessage("CatalogRequestMessage");
        message.put(CONTEXT, createDspContext());
        return message;
    }

    public static Map<String, Object> createDatasetRequest(String datasetId) {
        var message = createBaseMessage("CatalogRequestMessage");
        message.put(CONTEXT, createDspContext());
        message.put(DCAT_PROPERTY_DATASET, datasetId);
        return message;
    }

    public static Map<String, Object> fromDistribution(Dataset.Distribution distribution) {
        var message = createBaseMessage("Distribution");
        message.put(DCT_PROPERTY_FORMAT, distribution.format());
        message.put(DCAT_PROPERTY_ACCESS_SERVICE, fromDataService(distribution.dataService()));
        return message;
    }

    public static Map<String, Object> fromDataService(Dataset.DataService dataService) {
        var message = createBaseMessage("DataService");
        message.put(ID, dataService.id());
        message.put(DCAT_PROPERTY_ENDPOINT_URL, dataService.url());
        return message;
    }

    private static Map<String, Object> fromDataset(Dataset dataset) {
        var message = createBaseMessage("Dataset");
        message.put(ID, dataset.getId());
        message.put(DCAT_PROPERTY_HAS_POLICY, dataset.getOffers().stream().map(CatalogFunctions::fromPolicy).collect(Collectors.toList()));
        message.put(DCAT_PROPERTY_DISTRIBUTION, dataset.getDistributions().stream().map(CatalogFunctions::fromDistribution).collect(Collectors.toList()));
        return message;
    }

    private static Map<String, Object> fromPolicy(Dataset.Offer offer) {
        var message = createBaseMessage("Offer");
        message.put(ID, offer.id());
        message.put(ODRL_PROPERTY_PERMISSION, offer.permissions().stream().map(CatalogFunctions::fromPermission).collect(Collectors.toList()));
        return message;
    }

    private static Map<String, Object> fromPermission(Dataset.Permission permission) {
        var message = createBaseMessage("Permission");
        message.put(ODRL_PROPERTY_ACTION, permission.action());
        return message;
    }

    public static Map<String, Object> createCatalogResponse(Catalog catalog) {
        var message = createBaseMessage("Catalog");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PARTICIPANT_ID, catalog.getParticipantId());
        message.put(DCAT_PROPERTY_DATASET, catalog.getDatasets().values().stream().map(CatalogFunctions::fromDataset).collect(Collectors.toList()));
        return message;
    }

    public static Map<String, Object> createDatasetResponse(Dataset dataset) {
        var message = fromDataset(dataset);
        var context = createDspContext();
        message.put(CONTEXT, context);
        return message;
    }

    public static Map<String, Object> createCatalogErrorResponse(String code) {
        var message = createBaseMessage("CatalogError");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_CODE, code);
        return message;
    }

    @NotNull
    private static Map<String, Object> createBaseMessage(String type) {
        var message = new LinkedHashMap<String, Object>();
        message.put(ID, UUID.randomUUID().toString());
        message.put(TYPE, type);
        return message;
    }

}
