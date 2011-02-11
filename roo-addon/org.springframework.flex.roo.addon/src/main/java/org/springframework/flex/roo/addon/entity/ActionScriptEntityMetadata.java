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

package org.springframework.flex.roo.addon.entity;

import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifierNamingUtils;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.flex.roo.addon.mojos.FlexPath;
import org.springframework.roo.classpath.PhysicalTypeIdentifierNamingUtils;
import org.springframework.roo.metadata.AbstractMetadataItem;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;

/**
 * Metadata representation of the root of a managed mapping between an ActionScript entity and a Java entity.
 * 
 * @author Jeremy Grelle
 */
public class ActionScriptEntityMetadata extends AbstractMetadataItem {

    private static final String PROVIDES_TYPE_STRING = ActionScriptEntityMetadata.class.getName();

    private static final String PROVIDES_TYPE = MetadataIdentificationUtils.create(PROVIDES_TYPE_STRING);

    private final ActionScriptType actionScriptType;

    private final JavaType javaType;

    public ActionScriptEntityMetadata(String id, ActionScriptType actionScriptType, JavaType javaType) {
        super(id);
        Assert.notNull(actionScriptType, "The ActionScript type is required.");
        Assert.notNull(javaType, "The Java type is required.");

        this.actionScriptType = actionScriptType;
        this.javaType = javaType;
    }

    public ActionScriptType getActionScriptType() {
        return this.actionScriptType;
    }

    public JavaType getJavaType() {
        return this.javaType;
    }

    public static final String getMetadataIdentiferType() {
        return PROVIDES_TYPE;
    }

    public static final String createTypeIdentifier(ActionScriptType asType, FlexPath path) {
        return ASPhysicalTypeIdentifierNamingUtils.createIdentifier(PROVIDES_TYPE_STRING, asType, path);
    }

    public static final JavaType getJavaType(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getJavaType(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static final Path getJavaPath(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.getPath(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }

    public static boolean isValid(String metadataIdentificationString) {
        return PhysicalTypeIdentifierNamingUtils.isValid(PROVIDES_TYPE_STRING, metadataIdentificationString);
    }
}
