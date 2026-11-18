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
 *       Metaform Systems, Inc. - retry requests answered with 503 Service Unavailable
 *
 */

package org.eclipse.dataspacetck.dsp.system.api.http;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.util.function.Supplier;

import static org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer.serialize;
import static org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer.serializePlainJson;

/**
 * Utility methods for HTTP requests.
 */
public class HttpFunctions {
    private static Interceptor authorizationInterceptor = chain -> chain.proceed(chain.request());

    private HttpFunctions() {
    }

    public static void registerAuthorizationInterceptor(String authorizationHeader) {
        authorizationInterceptor = chain -> {
            var request = chain.request();
            var authenticatedRequest = request.newBuilder()
                    .header("Authorization", authorizationHeader).build();
            return chain.proceed(authenticatedRequest);
        };
    }

    public static void registerAuthorizationInterceptor(Supplier<String> authorizationHeaderProvider) {
        authorizationInterceptor = chain -> {
            var request = chain.request();
            var authenticatedRequest = request.newBuilder()
                    .header("Authorization", authorizationHeaderProvider.get()).build();
            return chain.proceed(authenticatedRequest);
        };
    }

    public static Response postJson(String url, Object message) {
        return postJson(url, message, false);
    }

    public static Response postJson(String url, Object message, boolean expectError) {
        return postJson(url, message, expectError, false);
    }

    public static Response postJson(String url, Object message, boolean expectError, boolean plain) {
        return postJson(url, message, expectError, plain, authorizationInterceptor);
    }

    public static Response postJson(String url, Object message, boolean expectError, Interceptor interceptor) {
        return postJson(url, message, expectError, false, interceptor);
    }

    public static Response postJson(String url, Object message, boolean expectError, boolean plain, Interceptor interceptor) {
        var interceptorToUse = interceptor != null ? interceptor : authorizationInterceptor;
        var serialized = plain ? serializePlainJson(message) : serialize(message);
        var requestBody = RequestBody.create(serialized, MediaType.get("application/json"));
        var httpRequest = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();

        var httpClient = new OkHttpClient.Builder().addInterceptor(interceptorToUse).build();
        int maxRetries = 3;
        int attempt = 0;
        long backoff = 200; // initial backoff in ms

        while (true) {
            try {
                var response = httpClient.newCall(httpRequest).execute();
                if (isRetryable(response, expectError) && attempt < maxRetries - 1) {
                    attempt++;
                    response.close();
                    try {
                        Thread.sleep(backoff);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException(ie);
                    }
                    backoff *= 2;
                    continue;
                }
                if (404 == response.code()) {
                    throw new AssertionError("Unexpected 404 received for request: " + url);
                } else if (!response.isSuccessful()) {
                    if (response.code() < 400 || response.code() >= 500 || !expectError) {
                        throw new AssertionError("Unexpected response code: " + response.code());
                    }
                } else if (expectError) {
                    throw new AssertionError("Expected to throw an error on request: " + url);
                }
                return response;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Whether a request should be sent again:
     * <ul>
     *     <li>{@code 503 Service Unavailable}: the connector under test can't process the message yet, e.g. because
     *     the message arrived before the connector received the acknowledgement of its own preceding message. The
     *     request is sent again, also when an error is expected, so that the final answer is checked;</li>
     *     <li>a client error other than {@code 404}, when no error is expected, for connectors that answer such a
     *     message with a client error.</li>
     * </ul>
     */
    private static boolean isRetryable(Response response, boolean expectError) {
        var code = response.code();
        if (code == 503) {
            return true;
        }
        return !expectError && code != 404 && code >= 400 && code < 500;
    }

    public static Response getJson(String url) {
        return getJson(url, false);
    }

    public static Response getJson(String url, boolean expectError) {
        return getJson(url, expectError, null);
    }

    public static Response getJson(String url, boolean expectError, Interceptor interceptor) {
        var httpRequest = new Request.Builder()
                .url(url)
                .get()
                .build();

        var httpClient = new OkHttpClient.Builder().addInterceptor(authorizationInterceptor).build();
        try {
            var response = httpClient.newCall(httpRequest).execute();
            if (404 == response.code() && !expectError) {
                throw new AssertionError("Unexpected 404 received for request: " + url);
            } else if (!response.isSuccessful() && !expectError) {
                throw new AssertionError("Unexpected response code: " + response.code());
            }
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
