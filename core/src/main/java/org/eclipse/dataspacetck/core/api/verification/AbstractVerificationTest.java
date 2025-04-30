/*
 *  Copyright (c) 2023 Bayerische Motoren Werke Aktiengesellschaft (BMW AG)
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Bayerische Motoren Werke Aktiengesellschaft (BMW AG) - initial API and implementation
 *
 *
 */

package org.eclipse.dataspacetck.core.api.verification;

import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.ValidationMessage;
import org.eclipse.dataspacetck.core.api.message.MessageValidator;
import org.eclipse.dataspacetck.core.system.SystemBootstrapExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Collectors;

import static com.networknt.schema.SpecVersion.VersionFlag.V202012;


/**
 * Base class for verification tests. Uses the system bootstrap extension.
 */
@ExtendWith(SystemBootstrapExtension.class)
public abstract class AbstractVerificationTest {

    protected static final String CLASSPATH_SCHEMA = "classpath:/";
    protected static final String DSPACE_NAMESPACE = "https://w3id.org/dspace/2025/1";

    private static final JsonSchemaFactory SCHEMA_FACTORY = JsonSchemaFactory.getInstance(V202012, builder ->
            builder.schemaMappers(schemaMappers ->
                    schemaMappers.mapPrefix(DSPACE_NAMESPACE + "/", CLASSPATH_SCHEMA))
    );


    protected static MessageValidator forSchema(String schema) {
        return (input) -> {
            var schemaValidator = SCHEMA_FACTORY.getSchema(SchemaLocation.of(DSPACE_NAMESPACE + schema));

            var response = schemaValidator.validate(input);
            if (response.isEmpty()) {
                return List.of();
            }
            return response.stream().map(ValidationMessage::getMessage).collect(Collectors.toList());
        };
    }
}
