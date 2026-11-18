/*
 *  Copyright (c) 2026 Metaform Systems, Inc.
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

package org.eclipse.dataspacetck.dsp.system.api.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.eclipse.dataspacetck.dsp.system.api.http.HttpFunctions.postJson;

class HttpFunctionsTest {

    private final ConcurrentLinkedQueue<Integer> statusCodes = new ConcurrentLinkedQueue<>();
    private final AtomicInteger requests = new AtomicInteger();
    private HttpServer server;
    private String url;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/", exchange -> {
            requests.incrementAndGet();
            var status = statusCodes.size() > 1 ? statusCodes.poll() : statusCodes.peek();
            var body = "{}".getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            try (var out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.start();
        url = "http://localhost:%d/message".formatted(server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void postJson_shouldRetry_whenServiceIsUnavailable() {
        statusCodes.addAll(List.of(503, 200));

        try (var response = postJson(url, Map.of("key", "value"), false, true)) {
            assertThat(response.code()).isEqualTo(200);
        }
        assertThat(requests.get()).isEqualTo(2);
    }

    @Test
    void postJson_shouldRetry_whenServiceIsUnavailable_andErrorIsExpected() {
        statusCodes.addAll(List.of(503, 400));

        try (var response = postJson(url, Map.of("key", "value"), true, true)) {
            assertThat(response.code()).isEqualTo(400);
        }
        assertThat(requests.get()).isEqualTo(2);
    }

    @Test
    void postJson_shouldFail_whenServiceStaysUnavailable() {
        statusCodes.add(503);

        assertThatThrownBy(() -> postJson(url, Map.of("key", "value"), false, true))
                .isInstanceOf(AssertionError.class)
                .hasMessageContaining("503");
        assertThat(requests.get()).isEqualTo(3);
    }

    @Test
    void postJson_shouldNotRetry_whenErrorIsExpectedAndReceived() {
        statusCodes.add(400);

        try (var response = postJson(url, Map.of("key", "value"), true, true)) {
            assertThat(response.code()).isEqualTo(400);
        }
        assertThat(requests.get()).isEqualTo(1);
    }
}
