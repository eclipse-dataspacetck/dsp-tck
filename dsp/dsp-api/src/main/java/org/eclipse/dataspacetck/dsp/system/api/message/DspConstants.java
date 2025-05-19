/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package org.eclipse.dataspacetck.dsp.system.api.message;

/**
 * DSP Message constants.
 */
public interface DspConstants {

    String TCK_PARTICIPANT_ID = "TCK_PARTICIPANT";

    String DSPACE_NAMESPACE = "https://w3id.org/dspace/2025/1/";

    String DSPACE_CONTEXT = "https://w3id.org/dspace/2025/1/context.jsonld";

    String CONTEXT = "@context";

    String ID = "@id";

    String VALUE = "@value";

    String TYPE = "@type";


    String DSPACE_PROPERTY_TIMESTAMP = "timestamp";
    String DSPACE_PROPERTY_PROVIDER_PID = "providerPid";
    String DSPACE_PROPERTY_CODE = "code";
    String DSPACE_PROPERTY_REASON = "reason";
    String DSPACE_PROPERTY_STATE = "state";
    String DSPACE_PROPERTY_EVENT_TYPE = "eventType";
    String DSPACE_PROPERTY_CALLBACK_ADDRESS = "callbackAddress";
    String DSPACE_PROPERTY_OFFER = "offer";
    String DSPACE_PROPERTY_CONSUMER_PID = "consumerPid";
    String DSPACE_PROPERTY_AGREEMENT = "agreement";
    String DSPACE_PROPERTY_CONSUMER_PID_EXPANDED = DSPACE_NAMESPACE + "consumerPid";
    String DSPACE_PROPERTY_PROVIDER_PID_EXPANDED = DSPACE_NAMESPACE + "providerPid";
    String DSPACE_PROPERTY_STATE_EXPANDED = DSPACE_NAMESPACE + "state";
    String DSPACE_PROPERTY_EVENT_TYPE_EXPANDED = DSPACE_NAMESPACE + "eventType";
    String DSPACE_PROPERTY_CALLBACK_ADDRESS_EXPANDED = DSPACE_NAMESPACE + "callbackAddress";
    String DSPACE_PROPERTY_OFFER_EXPANDED = DSPACE_NAMESPACE + "offer";

    // Transfer Process
    String DSPACE_PROPERTY_DATA_ADDRESS = "dataAddress";
    String DSPACE_PROPERTY_ENDPOINT_TYPE = "endpointType";
    String DSPACE_PROPERTY_ENDPOINT = "endpoint";
    String DSPACE_PROPERTY_ENDPOINT_PROPERTIES = "endpointProperties";
    String DSPACE_PROPERTY_AGREEMENT_ID = "agreementId";
    String DSPACE_PROPERTY_FORMAT = "format";
    String DSPACE_PROPERTY_NAME = "name";
    String DSPACE_PROPERTY_VALUE = "value";

    String DSPACE_PROPERTY_DATA_ADDRESS_EXPANDED = DSPACE_NAMESPACE + DSPACE_PROPERTY_DATA_ADDRESS;
    String DSPACE_PROPERTY_ENDPOINT_TYPE_EXPANDED = DSPACE_NAMESPACE + DSPACE_PROPERTY_ENDPOINT_TYPE;
    String DSPACE_PROPERTY_ENDPOINT_EXPANDED = DSPACE_NAMESPACE + DSPACE_PROPERTY_ENDPOINT;
    String DSPACE_PROPERTY_ENDPOINT_PROPERTIES_EXPANDED = DSPACE_NAMESPACE + DSPACE_PROPERTY_ENDPOINT_PROPERTIES;
    String DSPACE_PROPERTY_ENDPOINT_PROPERTY_NAME_EXPANDED = DSPACE_NAMESPACE + DSPACE_PROPERTY_NAME;
    String DSPACE_PROPERTY_ENDPOINT_PROPERTY_VALUE_EXPANDED = DSPACE_NAMESPACE + DSPACE_PROPERTY_VALUE;


    // Catalog
    String DSPACE_PROPERTY_PARTICIPANT_ID = "participantId";
    String DSPACE_CATALOG_ERROR = DSPACE_NAMESPACE + "CatalogError";

}
