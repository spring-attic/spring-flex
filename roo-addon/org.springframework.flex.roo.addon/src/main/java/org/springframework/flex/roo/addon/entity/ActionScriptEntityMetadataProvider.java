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

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.flex.roo.addon.as.classpath.ASMutablePhysicalTypeMetadataProvider;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeCategory;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifier;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASMutableClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.DefaultASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.MetaTagAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.StringAttributeValue;
import org.springframework.flex.roo.addon.as.model.ActionScriptMappingUtils;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.flex.roo.addon.mojos.FlexPath;
import org.springframework.flex.roo.addon.mojos.FlexPathResolver;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.MutableClassOrInterfaceTypeDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.project.Path;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.CollectionUtils;
import org.springframework.roo.support.util.StringUtils;

/**
 * {@link MetadataProvider} for mappings between Java and ActionScript entities. 
 *
 * @author Jeremy Grelle
 */
@Component(immediate = true)
@Service
public class ActionScriptEntityMetadataProvider implements MetadataProvider, MetadataNotificationListener {

    /**
     * TODO - If the entity implements an interface, should we generate the interface as well? Currently they are
     * ignored. TODO - Consider adding support for getters and setters TODO - Currently ignoring non-accessor methods -
     * is there any reason to do otherwise? TODO - If the entity has a single constructor specified, should we mimic it?
     * Would probably prove overly complicated. TODO - Get JavaType's superclass and recursively generate a
     * corresponding ActionScript class if necessary
     */

    private static final String REMOTE_CLASS_TAG = "RemoteClass";

    private static final String ALIAS_ATTR = "alias";

    @Reference
    private FlexPathResolver flexPathResolver;

    @Reference
    private MetadataService metadataService;

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private ASMutablePhysicalTypeMetadataProvider asPhysicalTypeProvider;

