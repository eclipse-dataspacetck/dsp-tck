/*
 *  Copyright (c) 2024 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 */

package org.eclipse.dataspacetck.dsp.suite;

import org.eclipse.dataspacetck.core.system.ConsoleMonitor;
import org.eclipse.dataspacetck.dsp.system.DspSystemLauncher;
import org.eclipse.dataspacetck.runtime.TckRuntime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_LAUNCHER;

class DspTckSuiteTest {

    @Test
    void verifyTestSuite() {
        var result = TckRuntime.Builder.newInstance()
                .property("dataspacetck.dsp.local.connector", "true")
                .property(TCK_LAUNCHER, DspSystemLauncher.class.getName())
                .addPackage("org.eclipse.dataspacetck.dsp.verification")
                .monitor(new ConsoleMonitor(false, true))
                .build().execute();

        assertThat(result.getFailures()).isEmpty();
        assertThat(result.getTestsSucceededCount()).isNotZero();
    }
}
