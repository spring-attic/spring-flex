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
 * Utility methods for manipulation of metadata id's for ActionScript source files.
 *
 * @author Jeremy Grelle
 */
public class ASPhysicalTypeIdentifierNamingUtils {

    public static final String createIdentifier(String providesType, ActionScriptType actionScriptType, Path path) {
        Assert.notNull(actionScriptType, "ActionScript type required");
        Assert.notNull(path, "Path required");
        return MetadataIdentificationUtils.create(providesType, path.getName() + "?" + actionScriptType.getFullyQualifiedTypeName());
    }

    public static final ActionScriptType getActionScriptType(String providesType, String metadataIdentificationString) {
        Assert.isTrue(isValid(providesType, metadataIdentificationString), "Metadata identification string '" + metadataIdentificationString
            + "' does not appear to be a valid actionscript type identifier");
        String instance = MetadataIdentificationUtils.getMetadataInstance(metadataIdentificationString);
        int index = instance.indexOf("?");
        return new ActionScriptType(instance.substring(index + 1));
    }

    public static final Path getPath(String providesType, String metadataIdentificationString) {
        Assert.isTrue(isValid(providesType, metadataIdentificationString), "Metadata identification string '" + metadataIdentificationString
            + "' does not appear to be a valid actionscript type identifier");
        String instance = MetadataIdentificationUtils.getMetadataInstance(metadataIdentificationString);
        int index = instance.indexOf("?");
        return new Path(instance.substring(0, index));
    }

    /**
     * Indicates whether the presented metadata identification string appears to be valid.
     * 
     * @param providesType to verify the presented type (required)
     * @param metadataIdentificationString to evaluate (can be null or empty)
     * @return true only if the string appears to be valid
     */
    public static boolean isValid(String providesType, String metadataIdentificationString) {
        if (!MetadataIdentificationUtils.isIdentifyingInstance(metadataIdentificationString)) {
            return false;
        }
        if (!MetadataIdentificationUtils.getMetadataClass(metadataIdentificationString).equals(providesType)) {
            return false;
        }
        return MetadataIdentificationUtils.getMetadataInstance(metadataIdentificationString).contains("?");
    }
}
