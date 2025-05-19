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

import java.util.List;

/**
 * Represents a dataset in the catalog.
 * <p>
 * This class contains information about the dataset, including its ID, offers, and distributions.
 * </p>
 */
public class Dataset {

    private final String id;
    private final List<Offer> offers;
    private final List<Distribution> distributions;

    public Dataset(String id, List<Offer> offers, List<Distribution> distributions1) {
        this.id = id;
        this.offers = offers;
        this.distributions = distributions1;
    }

    public String getId() {
        return id;
    }

    public List<Offer> getOffers() {
        return offers;
    }

    public List<Distribution> getDistributions() {
        return distributions;
    }

    public record Offer(String id, List<Permission> permissions) {

    }

    public record Permission(String action) {

    }

    public record Distribution(String format, DataService dataService) {

    }

    public record DataService(String id, String url) {

    }
}
