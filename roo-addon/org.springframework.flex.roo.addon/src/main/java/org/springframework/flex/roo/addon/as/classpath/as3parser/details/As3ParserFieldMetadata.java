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

package org.springframework.flex.roo.addon.as.classpath.as3parser.details;

import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.roo.addon.as.classpath.as3parser.As3ParserUtils;
import org.springframework.flex.roo.addon.as.classpath.as3parser.CompilationUnitServices;
import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.AbstractASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.support.util.Assert;

import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASField;
import uk.co.badgersinfoil.metaas.dom.ASMetaTag;

/**
 * Parser-specific metadata representation of an ActionScript field.
 *
 * @author Jeremy Grelle
 */
public class As3ParserFieldMetadata extends AbstractASFieldMetadata {

    private final ActionScriptType fieldType;

    private ActionScriptSymbolName fieldName;

    private final ASTypeVisibility visibility;

    private final String fieldInitializer;

    private final List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();

    private String declaredByMetadataId;

    @SuppressWarnings("unchecked")
    public As3ParserFieldMetadata(String declaredByMetadataId, ASField field, CompilationUnitServices compilationUnitServices) {
        Assert.notNull(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(field, "ActionScript field is required");
        Assert.notNull(compilationUnitServices, "Compilation unit services are required");

        this.setDeclaredByMetadataId(declaredByMetadataId);

        this.fieldType = As3ParserUtils.getActionScriptType(compilationUnitServices.getCompilationUnitPackage(),
            compilationUnitServices.getImports(), field.getType());
        this.setFieldName(new ActionScriptSymbolName(field.getName()));
        this.visibility = As3ParserUtils.getASTypeVisibility(field.getVisibility());
        this.fieldInitializer = field.getInitializer() != null ? field.getInitializer().toString() : null;

        for (ASMetaTag metaTag : (List<ASMetaTag>) field.getAllMetaTags()) {
            this.metaTags.add(new As3ParserMetaTagMetadata(metaTag));
        }
    }

    @Override
    public String getDeclaredByMetadataId() {
        return this.declaredByMetadataId;
    }

    @Override
    public ActionScriptSymbolName getFieldName() {
        return this.fieldName;
    }

    public ActionScriptType getFieldType() {
        return this.fieldType;
    }

    public List<ASMetaTagMetadata> getMetaTags() {
        return this.metaTags;
    }

    public ASTypeVisibility getVisibility() {
        return this.visibility;
    }

    public String getFieldInitializer() {
        return this.fieldInitializer;
    }

    public static void addField(CompilationUnitServices compilationUnitServices, ASClassType clazz, ASFieldMetadata field, boolean permitFlush) {

        Assert.notNull(compilationUnitServices, "Compilation unit services required");
        Assert.notNull(clazz, "Class required");
        Assert.notNull(field, "Field required");

        // Import the field type into the compilation unit
        As3ParserUtils.importTypeIfRequired(compilationUnitServices, field.getFieldType());

        // Add the field
        ASField newField = clazz.newField(field.getFieldName().getSymbolName(), As3ParserUtils.getAs3ParserVisiblity(field.getVisibility()),
            field.getFieldType().getSimpleTypeName());

        if (field.getFieldInitializer() != null) {
            newField.setInitializer(field.getFieldInitializer());
        }

        // Add meta tags to the field
        for (ASMetaTagMetadata metaTag : field.getMetaTags()) {
            As3ParserMetaTagMetadata.addMetaTagToElement(compilationUnitServices, metaTag, newField, false);
        }

        if (permitFlush) {
            compilationUnitServices.flush();
        }
    }

    public static void updateField(CompilationUnitServices compilationUnitServices, ASClassType clazz, ASFieldMetadata field, boolean permitFlush) {

        Assert.notNull(compilationUnitServices, "Compilation unit services required");
        Assert.notNull(clazz, "Class required");
        Assert.notNull(field, "Field required");

        // Import the field type into the compilation unit
        As3ParserUtils.importTypeIfRequired(compilationUnitServices, field.getFieldType());

        ASField existingField = clazz.getField(field.getFieldName().getSymbolName());

        existingField.setVisibility(As3ParserUtils.getAs3ParserVisiblity(field.getVisibility()));

        existingField.setType(field.getFieldType().getSimpleTypeName());

        if (field.getFieldInitializer() != null) {
            existingField.setInitializer(field.getFieldInitializer());
        }

        // Add meta tags to the field
        for (ASMetaTagMetadata metaTag : field.getMetaTags()) {
            if (existingField.getFirstMetatag(metaTag.getName()) != null) {
                As3ParserMetaTagMetadata.addMetaTagToElement(compilationUnitServices, metaTag, existingField, false);
            }
        }
    }

    public static void removeField(CompilationUnitServices compilationUnitServices, ASClassType clazz, ActionScriptSymbolName fieldName,
        boolean permitFlush) {
        Assert.notNull(compilationUnitServices, "Compilation unit services required");
        Assert.notNull(clazz, "Class required");
        Assert.notNull(fieldName, "Field name required");

        Assert.notNull(clazz.getField(fieldName.getSymbolName()), "Could not locate field '" + fieldName + "' to delete");

        clazz.removeField(fieldName.getSymbolName());

        if (permitFlush) {
            compilationUnitServices.flush();
        }
    }

    private void setDeclaredByMetadataId(String declaredByMetadataId) {
        this.declaredByMetadataId = declaredByMetadataId;
    }

    private void setFieldName(ActionScriptSymbolName fieldName) {
        this.fieldName = fieldName;
    }
}
