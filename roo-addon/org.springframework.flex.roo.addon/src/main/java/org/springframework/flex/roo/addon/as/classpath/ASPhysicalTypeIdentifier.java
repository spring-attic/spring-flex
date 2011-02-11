/*
 * Copyright 2002-2010 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.roo.addon.as.classpath;

import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Helper class for working with metadata id's for ActionScript source files.
 *
 * @author Jeremy Grelle
 */
public final class ASPhysicalTypeIdentifier {

    private static final String PROVIDES_TYPE_STRING = ASPhysicalTypeIdentifier.class.getName();

    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createIdentifier(ActionScriptType actionScriptType, Path path) {
        return ASPhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, actionScriptType, path);
    }

    public static final ActionScriptType getActionScriptType(String metadataIdentificationString) {
        return ASPhysicalTypeIdentifierNamingUtils.getActionScriptType(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getPath(String metadataIdentificationString) {
        return ASPhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return ASPhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static String getFriendlyName(String metadataIdentificationString) {
        Assert.isTrue(isValid(metadataIdentificationString), "Invalid metadata identification string '" + metadataIdentificationString + "' provided");
        return getPath(metadataIdentificationString) + "/" + getActionScriptType(metadataIdentificationString);
    }
}
