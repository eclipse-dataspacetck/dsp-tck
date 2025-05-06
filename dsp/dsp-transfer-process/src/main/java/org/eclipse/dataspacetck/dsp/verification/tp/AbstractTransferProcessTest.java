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

import org.eclipse.dataspacetck.core.api.verification.AbstractVerificationTest;
import org.junit.jupiter.api.BeforeAll;

import static org.eclipse.dataspacetck.core.api.message.MessageSerializer.registerValidator;

public class AbstractTransferProcessTest extends AbstractVerificationTest {


    @BeforeAll
    static void setUp() {
        registerValidator("TransferRequestMessage", forSchema("/transfer/transfer-request-message-schema.json"));
        registerValidator("TransferStartMessage", forSchema("/transfer/transfer-start-message-schema.json"));
        registerValidator("TransferProcess", forSchema("/transfer/transfer-process-schema.json"));
    }
}
