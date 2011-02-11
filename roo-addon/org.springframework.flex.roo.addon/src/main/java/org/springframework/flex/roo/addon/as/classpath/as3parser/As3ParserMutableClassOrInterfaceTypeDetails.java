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

package org.springframework.flex.roo.addon.as.classpath.as3parser;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeCategory;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadataProvider;
import org.springframework.flex.roo.addon.as.classpath.as3parser.details.As3ParserConstructorMetadata;
import org.springframework.flex.roo.addon.as.classpath.as3parser.details.As3ParserFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.as3parser.details.As3ParserMetaTagMetadata;
import org.springframework.flex.roo.addon.as.classpath.as3parser.details.As3ParserMethodMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.details.ASConstructorMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASMethodMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASMutableClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.model.ActionScriptPackage;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.CollectionUtils;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;

import uk.co.badgersinfoil.metaas.ActionScriptFactory;
import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASField;
import uk.co.badgersinfoil.metaas.dom.ASInterfaceType;
import uk.co.badgersinfoil.metaas.dom.ASMetaTag;
import uk.co.badgersinfoil.metaas.dom.ASMethod;
import uk.co.badgersinfoil.metaas.dom.ASType;

/**
 * Parser-specific details for a mutable ActionScript source file. 
 *
 * @author Jeremy Grelle
 */
public class As3ParserMutableClassOrInterfaceTypeDetails implements ASMutableClassOrInterfaceTypeDetails, CompilationUnitServices {

    // passed into constructor
    private final FileManager fileManager;

    private final String fileIdentifier;

    private final String declaredByMetadataId;

    // to satisfy interface
    private final ActionScriptType name;

    private ASPhysicalTypeCategory physicalTypeCategory;

    private ASConstructorMetadata declaredConstructor;

    private final List<ASFieldMetadata> declaredFields = new ArrayList<ASFieldMetadata>();

    private final List<ASMethodMetadata> declaredMethods = new ArrayList<ASMethodMetadata>();

    private ASClassOrInterfaceTypeDetails superclass = null;

    private final List<ActionScriptType> extendsTypes = new ArrayList<ActionScriptType>();

    private final List<ActionScriptType> implementsTypes = new ArrayList<ActionScriptType>();

    private final List<ASMetaTagMetadata> typeMetaTags = new ArrayList<ASMetaTagMetadata>();

    // internal use
    private final ASCompilationUnit compilationUnit;

    private final ActionScriptPackage compilationUnitPackage;

    public ASType clazz;

    public boolean isDirty = false;