    protected void activate(ComponentContext context) {
        this.metadataDependencyRegistry.registerDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        this.metadataDependencyRegistry.registerDependency(ASPhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    }

    protected void deactivate(ComponentContext context) {
        this.metadataDependencyRegistry.deregisterDependency(PhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
        this.metadataDependencyRegistry.deregisterDependency(ASPhysicalTypeIdentifier.getMetadataIdentiferType(), getProvidesType());
    }

    public MetadataItem get(String metadataId) {

        JavaType javaType = ActionScriptEntityMetadata.getJavaType(metadataId);

        // TODO - Validate that the Java type exists and is a class

        ActionScriptType asType = ActionScriptMappingUtils.toActionScriptType(javaType);

        String asEntityId = this.asPhysicalTypeProvider.findIdentifier(asType);
        if (StringUtils.hasText(asEntityId)) {
            // TODO - If we add Roo-specific meta-tag, we could add it and then trigger off of it in the notification
            // Already exists, so return
            return new ActionScriptEntityMetadata(metadataId, asType, javaType);
        }

        asEntityId = ASPhysicalTypeIdentifier.createIdentifier(asType, FlexPath.SRC_MAIN_FLEX);

        createActionScriptMirrorClass(asEntityId, asType, javaType);

        ActionScriptEntityMetadata asEntityMetadata = new ActionScriptEntityMetadata(metadataId, asType, javaType);
        return asEntityMetadata;
    }

    private void createActionScriptMirrorClass(String asEntityId, ActionScriptType asType, JavaType javaType) {
        Queue<TypeMapping> relatedTypes = new LinkedList<TypeMapping>();

        List<MetaTagAttributeValue<?>> attributes = new ArrayList<MetaTagAttributeValue<?>>();
        attributes.add(new StringAttributeValue(new ActionScriptSymbolName(ALIAS_ATTR), javaType.getFullyQualifiedTypeName()));
        ASMetaTagMetadata remoteClassTag = new DefaultASMetaTagMetadata(REMOTE_CLASS_TAG, attributes);
        List<ASMetaTagMetadata> typeMetaTags = new ArrayList<ASMetaTagMetadata>();
        typeMetaTags.add(remoteClassTag);

        // TODO - for now we will only handle classes...interfaces could come later but would add complexity (i.e., need
        // to find all implementations and mirror those as well)

        List<ASFieldMetadata> declaredFields = new ArrayList<ASFieldMetadata>();
        BeanInfoMetadata beanInfoMetadata = getBeanInfoMetadata(javaType);
        for (MethodMetadata accessor : beanInfoMetadata.getPublicAccessors()) {
            JavaSymbolName propertyName = BeanInfoMetadata.getPropertyNameForJavaBeanMethod(accessor);
            FieldMetadata javaField = beanInfoMetadata.getFieldForPropertyName(propertyName);

            // TODO - We don't add any meta-tags and we set the field to public - any other choice?
            ASFieldMetadata asField = ActionScriptMappingUtils.toASFieldMetadata(asEntityId, javaField, true);
            relatedTypes.addAll(findRequiredMappings(javaField, asField));
            declaredFields.add(asField);
        }

        ASClassOrInterfaceTypeDetails asDetails = new DefaultASClassOrInterfaceTypeDetails(asEntityId, asType, ASPhysicalTypeCategory.CLASS,
            declaredFields, null, null, null, null, null, typeMetaTags);
        // new DefaultASClassOrInterfaceTypeDetails(declaredByMetadataId, name, physicalTypeCategory, declaredFields,
        // declaredConstructor, declaredMethods, superClass, extendsTypes, implementsTypes, typeMetaTags);
        ASPhysicalTypeMetadata asMetadata = new DefaultASPhysicalTypeMetadata(asEntityId, getPhysicalLocationCanonicalPath(asEntityId), asDetails);
        this.asPhysicalTypeProvider.createPhysicalType(asMetadata);

        // Now trigger the creation of any related types
        while (!relatedTypes.isEmpty()) {
            TypeMapping mapping = relatedTypes.poll();
            createActionScriptMirrorClass(mapping.getMetadataId(), mapping.getAsType(), mapping.getJavaType());
        }
    }

    private MutableClassOrInterfaceTypeDetails getClassDetails(String metadataId) {
        PhysicalTypeMetadata metadata = (PhysicalTypeMetadata) this.metadataService.get(metadataId);
        if (metadata == null) {
            return null;
        }
        Assert.isInstanceOf(MutableClassOrInterfaceTypeDetails.class, metadata.getPhysicalTypeDetails(), "Java entity must be a class or interface.");
        return (MutableClassOrInterfaceTypeDetails) metadata.getPhysicalTypeDetails();
    }

    private ASMutableClassOrInterfaceTypeDetails getASClassDetails(String metadataId) {
        ASPhysicalTypeMetadata metadata = (ASPhysicalTypeMetadata) this.metadataService.get(metadataId);
        if (metadata == null) {
            return null;
        }
        Assert.isInstanceOf(ASMutableClassOrInterfaceTypeDetails.class, metadata.getPhysicalTypeDetails(),
            "ActionScript entity must be a class or interface.");
        return (ASMutableClassOrInterfaceTypeDetails) metadata.getPhysicalTypeDetails();
    }

    private BeanInfoMetadata getBeanInfoMetadata(JavaType javaType) {
        String beanInfoMetadataKey = BeanInfoMetadata.createIdentifier(javaType, Path.SRC_MAIN_JAVA);
        return (BeanInfoMetadata) this.metadataService.get(beanInfoMetadataKey);
    }

    private String getPhysicalLocationCanonicalPath(String physicalTypeIdentifier) {
        Assert.isTrue(ASPhysicalTypeIdentifier.isValid(physicalTypeIdentifier), "Physical type identifier is invalid");
        Assert.notNull(this.flexPathResolver, "Cannot computed metadata ID of a type because the path resolver is presently unavailable");
        ActionScriptType asType = ASPhysicalTypeIdentifier.getActionScriptType(physicalTypeIdentifier);
        Path path = ASPhysicalTypeIdentifier.getPath(physicalTypeIdentifier);
        String relativePath = asType.getFullyQualifiedTypeName().replace('.', File.separatorChar) + ".as";
        String physicalLocationCanonicalPath = this.flexPathResolver.getIdentifier(path, relativePath);
        return physicalLocationCanonicalPath;
    }

    public String getProvidesType() {
        return ActionScriptEntityMetadata.getMetadataIdentiferType();
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils.getMetadataClass(upstreamDependency).equals(
            MetadataIdentificationUtils.getMetadataClass(PhysicalTypeIdentifier.getMetadataIdentiferType()))) {
            processJavaTypeChanged(upstreamDependency);
        } else if (MetadataIdentificationUtils.getMetadataClass(upstreamDependency).equals(
            MetadataIdentificationUtils.getMetadataClass(ASPhysicalTypeIdentifier.getMetadataIdentiferType()))) {
            processActionScriptTypeChanged(upstreamDependency);
        }
    }

    private void processActionScriptTypeChanged(String asEntityId) {
        List<String> processedFields = new ArrayList<String>();

        ActionScriptType asType = ASPhysicalTypeIdentifier.getActionScriptType(asEntityId);

        JavaType javaType = ActionScriptMappingUtils.toJavaType(asType);
        String javaEntityId = PhysicalTypeIdentifier.createIdentifier(javaType, Path.SRC_MAIN_JAVA);

        MutableClassOrInterfaceTypeDetails javaTypeDetails = getClassDetails(javaEntityId);

        // Nothing to do if Java class doesn't exist
        if (javaTypeDetails == null) {
            return;
        }

        ASMutableClassOrInterfaceTypeDetails asTypeDetails = getASClassDetails(asEntityId);

        // AS class was probably deleted, so nothing to do.
        if (asTypeDetails == null) {
            return;
        }

        // Verify that the ActionScript class is enabled for remoting
        if (!isRemotingClass(javaType, asTypeDetails)) {
            return;
        }

        List<String> javaFieldNames = new ArrayList<String>();
        for (FieldMetadata javaField : javaTypeDetails.getDeclaredFields()) {
            javaFieldNames.add(javaField.getFieldName().getSymbolName());
        }

        List<String> javaPropertyNames = new ArrayList<String>();
        BeanInfoMetadata beanInfoMetadata = getBeanInfoMetadata(javaType);
        for (MethodMetadata accessor : beanInfoMetadata.getPublicAccessors()) {
            javaPropertyNames.add(StringUtils.uncapitalize(BeanInfoMetadata.getPropertyNameForJavaBeanMethod(accessor).getSymbolName()));
        }

        // TODO - don't currently handle changing of field types because there is no updateField() method on
        // MutablePhysicalTypeDetails
        // TODO - we don't currently create new JavaTypes that don't exist because there is no simple "create entity"
        // operation for us to access

        // Add new fields - here we compare directly against property names instead of fields so that we don't
        // mistakenly add fields that
        // might be ITD-only like version and id
        for (ASFieldMetadata asField : asTypeDetails.getDeclaredFields()) {
            String fieldName = asField.getFieldName().getSymbolName();
            if (!javaPropertyNames.contains(fieldName)) {
                javaTypeDetails.addField(ActionScriptMappingUtils.toFieldMetadata(javaEntityId, asField, true));
            }
            processedFields.add(fieldName);
        }

        // TODO - how should we handle fields that don't exist in the ActionScript object? For now we will just
        // remove...should
        // add some way to turn this off later.

        // Remove missing fields - here we are careful to only remove things for wich there is an actual field in the
        // Java source, so the user
        // can't accidentally remove ITD-required fields like version and id
        for (String javaFieldName : javaFieldNames) {
            if (!processedFields.contains(javaFieldName)) {
                javaTypeDetails.removeField(new JavaSymbolName(javaFieldName));
            }
        }

    }

    private void processJavaTypeChanged(String javaEntityId) {
        Queue<TypeMapping> relatedTypes = new LinkedList<TypeMapping>();
        List<ASFieldMetadata> processedProperties = new ArrayList<ASFieldMetadata>();

        JavaType javaType = PhysicalTypeIdentifier.getJavaType(javaEntityId);

        ActionScriptType asType = ActionScriptMappingUtils.toActionScriptType(javaType);
        String asEntityId = ASPhysicalTypeIdentifier.createIdentifier(asType, FlexPath.SRC_MAIN_FLEX);

        ASMutableClassOrInterfaceTypeDetails asTypeDetails = getASClassDetails(asEntityId);

        if (asTypeDetails == null) {
            return;
        }

        // Verify that the ActionScript class is enabled for remoting
        if (!isRemotingClass(javaType, asTypeDetails)) {
            return;
        }

        List<ASFieldMetadata> declaredFields = asTypeDetails.getDeclaredFields();

        BeanInfoMetadata beanInfoMetadata = getBeanInfoMetadata(javaType);
        for (MethodMetadata accessor : beanInfoMetadata.getPublicAccessors()) {
            JavaSymbolName propertyName = BeanInfoMetadata.getPropertyNameForJavaBeanMethod(accessor);
            FieldMetadata javaField = beanInfoMetadata.getFieldForPropertyName(propertyName);

            // TODO - We don't add any meta-tags and we set the field to public - any other choice? Probaby not until
            // we potentially add some sort of support for AS getters and setters
            ASFieldMetadata asField = ActionScriptMappingUtils.toASFieldMetadata(asEntityId, javaField, true);

            int existingIndex = declaredFields.indexOf(asField);
            if (existingIndex > -1) {
                // Field already exists...does it need to be updated? Should we even do this, or just assume if the
                // type is different that the user changed it intentionally.
                ASFieldMetadata existingField = declaredFields.get(existingIndex);
                if (!existingField.getFieldType().equals(asField.getFieldType())) {
                    asTypeDetails.updateField(asField, false);
                }
            } else {
                asTypeDetails.addField(asField, false);
            }

            relatedTypes.addAll(findRequiredMappings(javaField, asField));

            processedProperties.add(asField);
        }

        // TODO - how should we handle fields that don't exist in the Java object? For now we will just remove...should
        // add some way to turn this off later.
        for (ASFieldMetadata asField : asTypeDetails.getDeclaredFields()) {
            if (!processedProperties.contains(asField)) {
                asTypeDetails.removeField(asField.getFieldName());
            }
        }

        asTypeDetails.commit();

        // Now trigger the creation of any newly added related types
        while (!relatedTypes.isEmpty()) {
            TypeMapping mapping = relatedTypes.poll();
            createActionScriptMirrorClass(mapping.getMetadataId(), mapping.getAsType(), mapping.getJavaType());
        }
    }

    private boolean isRemotingClass(JavaType javaType, ASMutableClassOrInterfaceTypeDetails asTypeDetails) {
        boolean isRemotingClass = false;
        for (ASMetaTagMetadata metaTag : asTypeDetails.getTypeMetaTags()) {
            if (metaTag.getName().equals(REMOTE_CLASS_TAG)) {
                MetaTagAttributeValue<?> value = metaTag.getAttribute(new ActionScriptSymbolName(ALIAS_ATTR));
                if (value != null && value instanceof StringAttributeValue) {
                    if (javaType.getFullyQualifiedTypeName().equals(value.getValue())) {
                        isRemotingClass = true;
                        break;
                    }
                }
            }
        }
        return isRemotingClass;
    }

    private List<TypeMapping> findRequiredMappings(FieldMetadata javaField, ASFieldMetadata asField) {
        List<TypeMapping> relatedTypes = new ArrayList<TypeMapping>();
        if (ActionScriptMappingUtils.isMappableType(asField.getFieldType())) {
            if (!StringUtils.hasText(this.asPhysicalTypeProvider.findIdentifier(asField.getFieldType()))) {
                String relatedEntityId = ASPhysicalTypeIdentifier.createIdentifier(asField.getFieldType(), FlexPath.SRC_MAIN_FLEX);
                if (!asField.getDeclaredByMetadataId().equals(relatedEntityId)) {
                    relatedTypes.add(new TypeMapping(relatedEntityId, asField.getFieldType(), javaField.getFieldType()));
                }
            }
        } else if (javaField.getFieldType().isCommonCollectionType() && !CollectionUtils.isEmpty(javaField.getFieldType().getParameters())) {
            for (JavaType javaParamType : javaField.getFieldType().getParameters()) {
                ActionScriptType asParamType = ActionScriptMappingUtils.toActionScriptType(javaParamType);
                if (!StringUtils.hasText(this.asPhysicalTypeProvider.findIdentifier(asField.getFieldType()))) {
                    String relatedEntityId = ASPhysicalTypeIdentifier.createIdentifier(asParamType, FlexPath.SRC_MAIN_FLEX);
                    if (!asField.getDeclaredByMetadataId().equals(relatedEntityId)) {
                        relatedTypes.add(new TypeMapping(relatedEntityId, asParamType, javaParamType));
                    }
                }
            }
        }
        return relatedTypes;
    }
}
