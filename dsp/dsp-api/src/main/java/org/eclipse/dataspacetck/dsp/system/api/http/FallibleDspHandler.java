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

package org.eclipse.dataspacetck.dsp.system.api.http;

import org.eclipse.dataspacetck.core.api.system.HandlerResponse;
import org.eclipse.dataspacetck.core.api.system.ProtocolHandler;
import org.eclipse.dataspacetck.dsp.system.api.service.Result;
import org.jetbrains.annotations.NotNull;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.serialize;

/**
 * A {@link ProtocolHandler} that can return a failure response.
 */
public class FallibleDspHandler implements ProtocolHandler {

    private final Function<InputStream, Result<Map<String, Object>, Map<String, Object>>> handler;

    public FallibleDspHandler(Function<InputStream, Result<Map<String, Object>, Map<String, Object>>> handler) {
        this.handler = handler;
    }

    @Override
    public HandlerResponse apply(Map<String, List<String>> headers, InputStream body) {
        try {
            var result = handler.apply(body);
            if (result.failed()) {
                return toResponse(result);
            }
            return new HandlerResponse(200, serialize(result.getContent()));
        } catch (Exception e) {
            return new HandlerResponse(500, e.getMessage());
        }
    }

    @NotNull
    private HandlerResponse toResponse(Result<Map<String, Object>, Map<String, Object>> result) {
        var failure = serialize(result.getFailure());
        return switch (result.getErrorType()) {
            case BAD_REQUEST -> new HandlerResponse(400, failure);
            case UNAUTHORIZED -> new HandlerResponse(401, failure);
            case NOT_FOUND -> new HandlerResponse(404, failure);
            case GENERAL_ERROR -> new HandlerResponse(500, failure);
            case CONFLICT -> new HandlerResponse(409, failure);
        };
    }
}
