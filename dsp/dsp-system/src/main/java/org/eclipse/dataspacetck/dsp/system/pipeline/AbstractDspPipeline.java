/*
 *  Copyright (c) 2026 Think-it GmbH
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Think-it GmbH - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.system.pipeline;

import org.eclipse.dataspacetck.core.api.pipeline.AbstractAsyncPipeline;
import org.eclipse.dataspacetck.core.api.pipeline.AsyncPipeline;
import org.eclipse.dataspacetck.core.api.system.CallbackEndpoint;
import org.eclipse.dataspacetck.core.spi.boot.Monitor;
import org.eclipse.dataspacetck.dsp.system.api.message.MessageSerializer;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

public class AbstractDspPipeline<P extends AsyncPipeline<P>> extends AbstractAsyncPipeline<P> {

    public AbstractDspPipeline(CallbackEndpoint endpoint, Monitor monitor, long waitTime) {
        super(endpoint, monitor, waitTime);
    }

    protected P addHandlerAction(String path, Consumer<Map<String, Object>> action) {
        var latch = new CountDownLatch(1);
        expectLatches.add(latch);
        stages.add(() ->
                endpoint.registerHandler(path, agreement -> {
                    action.accept((MessageSerializer.expandAndDeserialize(agreement)));
                    endpoint.deregisterHandler(path);
                    latch.countDown();
                    return null;
                }));
        //noinspection unchecked
        return (P) this;
    }
}
