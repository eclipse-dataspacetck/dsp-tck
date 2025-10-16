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

package org.eclipse.dataspacetck.runtime.test;

import org.eclipse.dataspacetck.core.system.SystemBootstrapExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@EnabledIfSystemProperty(named = "tck-dummy-test", matches = "true")
@ExtendWith(SystemBootstrapExtension.class)
public class DummyTest {

    @Test
    void name() {
        // this test exists only to permit to test TckRuntime
        assertThat(true).isTrue();
    }
}
