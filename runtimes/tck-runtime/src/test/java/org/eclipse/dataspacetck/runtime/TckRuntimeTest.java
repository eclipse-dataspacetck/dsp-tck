/*
 *  Copyright (c) 2025 Think-it GmbH
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

package org.eclipse.dataspacetck.runtime;

import org.eclipse.dataspacetck.core.spi.system.SystemConfiguration;
import org.eclipse.dataspacetck.core.spi.system.SystemLauncher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.eclipse.dataspacetck.core.api.system.SystemsConstants.TCK_LAUNCHER;
import static org.mockito.Mockito.mock;

public class TckRuntimeTest {

    @BeforeEach
    void setUp() {
        System.setProperty("tck-dummy-test", "true");
    }

    @AfterEach
    void tearDown() {
        System.setProperty("tck-dummy-test", "false");
    }

    @Test
    void shouldOverrideLauncherProperty_whenLauncherClassPassed() {
        var runtime = TckRuntime.Builder.newInstance()
                .monitor(mock())
                .property(TCK_LAUNCHER, "any.other.LauncherClass")
                .launcher(TestSystemLauncher.class)
                .addPackage("org.eclipse.dataspacetck.runtime.test")
                .build();

        var summary = runtime.execute();

        assertThat(summary.getFailures()).isEmpty();
    }

    @Test
    void shouldFail_whenNoLauncherProvided() {
        var runtime = TckRuntime.Builder.newInstance()
                .monitor(mock())
                .addPackage("org.eclipse.dataspacetck.runtime.test")
                .build();

        var summary = runtime.execute();

        assertThat(summary.getFailures()).isNotEmpty().anySatisfy(failure -> {
            assertThat(failure.getException()).isInstanceOf(IllegalArgumentException.class).message().contains(TCK_LAUNCHER);
        });
    }

    @Test
    void shouldFail_whenUnexistingLauncherProvided() {
        var runtime = TckRuntime.Builder.newInstance()
                .monitor(mock())
                .property(TCK_LAUNCHER, "an.unexistent.Launcher")
                .addPackage("org.eclipse.dataspacetck.runtime.test")
                .build();

        var summary = runtime.execute();

        assertThat(summary.getFailures()).isNotEmpty().anySatisfy(failure -> {
            assertThat(failure.getException()).isInstanceOf(IllegalArgumentException.class).message()
                    .containsIgnoringCase("unable to create launcher").contains("an.unexistent.Launcher");
        });
    }

    @Test
    void canFilterByTestName() {
        var runtime = TckRuntime.Builder.newInstance()
                .monitor(mock())
                .launcher(TestSystemLauncher.class)
                .addPackage("org.eclipse.dataspacetck.runtime.test")
                .displayNameMatching(displayName -> displayName.equals("FILTER"))
                .build();

        var summary = runtime.execute();

        assertThat(summary.getTestsSucceededCount()).isEqualTo(1);
        assertThat(summary.getTestsFoundCount()).isEqualTo(1);
    }

    public static class TestSystemLauncher implements SystemLauncher {

        @Override
        public void start(SystemConfiguration configuration) {

        }
    }
}