    @SuppressWarnings("unchecked")
    public As3ParserMutableClassOrInterfaceTypeDetails(ASCompilationUnit compilationUnit, FileManager fileManager, String declaredByMetadataId,
        String fileIdentifier, ActionScriptType typeName, MetadataService metadataService, ASPhysicalTypeMetadataProvider physicalTypeMetadataProvider) {
        Assert.notNull(compilationUnit, "Compilation unit required");
        Assert.notNull(fileManager, "File manager requried");
        Assert.notNull(declaredByMetadataId, "Declared by metadata ID required");
        Assert.notNull(fileIdentifier, "File identifier (canonical path) required");
        Assert.notNull(typeName, "Name required");
        Assert.notNull(metadataService, "Metadata service required");
        Assert.notNull(physicalTypeMetadataProvider, "Physical type metadata provider required");

        this.name = typeName;

        this.declaredByMetadataId = declaredByMetadataId;
        this.fileManager = fileManager;

        this.fileIdentifier = fileIdentifier;

        this.compilationUnit = compilationUnit;

        this.compilationUnitPackage = typeName.getPackage();

        Assert.notNull(compilationUnit.getType(), "No types in compilation unit, so unable to continue parsing");

        this.clazz = compilationUnit.getType();

        // Determine the type name
        // ActionScriptType newName = As3ParserUtils.getActionScriptType(compilationUnitPackage, imports, this.clazz);

        // Revert back to the original type name (thus avoiding unnecessary inferences about java.lang types; see
        // ROO-244)
        // TODO - is this necessary for us?
        // this.name = new ActionScriptType(this.name.getFullyQualifiedTypeName(), newName.getArray(),
        // newName.getDataType());

        if (this.clazz instanceof ASInterfaceType) {
            this.physicalTypeCategory = ASPhysicalTypeCategory.INTERFACE;
        } else {
            this.physicalTypeCategory = ASPhysicalTypeCategory.CLASS;
        }

        // Verify the package declaration appears to be correct
        Assert.isTrue(this.compilationUnitPackage.equals(this.name.getPackage()), "Compilation unit package '" + this.compilationUnitPackage
            + "' unexpected for type '" + this.name.getPackage() + "'");

        if (this.clazz instanceof ASClassType) {
            ASClassType classDef = (ASClassType) this.clazz;
            if (StringUtils.hasLength(classDef.getSuperclass())) {
                ActionScriptType superType = As3ParserUtils.getActionScriptType(this.compilationUnitPackage, getImports(), classDef.getSuperclass());
                this.extendsTypes.add(superType);
                String superclassId = physicalTypeMetadataProvider.findIdentifier(superType);
                ASPhysicalTypeMetadata superPtm = null;
                if (superclassId != null) {
                    superPtm = (ASPhysicalTypeMetadata) metadataService.get(superclassId);
                }
                if (superPtm != null && superPtm.getPhysicalTypeDetails() != null
                    && superPtm.getPhysicalTypeDetails() instanceof ASClassOrInterfaceTypeDetails) {
                    this.superclass = (ASClassOrInterfaceTypeDetails) superPtm.getPhysicalTypeDetails();
                }
            }
            if (!CollectionUtils.isEmpty(classDef.getImplementedInterfaces())) {
                List<String> interfaces = classDef.getImplementedInterfaces();
                for (String interfaceName : interfaces) {
                    this.implementsTypes.add(As3ParserUtils.getActionScriptType(this.compilationUnitPackage, getImports(), interfaceName));
                }
            }
        } else if (this.clazz instanceof ASInterfaceType) {
            ASInterfaceType interfaceDef = (ASInterfaceType) this.clazz;
            if (!CollectionUtils.isEmpty(interfaceDef.getSuperInterfaces())) {
                List<String> superInterfaces = interfaceDef.getSuperInterfaces();
                for (String superInterface : superInterfaces) {
                    this.extendsTypes.add(As3ParserUtils.getActionScriptType(this.compilationUnitPackage, getImports(), superInterface));
                }
            }
        }

        List<ASMetaTag> metaTagList = this.clazz.getAllMetaTags();
        if (metaTagList != null) {
            for (ASMetaTag metaTag : metaTagList) {
                As3ParserMetaTagMetadata md = new As3ParserMetaTagMetadata(metaTag);
                this.typeMetaTags.add(md);
            }
        }

        for (ASMethod method : (List<ASMethod>) this.clazz.getMethods()) {
            if (method.getName().equals(this.name.getSimpleTypeName())) {
                Assert.isNull(this.declaredConstructor, "ActionScript classes may only have one constructor method.");
                this.declaredConstructor = new As3ParserConstructorMetadata(declaredByMetadataId, method, this);
            } else {
                this.declaredMethods.add(new As3ParserMethodMetadata(declaredByMetadataId, method, this));
            }
        }

        if (this.physicalTypeCategory == ASPhysicalTypeCategory.CLASS) {
            ASClassType clazzType = (ASClassType) this.clazz;

            for (ASField field : (List<ASField>) clazzType.getFields()) {
                this.declaredFields.add(new As3ParserFieldMetadata(declaredByMetadataId, field, this));
            }
        }

    }

