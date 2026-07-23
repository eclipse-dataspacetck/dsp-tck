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

import com.apicatalog.jsonld.JsonLdError;
import com.apicatalog.jsonld.JsonLdOptions;
import com.apicatalog.jsonld.document.Document;
import com.apicatalog.jsonld.document.JsonDocument;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import org.eclipse.dataspacetck.core.api.message.MessageValidator;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;
import tools.jackson.datatype.jsonp.JSONPModule;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.apicatalog.jsonld.JsonLd.compact;
import static com.apicatalog.jsonld.JsonLd.expand;
import static com.apicatalog.jsonld.lang.Keywords.CONTEXT;
import static com.apicatalog.jsonld.lang.Keywords.TYPE;
import static java.util.Collections.emptyList;

/**
 * Provides a configured {@link ObjectMapper} for serializing and deserializing JSON-LD messages.
 */
public class MessageSerializer {


    public static final JsonDocument COMPACT_CONTEXT = JsonDocument.of(Json.createObjectBuilder()
            .add(CONTEXT, Json.createArrayBuilder().add("https://w3id.org/dspace/2025/1/context.jsonld"))
            .build());
    public static final ObjectMapper MAPPER;
    private static final Map<URI, Document> CONTEXTS;
    private static final Map<String, MessageValidator> VALIDATORS = new ConcurrentHashMap<>();
    private static final Pattern JSONLD_PREFIX_REGEX = Pattern.compile("dataspacetck\\.dsp\\.jsonld\\.context\\.(\\w*)");
    private static final String JSONLD_PREFIX = "dataspacetck.dsp.jsonld.context.";

    static {
        MAPPER = JsonMapper.builder()
                .addModule(new JSONPModule())
                .addModule(new SimpleModule())
                .build();
        CONTEXTS = new HashMap<>();
        registerDocument(URI.create("https://w3id.org/dspace/2025/1/context.jsonld"), "dsp-2025-1.jsonld");
        registerDocument(URI.create("https://w3id.org/dspace/2025/1/odrl-profile.jsonld"), "dsp-2025-1-odrl-profile.jsonld");
        loadCustomContexts();
    }

    private MessageSerializer() {
    }

    public static void registerValidator(String type, MessageValidator validator) {
        VALIDATORS.put(type, validator);
    }

    public static String serialize(Object object) {
        try {
            var options = new JsonLdOptions((uri, documentLoaderOptions) -> CONTEXTS.get(uri));
            var compacted = compact(JsonDocument.of(MAPPER.convertValue(object, JsonObject.class)), COMPACT_CONTEXT)
                    .options(options)
                    .get();
            validateMessage(compacted);

            return MAPPER.writeValueAsString(compacted);
        } catch (JsonLdError e) {
            throw new RuntimeException(e);
        }
    }

    public static String serializePlainJson(Object object) {
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T deserialize(InputStream stream, Class<T> type) {
        try {
            return MAPPER.readValue(stream, type);
        } catch (JacksonException e) {
            throw new AssertionError("Cannot deserialize json: " + e.getMessage(), e);
        }
    }

    public static Map<String, Object> expandAndDeserialize(InputStream stream) {
        return expandAndDeserialize(deserialize(stream, JsonObject.class));
    }

    public static Map<String, Object> expandAndDeserialize(Map<String, Object> message) {
        return expandAndDeserialize(Json.createObjectBuilder(message).build());
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> expandAndDeserialize(JsonObject document) {
        try {
            validateMessage(document);

            var options = new JsonLdOptions((uri, documentLoaderOptions) -> CONTEXTS.get(uri));
            var jsonArray = expand(JsonDocument.of(document)).options(options).get();
            if (jsonArray.isEmpty()) {
                throw new AssertionError("Invalid Json document, expecting a non-empty array");
            }
            var expanded = jsonArray.get(0);
            return MAPPER.convertValue(expanded, Map.class);
        } catch (JsonLdError e) {
            throw new AssertionError("Cannot expand json-ld document: " + e.getMessage(), e);
        }
    }

    private static void validateMessage(JsonObject document) {
        if (!document.containsKey(TYPE)) {
            throw new AssertionError("Invalid JsonLd Document, expecting a @type attribute");
        }

        var result = Optional.of(document.getString(TYPE))
                .map(VALIDATORS::get)
                .map(validator -> validator.validate(MAPPER.convertValue(document, JsonNode.class)))
                .orElse(emptyList());

        if (!result.isEmpty()) {
            throw new AssertionError("Invalid message: " + result);
        }
    }

    public static void registerDocument(URI uri, String resource) {
        var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (stream == null) {
            throw new IllegalArgumentException("Resource not found: " + resource);
        }
        registerDocument(uri, stream);
    }

    public static void registerDocument(URI uri, InputStream stream) {
        try {
            CONTEXTS.put(uri, JsonDocument.of(stream));
        } catch (JsonLdError e) {
            throw new RuntimeException(e);
        }
    }


    private static void loadCustomContexts() {
        var ids = System.getProperties()
                .stringPropertyNames()
                .stream()
                .map(MessageSerializer::getContextName)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (var id : ids) {
            var path = System.getProperty(JSONLD_PREFIX + id + ".path");
            if (path == null) {
                continue;
            }
            var uri = System.getProperty(JSONLD_PREFIX + id + ".uri");
            if (uri == null) {
                continue;
            }
            try {
                registerDocument(URI.create(uri), new FileInputStream(path));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

    private static String getContextName(String key) {
        var matcher = JSONLD_PREFIX_REGEX.matcher(key);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }
}
