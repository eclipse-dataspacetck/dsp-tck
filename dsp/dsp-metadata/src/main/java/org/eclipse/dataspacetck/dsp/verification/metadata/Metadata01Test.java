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

package org.eclipse.dataspacetck.dsp.verification.metadata;

import org.eclipse.dataspacetck.api.system.MandatoryTest;
import org.eclipse.dataspacetck.api.system.TestSequenceDiagram;
import org.eclipse.dataspacetck.core.api.system.Inject;
import org.eclipse.dataspacetck.core.api.verification.AbstractVerificationTest;
import org.eclipse.dataspacetck.dsp.system.api.client.metadata.MetadataClient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("dsp-cat")
@Tag("base-compliance")
@DisplayName("MET_01: Metadata request scenarios")
public class Metadata01Test extends AbstractVerificationTest {

    @Inject
    private MetadataClient metadataClient;

    @SuppressWarnings("unchecked")
    @MandatoryTest
    @DisplayName("MET:01-01: Verify metadata request")
    @TestSequenceDiagram("""
            participant TCK as Technology Compatibility Kit (consumer)
            participant CUT as Connector Under Test (provider)
            
            TCK->>CUT: MetadataRequest
            CUT-->>TCK: MetadataResponse
            """)
    public void cat_01_01() {

        var metadata = metadataClient.getMetadata();

        List<Map<String, Object>> versions = (List<Map<String, Object>>) metadata.get("protocolVersions");
        assertThat(versions).isNotNull();

        assertThat(versions).anySatisfy(entry -> {
            assertThat(entry.get("version")).isNotNull()
                    .isEqualTo("2025-1");
            assertThat(entry.get("path")).isNotNull();
        });

    }


}