    public void addField(ASFieldMetadata fieldMetadata, boolean flush) {
        Assert.isInstanceOf(ASClassType.class, this.clazz, "Cannot add a field to an interface");
        As3ParserFieldMetadata.addField(this, ((ASClassType) this.clazz), fieldMetadata, flush);
        if (!flush) {
            this.isDirty = true;
        }
    }

    public void addMethod(ASMethodMetadata methodMetadata, boolean flush) {
        As3ParserMethodMetadata.addMethod(this, this.clazz, methodMetadata, flush);
        if (!flush) {
            this.isDirty = true;
        }
    }

    public void addTypeMetaTag(ASMetaTagMetadata metaTag, boolean flush) {
        As3ParserMetaTagMetadata.addMetaTagToElement(this, metaTag, this.clazz, flush);
        if (!flush) {
            this.isDirty = true;
        }
    }

    public void updateField(ASFieldMetadata fieldMetadata, boolean flush) {
        Assert.isInstanceOf(ASClassType.class, this.clazz, "Cannot update a field on an interface");
        Assert.isTrue(getDeclaredFields().contains(fieldMetadata), "Field does not exist.");
        As3ParserFieldMetadata.updateField(this, ((ASClassType) this.clazz), fieldMetadata, flush);
        if (!flush) {
            this.isDirty = true;
        }
    }

