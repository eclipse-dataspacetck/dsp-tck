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

package org.eclipse.dataspacetck.dsp.system.api.message;

public interface DcatConstants {

    String DCAT_NAMESPACE = "http://www.w3.org/ns/dcat#";
    String DCAT_PROPERTY_DISTRIBUTION = "distribution";
    String DCT_PROPERTY_FORMAT = "format";
    String DCAT_PROPERTY_DATASET = "dataset";
    String DCAT_PROPERTY_ACCESS_SERVICE = "accessService";
    String DCAT_PROPERTY_ENDPOINT_URL = "endpointURL";
    String DCAT_PROPERTY_DATASET_EXPANDED = DCAT_NAMESPACE + DCAT_PROPERTY_DATASET;
    String DCAT_PROPERTY_HAS_POLICY = "hasPolicy";
}
