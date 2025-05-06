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

package org.eclipse.dataspacetck.dsp.verification.tp;

import org.eclipse.dataspacetck.core.api.system.ConfigParam;
import org.eclipse.dataspacetck.core.api.system.Inject;
import org.eclipse.dataspacetck.dsp.system.api.connector.Connector;
import org.eclipse.dataspacetck.dsp.system.api.connector.Provider;
import org.eclipse.dataspacetck.dsp.system.api.mock.tp.ConsumerTransferProcessMock;
import org.eclipse.dataspacetck.dsp.system.api.pipeline.tp.ConsumerTransferProcessPipeline;
import org.junit.jupiter.api.Tag;

import static java.util.UUID.randomUUID;

/**
 * Base class for verifying a connector in the consumer role.
 */
@Tag("dsp-tp")
public abstract class AbstractTransferProcessConsumerTest extends AbstractTransferProcessTest {

    @Inject
    @Provider
    protected Connector providerConnector;

    @Inject
    protected ConsumerTransferProcessPipeline transferProcessPipeline;

    @Inject
    protected ConsumerTransferProcessMock transferProcessMock;

    @ConfigParam
    protected String agreementId = randomUUID().toString();

    @ConfigParam
    protected String format = "HTTP-PULL";

}