    public String getDeclaredByMetadataId() {
        return this.declaredByMetadataId;
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

    public List<ActionScriptType> getExtendsTypes() {
        return this.extendsTypes;
    }

    public List<ActionScriptType> getImplementsTypes() {
        return this.implementsTypes;
    }

    public List<ASMetaTagMetadata> getTypeMetaTags() {
        return this.typeMetaTags;
    }

    public void removeField(ActionScriptSymbolName fieldName, boolean flush) {
        Assert.isInstanceOf(ASClassType.class, this.clazz, "Cannot remove a field from an interface");
        As3ParserFieldMetadata.removeField(this, ((ASClassType) this.clazz), fieldName, flush);
        if (!flush) {
            this.isDirty = true;
        }
    }

    public void removeTypeMetaTag(String name, boolean flush) {
        As3ParserMetaTagMetadata.removeMetatagFromElement(this, this.clazz, name, flush);
        if (!flush) {
            this.isDirty = true;
        }
    }

    public ActionScriptType getName() {
        return this.name;
    }

    public void flush() {
        ActionScriptFactory factory = new ActionScriptFactory();
        StringWriter writer = new StringWriter();
        try {
            factory.newWriter().write(writer, this.compilationUnit);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Reader compilationUnitReader = new StringReader(writer.toString());
        MutableFile mutableFile = this.fileManager.updateFile(this.fileIdentifier);
        try {
            FileCopyUtils.copy(compilationUnitReader, new OutputStreamWriter(mutableFile.getOutputStream()));
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not update '" + this.fileIdentifier + "'", ioe);
        }
    }

    public ActionScriptPackage getCompilationUnitPackage() {
        return this.compilationUnitPackage;
    }

    @SuppressWarnings("unchecked")
    public List<String> getImports() {
        return this.compilationUnit.getPackage().findImports();
    }

    public void addImport(String fullyQualifiedTypeName) {
        this.compilationUnit.getPackage().addImport(fullyQualifiedTypeName);
    }

    public ASPhysicalTypeCategory getPhysicalTypeCategory() {
        return this.physicalTypeCategory;
    }

    public static final void createType(FileManager fileManager, final ASClassOrInterfaceTypeDetails cit, String fileIdentifier) {
        Assert.notNull(fileManager, "File manager required");
        Assert.notNull(cit, "Class or interface type details required");
        Assert.hasText(fileIdentifier, "File identifier required");

        final String newContents = getOutput(cit);

        fileManager.createOrUpdateTextFileIfRequired(fileIdentifier, newContents);
    }

    public static final String getOutput(final ASClassOrInterfaceTypeDetails cit) {
        ActionScriptFactory factory = new ActionScriptFactory();
        final ASCompilationUnit compilationUnit;
        if (ASPhysicalTypeCategory.CLASS.equals(cit.getPhysicalTypeCategory())) {
            compilationUnit = factory.newClass(cit.getName().getFullyQualifiedTypeName());
        } else {
            compilationUnit = factory.newInterface(cit.getName().getFullyQualifiedTypeName());
        }

        CompilationUnitServices compilationUnitServices = new CompilationUnitServices() {

            public void flush() {
                // No-op
            }

            public ActionScriptPackage getCompilationUnitPackage() {
                return cit.getName().getPackage();
            }

            @SuppressWarnings("unchecked")
            public List<String> getImports() {
                return compilationUnit.getPackage().findImports();
            }

            public void addImport(String fullyQualifiedTypeName) {
                compilationUnit.getPackage().addImport(fullyQualifiedTypeName);
            }
        };

        for (ActionScriptType extendsType : cit.getExtendsTypes()) {
            As3ParserUtils.importTypeIfRequired(compilationUnitServices, extendsType);
            if (compilationUnit.getType() instanceof ASClassType) {
                Assert.isNull(((ASClassType) compilationUnit.getType()).getSuperclass(), "An ActionScript class may only extend one type.");
                ((ASClassType) compilationUnit.getType()).setSuperclass(extendsType.getSimpleTypeName());
            } else {
                ((ASInterfaceType) compilationUnit.getType()).addSuperInterface(extendsType.getSimpleTypeName());
            }
        }

        for (ActionScriptType implementsType : cit.getImplementsTypes()) {
            As3ParserUtils.importTypeIfRequired(compilationUnitServices, implementsType);
            ((ASClassType) compilationUnit.getType()).addImplementedInterface(implementsType.getSimpleTypeName());
        }

        // Add type MetaTags
        for (ASMetaTagMetadata metaTag : cit.getTypeMetaTags()) {
            As3ParserMetaTagMetadata.addMetaTagToElement(compilationUnitServices, metaTag, compilationUnit.getType(), false);
        }

        if (compilationUnit.getType() instanceof ASClassType) {
            // Add fields
            for (ASFieldMetadata field : cit.getDeclaredFields()) {
                As3ParserFieldMetadata.addField(compilationUnitServices, ((ASClassType) compilationUnit.getType()), field, false);
            }

            // Add constructor
            if (cit.getDeclaredConstructor() != null) {
                As3ParserConstructorMetadata.addConstructor(compilationUnitServices, compilationUnit.getType(), cit.getDeclaredConstructor(), false);
            }
        }

        for (ASMethodMetadata method : cit.getDeclaredMethods()) {
            As3ParserMethodMetadata.addMethod(compilationUnitServices, compilationUnit.getType(), method, false);
        }

        StringWriter writer = new StringWriter();
        try {
            factory.newWriter().write(writer, compilationUnit);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return writer.toString();
    }

    public ASClassOrInterfaceTypeDetails getSuperClass() {
        return this.superclass;
    }

    public void addField(ASFieldMetadata fieldMetadata) {
        addField(fieldMetadata, true);
    }

    public void addMethod(ASMethodMetadata methodMetadata) {
        addMethod(methodMetadata, true);
    }

    public void addTypeMetaTag(ASMetaTagMetadata metaTag) {
        addTypeMetaTag(metaTag, true);
    }

    public void updateField(ASFieldMetadata fieldMetadata) {
        addField(fieldMetadata, true);
    }

    public void removeField(ActionScriptSymbolName fieldName) {
        removeField(fieldName, true);
    }

    public void removeTypeMetaTag(String name) {
        removeTypeMetaTag(name, true);
    }

    public void commit() {
        if (this.isDirty) {
            flush();
            this.isDirty = false;
        }
    }
}
