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

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.DSPACE_CONTEXT;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.ID;
import static org.eclipse.dataspacetck.dsp.system.api.message.DspConstants.VALUE;

public class JsonLdFunctions {
    
    private static final Map<String, String> IDENTITY_TYPE = Map.of("@type", "@id");

    public static Map<String, Object> mapProperty(String key, Map<String, Object> map) {
        return mapProperty(key, map, false);
    }

    public static Map<String, Object> mapProperty(String key, Map<String, Object> map, boolean optional) {
        var untypedValue = map.get(key);
        if (untypedValue == null) {
            if (optional) {
                return null;
            }
            throw new AssertionError(format("Property '%s' was not found", key));
        }
        //noinspection rawtypes
        if (untypedValue instanceof List valueList) {
            if (valueList.isEmpty()) {
                throw new AssertionError(format("Property '%s' was empty", key));
            }
            @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
            var valueContainer = valueList.get(0);
            //noinspection rawtypes
            if (valueContainer instanceof Map propertyMap) {
                //noinspection unchecked
                return propertyMap;
            }
        }
        throw new AssertionError(format("Property '%s' is not a Map", key));
    }

    public static String compactStringProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        return (String) value;
    }

    public static String stringProperty(String key, Map<String, Object> map) {
        return stringProperty(key, VALUE, map);
    }

    public static String stringIdProperty(String key, Map<String, Object> map) {
        return stringProperty(key, ID, map);
    }

    public static String stringProperty(String key, String valKey, Map<String, Object> map) {
        var untypedValue = requireNonNull(map.get(key), "No value for: " + key);
        //noinspection rawtypes
        if (untypedValue instanceof List valueList) {
            if (valueList.isEmpty()) {
                throw new AssertionError(format("Property '%s' was empty", key));
            }
            @SuppressWarnings("SequencedCollectionMethodCanBeUsed")
            var valueContainer = valueList.get(0);
            if (valueContainer instanceof Map) {
                @SuppressWarnings("rawtypes")
                var value = requireNonNull(((Map) valueContainer).get(valKey), format("No %s attribute for property: %s", valKey, key));
                return value.toString();
            }
        }
        throw new AssertionError(format("Property '%s' was not in expanded @value form", key));
    }

    public static String identityProperty(String key, Map<String, Object> map) {
        var value = requireNonNull(map.get(key), "No value for: " + key);
        if (value instanceof Map) {
            @SuppressWarnings("rawtypes")
            var idValue = requireNonNull(((Map) value).get(ID), "No @id value for property: " + key);
            return idValue.toString();
        }
        throw new AssertionError(format("Property '%s' was not in expanded @id form", key));
    }

    public static List<String> createDspContext() {
        return List.of(DSPACE_CONTEXT);
    }

}
