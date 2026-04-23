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

package org.eclipse.dataspacetck.dsp.verification;

import org.eclipse.dataspacetck.dsp.system.DspSystemLauncher;
import org.eclipse.dataspacetck.runtime.TckRuntime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataTest {
    @Test
    void verifyTestSuite() {
        var runtime = TckRuntime.Builder.newInstance()
                .property("dataspacetck.dsp.local.connector", "true")
                .launcher(DspSystemLauncher.class)
                .addPackage("org.eclipse.dataspacetck.dsp.verification.metadata")
                .build();

        var result = runtime.execute();

        assertThat(result.getTestsStartedCount()).isGreaterThan(0);
        assertThat(result.getFailures()).isEmpty();
        assertThat(result.getTestsSucceededCount()).isNotZero();
    }
}
