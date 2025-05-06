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

package org.eclipse.dataspacetck.dsp.system.api.message.tp;

import org.eclipse.dataspacetck.dsp.system.api.statemachine.TransferProcess;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.CONTEXT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_AGREEMENT_ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CALLBACK_ADDRESS;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CODE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_CONSUMER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_DATA_ADDRESS;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_ENDPOINT_TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_FORMAT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_PROVIDER_PID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_REASON;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_PROPERTY_STATE;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.TYPE;
import static org.eclipse.dataspacetck.dsp.system.api.message.JsonLdFunctions.createDspContext;

/**
 * Utility methods for creating transfer DSP messages.
 */
public class TransferFunctions {

    private TransferFunctions() {
    }


    public static Map<String, Object> createTransferRequest(String consumerPid, String agreementId, String format, TransferProcess.DataAddress dataAddress, String callbackAddress) {
        var message = createBaseMessage("TransferRequestMessage");
        message.put(CONTEXT, createDspContext());
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerPid);
        if (dataAddress != null) {
            var dad = new LinkedHashMap<String, Object>();
            message.put(DSPACE_PROPERTY_DATA_ADDRESS, dad);
        }
        message.put(DSPACE_PROPERTY_AGREEMENT_ID, agreementId);
        message.put(DSPACE_PROPERTY_FORMAT, format);
        message.put(DSPACE_PROPERTY_CALLBACK_ADDRESS, callbackAddress);
        return message;
    }

    public static Map<String, Object> createTransferResponse(String providerPid, String consumerPid, String state) {
        var message = createBaseMessage("TransferProcess");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerPid);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerPid);
        message.put(DSPACE_PROPERTY_STATE, state);
        return message;
    }

    public static Map<String, Object> createStartRequest(String providerId,
                                                         String consumerId,
                                                         Map<String, Object> dataAddress) {
        var message = createBaseMessage("TransferStartMessage");
        var context = createDspContext();
        message.put(CONTEXT, context);
        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);

        if (dataAddress != null) {
            message.put(DSPACE_PROPERTY_DATA_ADDRESS, dataAddress);
        }
        return message;
    }

    public static Map<String, Object> createTermination(String providerId, String consumerId, String code, String... reasons) {
        var message = createBaseMessage("TransferTerminationMessage");
        message.put(CONTEXT, createDspContext());

        message.put(DSPACE_PROPERTY_PROVIDER_PID, providerId);
        message.put(DSPACE_PROPERTY_CONSUMER_PID, consumerId);
        message.put(DSPACE_PROPERTY_CODE, code);

        if (reasons != null && reasons.length > 0) {
            message.put(DSPACE_PROPERTY_REASON, Arrays.stream(reasons).map(reason -> Map.of("message", reason)).collect(toList()));
        }
        return message;
    }

    public static Map<String, Object> dataAddress() {
        var dataAddress = new LinkedHashMap<String, Object>();
        dataAddress.put(TYPE, "DataAddress");
        dataAddress.put(DSPACE_PROPERTY_ENDPOINT, "http://example.com");
        dataAddress.put(DSPACE_PROPERTY_ENDPOINT_TYPE, "https://w3id.org/idsa/v4.1/HTTP");
        return dataAddress;
    }

    @NotNull
    private static Map<String, Object> createBaseMessage(String type) {
        var message = new LinkedHashMap<String, Object>();
        message.put(ID, UUID.randomUUID().toString());
        message.put(TYPE, type);
        return message;
    }

}
