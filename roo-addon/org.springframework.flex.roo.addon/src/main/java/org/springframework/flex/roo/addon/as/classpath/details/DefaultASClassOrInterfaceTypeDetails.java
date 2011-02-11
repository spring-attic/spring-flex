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

package org.springframework.flex.roo.addon.as.classpath.details;

import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeCategory;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.support.util.Assert;

/**
 * Default detail representation of an ActionScript class or interface.
 *
 * @author Jeremy Grelle
 */
public class DefaultASClassOrInterfaceTypeDetails implements ASClassOrInterfaceTypeDetails {

    private final ActionScriptType name;

    private final ASPhysicalTypeCategory physicalTypeCategory;

    private ASConstructorMetadata declaredConstructor;

    private List<ASFieldMetadata> declaredFields = new ArrayList<ASFieldMetadata>();

    private List<ASMethodMetadata> declaredMethods = new ArrayList<ASMethodMetadata>();

    private ASClassOrInterfaceTypeDetails superClass;

    private List<ActionScriptType> extendsTypes = new ArrayList<ActionScriptType>();

    private List<ActionScriptType> implementsTypes = new ArrayList<ActionScriptType>();

    private List<ASMetaTagMetadata> typeMetaTags = new ArrayList<ASMetaTagMetadata>();

    private final String declaredByMetadataId;

    public DefaultASClassOrInterfaceTypeDetails(String declaredByMetadataId, ActionScriptType name, ASPhysicalTypeCategory physicalTypeCategory,
        List<ASMetaTagMetadata> typeMetaTags) {
        Assert.hasText(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(name, "Name required");
        Assert.notNull(physicalTypeCategory, "Physical type category required");

        this.declaredByMetadataId = declaredByMetadataId;
        this.name = name;
        this.physicalTypeCategory = physicalTypeCategory;

        if (typeMetaTags != null) {
            this.typeMetaTags = typeMetaTags;
        }
    }

    public DefaultASClassOrInterfaceTypeDetails(String declaredByMetadataId, ActionScriptType name, ASPhysicalTypeCategory physicalTypeCategory,
        List<ASFieldMetadata> declaredFields, ASConstructorMetadata declaredConstructor, List<ASMethodMetadata> declaredMethods,
        ASClassOrInterfaceTypeDetails superClass, List<ActionScriptType> extendsTypes, List<ActionScriptType> implementsTypes,
        List<ASMetaTagMetadata> typeMetaTags) {
        Assert.hasText(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(name, "Name required");
        Assert.notNull(physicalTypeCategory, "Physical type category required");

        this.declaredByMetadataId = declaredByMetadataId;
        this.name = name;
        this.physicalTypeCategory = physicalTypeCategory;
        this.superClass = superClass;
        this.declaredConstructor = declaredConstructor;

        if (declaredFields != null) {
            this.declaredFields = declaredFields;
        }

        if (declaredMethods != null) {
            this.declaredMethods = declaredMethods;
        }

        if (extendsTypes != null) {
            this.extendsTypes = extendsTypes;
        }

        if (implementsTypes != null) {
            this.implementsTypes = implementsTypes;
        }

        if (typeMetaTags != null) {
            this.typeMetaTags = typeMetaTags;
        }
    }

    public ActionScriptType getName() {
        return this.name;
    }

    public ASPhysicalTypeCategory getPhysicalTypeCategory() {
        return this.physicalTypeCategory;
    }

    public ASConstructorMetadata getDeclaredConstructor() {
        return this.declaredConstructor;
    }

    public List<ASFieldMetadata> getDeclaredFields() {
        return this.declaredFields;
    }

    public List<ASMethodMetadata> getDeclaredMethods() {
        return this.declaredMethods;
    }

    public ASClassOrInterfaceTypeDetails getSuperClass() {
        return this.superClass;
    }

    public List<ActionScriptType> getExtendsTypes() {
        return this.extendsTypes;
    }

    public List<ActionScriptType> getImplementsTypes() {
        return this.implementsTypes;
    }

    public List<ASMetaTagMetadata> getTypeMetaTags() {
        return this.typeMetaTags;
    }

    public String getDeclaredByMetadataId() {
        return this.declaredByMetadataId;
    }

}
