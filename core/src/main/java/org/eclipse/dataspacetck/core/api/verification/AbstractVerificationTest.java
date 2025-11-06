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

import com.networknt.schema.Error;
import com.networknt.schema.SchemaLocation;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
import org.eclipse.dataspacetck.core.api.message.MessageValidator;
import org.eclipse.dataspacetck.core.system.SystemBootstrapExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Base class for verification tests. Uses the system bootstrap extension.
 */
@ExtendWith(SystemBootstrapExtension.class)
public abstract class AbstractVerificationTest {

    protected static final String CLASSPATH_SCHEMA = "classpath:/";
    protected static final String DSPACE_NAMESPACE = "https://w3id.org/dspace/2025/1";

    private static final SchemaRegistry SCHEMA_FACTORY = SchemaRegistry.withDialect(Dialects.getDraft201909(), builder ->
            builder.schemaIdResolvers(schemaIdResolvers ->
                    schemaIdResolvers.mapPrefix(DSPACE_NAMESPACE + "/", CLASSPATH_SCHEMA))
    );


    protected static MessageValidator forSchema(String schema) {
        return (input) -> {
            var schemaValidator = SCHEMA_FACTORY.getSchema(SchemaLocation.of(DSPACE_NAMESPACE + schema));

            var response = schemaValidator.validate(input);
            if (response.isEmpty()) {
                return List.of();
            }
            return response.stream().map(Error::getMessage).collect(Collectors.toList());
        };
    }
}
