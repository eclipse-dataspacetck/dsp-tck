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

package org.eclipse.dataspacetck.dsp.system.api.service;

import static java.util.Optional.ofNullable;

/**
 * Result of a service invocation.
 */
public class Result<T, F> {
    private final T content;
    private final F failure;
    private final ErrorType errorType;

    private Result(T content, F failure, ErrorType errorType) {
        this.content = content;
        this.failure = failure;
        this.errorType = errorType;
    }

    public static <F> Result<Void, F> success() {
        return new Result<>(null, null, null);
    }

    public static <T, F> Result<T, F> success(T content) {
        return new Result<>(content, null, null);
    }

    public static <T, F> Result<T, F> failure(F failure) {
        return new Result<>(null, failure, ErrorType.GENERAL_ERROR);
    }

    public static <T, F> Result<T, F> failure(F failure, ErrorType errorType) {
        return new Result<>(null, failure, errorType);
    }

    public boolean succeeded() {
        return failure == null;
    }

    public boolean failed() {
        return !succeeded();
    }

    public T getContent() {
        return content;
    }

    public F getFailure() {
        return failure;
    }

    public ErrorType getErrorType() {
        return errorType;
    }

    public <R> R convert() {
        //noinspection unchecked
        return (R) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public String toString() {
        return succeeded() ? ofNullable(getContent()).orElseGet(() -> (T) "").toString() : failure.toString();
    }

    public enum ErrorType {
        NOT_FOUND, UNAUTHORIZED, BAD_REQUEST, GENERAL_ERROR, CONFLICT
    }
}

