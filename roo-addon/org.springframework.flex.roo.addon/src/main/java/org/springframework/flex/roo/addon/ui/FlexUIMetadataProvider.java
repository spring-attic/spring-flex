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

package org.springframework.flex.roo.addon.ui;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.flex.roo.addon.FlexOperations;
import org.springframework.flex.roo.addon.FlexScaffoldMetadata;
import org.springframework.flex.roo.addon.as.classpath.ASMutablePhysicalTypeMetadataProvider;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifier;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASMutableClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.model.ActionScriptMappingUtils;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.flex.roo.addon.mojos.FlexPath;
import org.springframework.flex.roo.addon.mojos.FlexPathResolver;
import org.springframework.roo.addon.beaninfo.BeanInfoMetadata;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.MemberFindingUtils;
import org.springframework.roo.classpath.details.MethodMetadata;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataIdentificationUtils;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataNotificationListener;
import org.springframework.roo.metadata.MetadataProvider;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.StringUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * {@link MetadataProvider} for the user interface scaffolding for a Flex remoting destination.
 *
 * @author Jeremy Grelle
 */
@Component(immediate = true)
@Service
public class FlexUIMetadataProvider implements MetadataProvider, MetadataNotificationListener {

    @Reference
    private FlexPathResolver flexPathResolver;

    @Reference
    private MetadataDependencyRegistry metadataDependencyRegistry;

    @Reference
    private FileManager fileManager;

    @Reference
    private MetadataService metadataService;

    @Reference
    private ASMutablePhysicalTypeMetadataProvider asPhysicalTypeProvider;

    @Reference
    private FlexOperations flexOperations;

    // private Map<JavaType, String> pluralCache;

    private StringTemplateGroup templateGroup;

    protected void activate(ComponentContext context) {
        this.metadataDependencyRegistry.registerDependency(FlexScaffoldMetadata.getMetadataIdentiferType(), getProvidesType());
        this.templateGroup = new StringTemplateGroup("flexUIMetadataTemplateGroup");
    }

    protected void deactivate(ComponentContext context) {
        this.metadataDependencyRegistry.deregisterDependency(FlexScaffoldMetadata.getMetadataIdentiferType(), getProvidesType());
    }

    // TODO - For now this is mainly being driven by the JavaType referenced by FlexScaffoldMetadata...should see if we
    // can make
    // things happen in the right order to be able to consistently rely on the ActionScriptType instead
    public MetadataItem get(String metadataId) {
        // pluralCache = new HashMap<JavaType, String>();

        JavaType javaType = FlexUIMetadata.getJavaType(metadataId);
        Path path = FlexUIMetadata.getPath(metadataId);
        String flexScaffoldMetadataKey = FlexScaffoldMetadata.createIdentifier(javaType, path);
        FlexScaffoldMetadata flexScaffoldMetadata = (FlexScaffoldMetadata) this.metadataService.get(flexScaffoldMetadataKey);
        ProjectMetadata projectMetadata = (ProjectMetadata) this.metadataService.get(ProjectMetadata.getProjectIdentifier());

        if (flexScaffoldMetadata == null || !flexScaffoldMetadata.isValid() || projectMetadata == null || !projectMetadata.isValid()) {
            return null;
        }

        String presentationPackage = projectMetadata.getTopLevelPackage() + ".presentation";
        String entityPresentationPackage = presentationPackage + "." + flexScaffoldMetadata.getEntityReference().toLowerCase();

        // Install the root application MXML document if it doesn't already exist
        String scaffoldAppFileId = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, projectMetadata.getProjectName() + "_scaffold.mxml");
        if (!this.fileManager.exists(scaffoldAppFileId)) {
            this.flexOperations.createScaffoldApp();
        }

        updateScaffoldIfNecessary(scaffoldAppFileId, flexScaffoldMetadata);

        String flexConfigFileId = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, projectMetadata.getProjectName()
            + "_scaffold-config.xml");
        if (!this.fileManager.exists(flexConfigFileId)) {
            this.flexOperations.createFlexCompilerConfig();
        }

        updateCompilerConfigIfNecessary(flexConfigFileId, entityPresentationPackage, flexScaffoldMetadata);

        // Install the entity event class if it doesn't already exist
        ActionScriptType entityEventType = new ActionScriptType(entityPresentationPackage + "."
            + flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean().getSimpleTypeName() + "Event");
        if (!StringUtils.hasText(this.asPhysicalTypeProvider.findIdentifier(entityEventType))) {
            createEntityEventType(entityEventType, flexScaffoldMetadata);
        }

        // Create or update the list view
        String listViewRelativePath = (entityPresentationPackage + "." + flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean().getSimpleTypeName() + "View").replace(
            '.', File.separatorChar)
            + ".mxml";
        String listViewPath = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, listViewRelativePath);
        writeToDiskIfNecessary(listViewPath,
            buildListViewDocument(flexScaffoldMetadata, getElegibleListFields(projectMetadata, flexScaffoldMetadata)));

        // Create or update the form view
        String formRelativePath = (entityPresentationPackage + "." + flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean().getSimpleTypeName() + "Form").replace(
            '.', File.separatorChar)
            + ".mxml";
        String formPath = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, formRelativePath);
        writeToDiskIfNecessary(formPath, buildFormDocument(flexScaffoldMetadata, getElegibleFormFields(projectMetadata, flexScaffoldMetadata)));

        return new FlexUIMetadata(metadataId);
    }

    private void updateScaffoldIfNecessary(String scaffoldAppFileId, FlexScaffoldMetadata flexScaffoldMetadata) {
        MutableFile scaffoldMutableFile = null;

        Document scaffoldDoc;
        try {
            scaffoldMutableFile = this.fileManager.updateFile(scaffoldAppFileId);
            scaffoldDoc = XmlUtils.getDocumentBuilder().parse(scaffoldMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        String entityName = flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean().getSimpleTypeName();

        if (XmlUtils.findFirstElement("/Application/Declarations/ArrayList[@id='entities' and String='" + entityName + "']",
            scaffoldDoc.getDocumentElement()) != null) {
            return;
        }

        Element entitiesElement = XmlUtils.findFirstElement("/Application/Declarations/ArrayList[@id='entities']", scaffoldDoc.getDocumentElement());
        Assert.notNull(entitiesElement, "Could not find the entities element in the main scaffold mxml.");
        Element entityElement = scaffoldDoc.createElement("fx:String");
        entityElement.setTextContent(entityName);
        entitiesElement.appendChild(entityElement);

        // Build a string representation of the MXML and write it to disk
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlUtils.writeXml(XmlUtils.createIndentingTransformer(), byteArrayOutputStream, scaffoldDoc);
        String mxmlContent = byteArrayOutputStream.toString();

        try {
            FileCopyUtils.copy(mxmlContent, new OutputStreamWriter(scaffoldMutableFile.getOutputStream()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void updateCompilerConfigIfNecessary(String flexConfigFileId, String entityPresentationPackage, FlexScaffoldMetadata flexScaffoldMetadata) {
        MutableFile flexConfigMutableFile = null;

        Document flexConfigDoc;
        try {
            flexConfigMutableFile = this.fileManager.updateFile(flexConfigFileId);
            flexConfigDoc = XmlUtils.getDocumentBuilder().parse(flexConfigMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        String viewName = entityPresentationPackage + "." + flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean().getSimpleTypeName() + "View";

        if (XmlUtils.findFirstElement("/flex-config/includes[symbol='" + viewName + "']", flexConfigDoc.getDocumentElement()) != null) {
            return;
        }

        Element includesElement = XmlUtils.findFirstElement("/flex-config/includes", flexConfigDoc.getDocumentElement());
        Assert.notNull(includesElement, "Could not find the includes element in the flex compiler config.");

        Element entityElement = flexConfigDoc.createElement("symbol");
        entityElement.setTextContent(viewName);
        includesElement.appendChild(entityElement);

        // Build a string representation of the MXML and write it to disk
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlUtils.writeXml(XmlUtils.createIndentingTransformer(), byteArrayOutputStream, flexConfigDoc);
        String mxmlContent = byteArrayOutputStream.toString();

        try {
            FileCopyUtils.copy(mxmlContent, new OutputStreamWriter(flexConfigMutableFile.getOutputStream()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public String getProvidesType() {
        return FlexUIMetadata.getMetadataIdentifierType();
    }

    public void notify(String upstreamDependency, String downstreamDependency) {
        if (MetadataIdentificationUtils.isIdentifyingClass(downstreamDependency)) {
            Assert.isTrue(MetadataIdentificationUtils.getMetadataClass(upstreamDependency).equals(
                MetadataIdentificationUtils.getMetadataClass(FlexScaffoldMetadata.getMetadataIdentiferType())),
                "Expected class-level notifications only for web scaffold metadata (not '" + upstreamDependency + "')");

            // A physical Java type has changed, and determine what the corresponding local metadata identification
            // string would have been
            JavaType javaType = FlexScaffoldMetadata.getJavaType(upstreamDependency);
            Path path = FlexScaffoldMetadata.getPath(upstreamDependency);
            downstreamDependency = FlexUIMetadata.createIdentifier(javaType, path);

            // We only need to proceed if the downstream dependency relationship is not already registered
            // (if it's already registered, the event will be delivered directly later on)
            if (this.metadataDependencyRegistry.getDownstream(upstreamDependency).contains(downstreamDependency)) {
                return;
            }
        }

        // We should now have an instance-specific "downstream dependency" that can be processed by this class
        Assert.isTrue(MetadataIdentificationUtils.getMetadataClass(downstreamDependency).equals(
            MetadataIdentificationUtils.getMetadataClass(getProvidesType())), "Unexpected downstream notification for '" + downstreamDependency
            + "' to this provider (which uses '" + getProvidesType() + "'");

        this.metadataService.evict(downstreamDependency);
        if (get(downstreamDependency) != null) {
            this.metadataDependencyRegistry.notifyDownstream(downstreamDependency);
        }

    }

    private void createEntityEventType(ActionScriptType entityEventType, FlexScaffoldMetadata flexScaffoldMetadata) {
        ActionScriptType entityType = ActionScriptMappingUtils.toActionScriptType(flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean());
        StringTemplate entityEventTemplate = this.templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_event");
        entityEventTemplate.setAttribute("entityEventType", entityEventType);
        entityEventTemplate.setAttribute("entityType", entityType);
        entityEventTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);

        String relativePath = entityEventType.getFullyQualifiedTypeName().replace('.', File.separatorChar) + ".as";
        String fileIdentifier = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, relativePath);
        this.fileManager.createOrUpdateTextFileIfRequired(fileIdentifier, entityEventTemplate.toString());
    }

    private Document buildListViewDocument(FlexScaffoldMetadata flexScaffoldMetadata, List<FieldMetadata> elegibleFields) {
        ActionScriptType entityType = ActionScriptMappingUtils.toActionScriptType(flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean());
        StringTemplate listViewTemplate = this.templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_list_view");
        listViewTemplate.setAttribute("entityType", entityType);
        listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
        listViewTemplate.setAttribute("fields", elegibleFields);

        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(listViewTemplate.toString().getBytes("UTF-8"));
            return XmlUtils.getDocumentBuilder().parse(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build list view document", e);
        }
    }

    private Document buildFormDocument(FlexScaffoldMetadata flexScaffoldMetadata, List<FieldMetadata> elegibleFields) {
        ActionScriptType entityType = ActionScriptMappingUtils.toActionScriptType(flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean());
        StringTemplate listViewTemplate = this.templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
        listViewTemplate.setAttribute("entityType", entityType);
        listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
        listViewTemplate.setAttribute("fields", wrapFields(elegibleFields));
        Set<RelatedTypeWrapper> relatedTypes = findRelatedTypes(flexScaffoldMetadata, elegibleFields);
        listViewTemplate.setAttribute("relatedTypes", relatedTypes);
        listViewTemplate.setAttribute("labelFields", calculateLabelFields(relatedTypes));
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(listViewTemplate.toString().getBytes("UTF-8"));
            return XmlUtils.getDocumentBuilder().parse(stream);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build list view document", e);
        }
    }

    // TODO - Provide a clean way for the user to specify a specific label via metadata
    private Map<String, String> calculateLabelFields(Set<RelatedTypeWrapper> relatedTypes) {
        Map<String, String> labelFields = new HashMap<String, String>();
        for (RelatedTypeWrapper relatedType : relatedTypes) {
            String labelField = "id"; // Default in case we don't find a better candidate
            String asPhysicalTypeMetadataKey = ASPhysicalTypeIdentifier.createIdentifier(relatedType.getType(), FlexPath.SRC_MAIN_FLEX);
            ASMutableClassOrInterfaceTypeDetails details = getASClassDetails(asPhysicalTypeMetadataKey);
            if (details == null) {
                continue;
            }
            for (ASFieldMetadata asField : details.getDeclaredFields()) {
                if (asField.getFieldType().equals(ActionScriptType.STRING_TYPE)) {
                    labelField = asField.getFieldName().getSymbolName();
                    break;
                }
            }
            for (FieldMetadata fieldOfType : relatedType.fieldsOfType) {
                labelFields.put(fieldOfType.getFieldName().getSymbolName(), labelField);
            }
        }
        return labelFields;
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

    // TODO - Should we actually obtain the FlexScaffoldMetadata for the other type? Could wind up in an infinite loop
    // if not careful
    private Set<RelatedTypeWrapper> findRelatedTypes(FlexScaffoldMetadata flexScaffoldMetadata, List<FieldMetadata> elegibleFields) {
        Set<RelatedTypeWrapper> relatedTypes = new LinkedHashSet<RelatedTypeWrapper>();
        BeanInfoMetadata beanInfoMetadata = flexScaffoldMetadata.getBeanInfoMetadata();
        for (MethodMetadata accessor : beanInfoMetadata.getPublicAccessors()) {
            JavaSymbolName propertyName = BeanInfoMetadata.getPropertyNameForJavaBeanMethod(accessor);
            FieldMetadata javaField = beanInfoMetadata.getFieldForPropertyName(propertyName);
            if (null != MemberFindingUtils.getAnnotationOfType(javaField.getAnnotations(), new JavaType("javax.persistence.OneToOne"))
                || null != MemberFindingUtils.getAnnotationOfType(javaField.getAnnotations(), new JavaType("javax.persistence.ManyToOne"))) {
                ActionScriptType asType = ActionScriptMappingUtils.toActionScriptType(javaField.getFieldType());
                relatedTypes.add(new RelatedTypeWrapper(asType, elegibleFields, !asType.getFullyQualifiedTypeName().equals(
                    flexScaffoldMetadata.getBeanInfoMetadata().getJavaBean().getFullyQualifiedTypeName())));
            }
        }
        return relatedTypes;
    }

    protected static List<FormFieldWrapper> wrapFields(List<FieldMetadata> elegibleFields) {
        List<FormFieldWrapper> wrappedFields = new ArrayList<FormFieldWrapper>();
        for (FieldMetadata field : elegibleFields) {
            wrappedFields.add(new FormFieldWrapper(field));
        }
        return wrappedFields;
    }

    private List<FieldMetadata> getElegibleListFields(ProjectMetadata projectMetadata, FlexScaffoldMetadata flexScaffoldMetadata) {
        List<FieldMetadata> eligibleFields = new ArrayList<FieldMetadata>();
        BeanInfoMetadata beanInfoMetadata = flexScaffoldMetadata.getBeanInfoMetadata();
        for (MethodMetadata accessor : beanInfoMetadata.getPublicAccessors(false)) {
            JavaSymbolName propertyName = BeanInfoMetadata.getPropertyNameForJavaBeanMethod(accessor);
            FieldMetadata javaField = beanInfoMetadata.getFieldForPropertyName(propertyName);
            // TODO - For now we ignore relationships in the list view
            if (!javaField.getFieldType().isCommonCollectionType()
                && !javaField.getFieldType().isArray()
                && !javaField.getFieldType().getPackage().getFullyQualifiedPackageName().startsWith(
                    projectMetadata.getTopLevelPackage().getFullyQualifiedPackageName())) {
                // Never include id field
                if (MemberFindingUtils.getAnnotationOfType(javaField.getAnnotations(), new JavaType("javax.persistence.Id")) != null) {
                    continue;
                }
                // Never include version field
                if (MemberFindingUtils.getAnnotationOfType(javaField.getAnnotations(), new JavaType("javax.persistence.Version")) != null) {
                    continue;
                }
                eligibleFields.add(javaField);
            }
        }
        return eligibleFields;
    }

    private List<FieldMetadata> getElegibleFormFields(ProjectMetadata projectMetadata, FlexScaffoldMetadata flexScaffoldMetadata) {
        List<FieldMetadata> eligibleFields = new ArrayList<FieldMetadata>();
        BeanInfoMetadata beanInfoMetadata = flexScaffoldMetadata.getBeanInfoMetadata();
        for (MethodMetadata accessor : beanInfoMetadata.getPublicAccessors(false)) {
            JavaSymbolName propertyName = BeanInfoMetadata.getPropertyNameForJavaBeanMethod(accessor);
            FieldMetadata javaField = beanInfoMetadata.getFieldForPropertyName(propertyName);
            // TODO - For now we ignore relationships in the list view
            if (!javaField.getFieldType().isCommonCollectionType() && !javaField.getFieldType().isArray()) {
                // Never include id field
                if (MemberFindingUtils.getAnnotationOfType(javaField.getAnnotations(), new JavaType("javax.persistence.Id")) != null) {
                    continue;
                }
                // Never include version field
                if (MemberFindingUtils.getAnnotationOfType(javaField.getAnnotations(), new JavaType("javax.persistence.Version")) != null) {
                    continue;
                }
                eligibleFields.add(javaField);
            }
        }
        return eligibleFields;
    }

    /** return indicates if disk was changed (ie updated or created) */
    private boolean writeToDiskIfNecessary(String mxmlFilename, Document proposed) {

        Document original = null;

        // If mutableFile becomes non-null, it means we need to use it to write out the contents of jspContent to the
        // file
        MutableFile mutableFile = null;
        if (this.fileManager.exists(mxmlFilename)) {
            try {
                original = XmlUtils.getDocumentBuilder().parse(this.fileManager.getInputStream(mxmlFilename));
            } catch (Exception e) {
                new IllegalStateException("Could not parse file: " + mxmlFilename);
            }
            Assert.notNull(original, "Unable to parse " + mxmlFilename);
            if (MxmlRoundTripUtils.compareDocuments(original, proposed)) { // TODO - need to actually implement the
                                                                           // comparison algorithm in a way that works
                                                                           // for MXML to allow non-destructive editing
                mutableFile = this.fileManager.updateFile(mxmlFilename);
            }
        } else {
            original = proposed;
            mutableFile = this.fileManager.createFile(mxmlFilename);
            Assert.notNull(mutableFile, "Could not create MXML file '" + mxmlFilename + "'");
        }

        try {
            if (mutableFile != null) {
                // Build a string representation of the MXML
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                XmlUtils.writeXml(XmlUtils.createIndentingTransformer(), byteArrayOutputStream, original);
                String mxmlContent = byteArrayOutputStream.toString();

                // We need to write the file out (it's a new file, or the existing file has different contents)
                FileCopyUtils.copy(mxmlContent, new OutputStreamWriter(mutableFile.getOutputStream()));
                // Return and indicate we wrote out the file
                return true;
            }
        } catch (IOException ioe) {
            throw new IllegalStateException("Could not output '" + mutableFile.getCanonicalPath() + "'", ioe);
        }

        // A file existed, but it contained the same content, so we return false
        return false;
    }

    protected static Map<String, String> buildValidationsForField(FieldMetadata field) {
        Map<String, String> validations = new HashMap<String, String>();
        AnnotationMetadata annotationMetadata;
        if (null != (annotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType(
            "javax.validation.constraints.Min")))) {
            AnnotationAttributeValue<?> min = annotationMetadata.getAttribute(new JavaSymbolName("value"));
            if (min != null) {
                validations.put("minValue", min.getValue().toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType(
            "javax.validation.constraints.Max")))) {
            AnnotationAttributeValue<?> max = annotationMetadata.getAttribute(new JavaSymbolName("value"));
            if (max != null) {
                validations.put("maxValue", max.getValue().toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType(
            "javax.validation.constraints.Pattern")))) {
            AnnotationAttributeValue<?> regexp = annotationMetadata.getAttribute(new JavaSymbolName("regexp"));
            if (regexp != null) {
                validations.put("expression", regexp.getValue().toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType(
            "javax.validation.constraints.Size")))) {
            AnnotationAttributeValue<?> max = annotationMetadata.getAttribute(new JavaSymbolName("max"));
            if (max != null) {
                validations.put("maxLength", max.getValue().toString());
            }
            AnnotationAttributeValue<?> min = annotationMetadata.getAttribute(new JavaSymbolName("min"));
            if (min != null) {
                validations.put("minLength", min.getValue().toString());
            }
        }
        if (null != (annotationMetadata = MemberFindingUtils.getAnnotationOfType(field.getAnnotations(), new JavaType(
            "javax.validation.constraints.NotNull")))) {
            validations.put("required", "true");
        }
        return validations.size() > 0 ? validations : null;
    }

    public static final class FormFieldWrapper {

        private final FieldMetadata metadata;

        private final Map<String, String> validations;

        public FormFieldWrapper(FieldMetadata metadata) {
            this.metadata = metadata;
            this.validations = buildValidationsForField(metadata);
        }

        public FieldMetadata getMetadata() {
            return this.metadata;
        }

        public Map<String, String> getValidations() {
            return this.validations;
        }

        public Boolean isDate() {
            return ActionScriptMappingUtils.toActionScriptType(this.metadata.getFieldType()).equals(ActionScriptType.DATE_TYPE);
        }

        public Boolean isBoolean() {
            return ActionScriptMappingUtils.toActionScriptType(this.metadata.getFieldType()).equals(ActionScriptType.BOOLEAN_TYPE);
        }

        public Boolean isNumber() {
            return ActionScriptMappingUtils.toActionScriptType(this.metadata.getFieldType()).isNumeric();
        }

        public Boolean isSingleEndedRelationship() {
            if (ActionScriptMappingUtils.isMappableType(ActionScriptMappingUtils.toActionScriptType(this.metadata.getFieldType()))) {
                return null != MemberFindingUtils.getAnnotationOfType(this.metadata.getAnnotations(), new JavaType("javax.persistence.OneToOne"))
                    || null != MemberFindingUtils.getAnnotationOfType(this.metadata.getAnnotations(), new JavaType("javax.persistence.ManyToOne"));
            }
            return false;
        }
    }

    public static final class RelatedTypeWrapper {

        private final ActionScriptType asType;

        private final List<FieldMetadata> fieldsOfType = new ArrayList<FieldMetadata>();

        private final boolean importRequired;

        public RelatedTypeWrapper(ActionScriptType asType, List<FieldMetadata> elegibleFields, boolean importRequired) {
            this.asType = asType;
            this.importRequired = importRequired;
            for (FieldMetadata field : elegibleFields) {
                if (field.getFieldType().getFullyQualifiedTypeName().equals(asType.getFullyQualifiedTypeName())) {
                    this.fieldsOfType.add(field);
                }
            }
        }

        public String getServiceReference() {
            return StringUtils.uncapitalize(this.asType.getSimpleTypeName()) + "Service";
        }

        public List<FieldMetadata> getFieldsOfType() {
            return this.fieldsOfType;
        }

        public ActionScriptType getType() {
            return this.asType;
        }

        public String getTypeName() {
            return this.asType.getSimpleTypeName();
        }

        public String getTypeReference() {
            return StringUtils.uncapitalize(this.asType.getSimpleTypeName());
        }

        public Boolean isImportRequired() {
            return this.importRequired;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (this.asType == null ? 0 : this.asType.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            RelatedTypeWrapper other = (RelatedTypeWrapper) obj;
            if (this.asType == null) {
                if (other.asType != null) {
                    return false;
                }
            } else if (!this.asType.equals(other.asType)) {
                return false;
            }
            return true;
        }
    }
}
