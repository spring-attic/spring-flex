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

package org.springframework.flex.roo.addon;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.component.ComponentContext;
import org.springframework.flex.roo.addon.as.model.ActionScriptMappingUtils;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.flex.roo.addon.entity.ActionScriptEntityMetadata;
import org.springframework.flex.roo.addon.mojos.FlexPath;
import org.springframework.flex.roo.addon.mojos.FlexPathResolver;
import org.springframework.roo.addon.entity.EntityMetadata;
import org.springframework.roo.addon.web.mvc.controller.WebMvcOperations;
import org.springframework.roo.classpath.PhysicalTypeCategory;
import org.springframework.roo.classpath.PhysicalTypeIdentifier;
import org.springframework.roo.classpath.PhysicalTypeMetadata;
import org.springframework.roo.classpath.PhysicalTypeMetadataProvider;
import org.springframework.roo.classpath.details.ClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.DefaultClassOrInterfaceTypeDetails;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadata;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.DefaultAnnotationMetadata;
import org.springframework.roo.classpath.itd.ItdMetadataScanner;
import org.springframework.roo.classpath.operations.ClasspathOperations;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataItem;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.model.JavaPackage;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.project.Dependency;
import org.springframework.roo.project.Path;
import org.springframework.roo.project.PathResolver;
import org.springframework.roo.project.Plugin;
import org.springframework.roo.project.ProjectMetadata;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.ProjectType;
import org.springframework.roo.project.Repository;
import org.springframework.roo.support.osgi.UrlFindingUtils;
import org.springframework.roo.support.util.Assert;
import org.springframework.roo.support.util.FileCopyUtils;
import org.springframework.roo.support.util.TemplateUtils;
import org.springframework.roo.support.util.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of flex operations that are available via the Roo shell.
 * 
 * @author Jeremy Grelle
 * @since 1.0
 */
@Component
@Service
public class FlexOperationsImpl implements FlexOperations {

    private static final String TEMPLATE_PATH = FlexOperationsImpl.class.getPackage().getName().replace(".", "/");

    @Reference
    private FileManager fileManager;

    @Reference
    private MetadataService metadataService;

    @Reference
    private ProjectOperations projectOperations;

    @Reference
    private WebMvcOperations webMvcOperations;

    @Reference
    private ClasspathOperations classpathOperations;

    @Reference
    private FlexPathResolver flexPathResolver;

    @Reference
    private PhysicalTypeMetadataProvider physicalTypeMetadataProvider;

    @Reference
    private ItdMetadataScanner itdMetadataScanner;

    @Reference
    private MetadataDependencyRegistry dependencyRegistry;

    private ComponentContext context;

    private StringTemplateGroup templateGroup;

    protected void activate(ComponentContext context) {
        this.context = context;
        this.templateGroup = new StringTemplateGroup("flexOperationsTemplateGroup");
    }

    public void installFlex() {
        createServicesConfig();
        createFlexConfig();
        updateDependencies();
        configureFlexBuild();
        createScaffoldApp();
        createFlexCompilerConfig();
    }

    public boolean isFlexAvailable() {
        return getPathResolver() != null;
    }

    public void createScaffoldApp() {
        ProjectMetadata projectMetadata = (ProjectMetadata) this.metadataService.get(ProjectMetadata.getProjectIdentifier());
        String scaffoldAppFileId = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, projectMetadata.getProjectName() + "_scaffold.mxml");
        String presentationPackage = projectMetadata.getTopLevelPackage() + ".presentation";
        StringTemplate scaffoldTemplate = this.templateGroup.getInstanceOf(TEMPLATE_PATH + "/appname_scaffold");
        scaffoldTemplate.setAttribute("presentationPackage", presentationPackage);
        // TODO - Extract this value from services-config.xml?
        scaffoldTemplate.setAttribute("amfRemotingUrl", "http://localhost:8080/" + projectMetadata.getProjectName() + "/messagebroker/amf");
        this.fileManager.createOrUpdateTextFileIfRequired(scaffoldAppFileId, scaffoldTemplate.toString());

        // Create the HTML wrapper
        String htmlWrapperFileId = getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, projectMetadata.getProjectName() + "_scaffold.html");
        StringTemplate htmlWrapperTemplate = this.templateGroup.getInstanceOf(TEMPLATE_PATH + "/appname_scaffold_html");
        htmlWrapperTemplate.setAttribute("projectName", projectMetadata.getProjectName());
        this.fileManager.createOrUpdateTextFileIfRequired(htmlWrapperFileId, htmlWrapperTemplate.toString());

        copyDirectoryContents("htmlwrapper/*.*", getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, "/"));
        copyDirectoryContents("htmlwrapper/history/*.*", getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, "/history"));
        copyDirectoryContents("flashbuilder/html-template/*.*", getPathResolver().getIdentifier(Path.ROOT, "/html-template"));
        copyDirectoryContents("htmlwrapper/history/*.*", getPathResolver().getIdentifier(Path.ROOT, "/html-template/history"));
    }

    public void createFlexCompilerConfig() {
        ProjectMetadata projectMetadata = (ProjectMetadata) this.metadataService.get(ProjectMetadata.getProjectIdentifier());
        String compilerConfigFileId = this.flexPathResolver.getIdentifier(FlexPath.SRC_MAIN_FLEX, projectMetadata.getProjectName()
            + "_scaffold-config.xml");
        try {
            if (!this.fileManager.exists(compilerConfigFileId)) {
                FileCopyUtils.copy(TemplateUtils.getTemplate(getClass(), "flex-compiler-config.xml"), this.fileManager.createFile(
                    compilerConfigFileId).getOutputStream());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public void generateAll(JavaPackage javaPackage) {
        FileDetails srcRoot = new FileDetails(new File(getPathResolver().getRoot(Path.SRC_MAIN_JAVA)), null);
        String antPath = getPathResolver().getRoot(Path.SRC_MAIN_JAVA) + File.separatorChar + "**" + File.separatorChar + "*.java";
        SortedSet<FileDetails> entries = this.fileManager.findMatchingAntPath(antPath);

        each_file: for (FileDetails file : entries) {
            String fullPath = srcRoot.getRelativeSegment(file.getCanonicalPath());
            fullPath = fullPath.substring(1, fullPath.lastIndexOf(".java")).replace(File.separatorChar, '.'); // ditch
                                                                                                              // the
                                                                                                              // first /
                                                                                                              // and
                                                                                                              // .java
            JavaType javaType = new JavaType(fullPath);
            String id = this.physicalTypeMetadataProvider.findIdentifier(javaType);
            if (id != null) {
                PhysicalTypeMetadata ptm = (PhysicalTypeMetadata) this.metadataService.get(id);
                if (ptm == null || ptm.getPhysicalTypeDetails() == null || !(ptm.getPhysicalTypeDetails() instanceof ClassOrInterfaceTypeDetails)) {
                    continue;
                }

                ClassOrInterfaceTypeDetails cid = (ClassOrInterfaceTypeDetails) ptm.getPhysicalTypeDetails();
                if (Modifier.isAbstract(cid.getModifier())) {
                    continue;
                }

                Set<MetadataItem> metadata = this.itdMetadataScanner.getMetadata(id);
                for (MetadataItem item : metadata) {
                    if (item instanceof EntityMetadata) {
                        EntityMetadata em = (EntityMetadata) item;
                        Set<String> downstream = this.dependencyRegistry.getDownstream(em.getId());
                        // check to see if this entity metadata has a web scaffold metadata listening to it
                        for (String ds : downstream) {
                            if (FlexScaffoldMetadata.isValid(ds)) {
                                // there is already a controller for this entity
                                continue each_file;
                            }
                        }
                        // to get here, there is no listening controller, so add on
                        JavaType service = new JavaType(javaPackage.getFullyQualifiedPackageName() + "." + javaType.getSimpleTypeName() + "Service");
                        JavaType entity = javaType;
                        createRemotingDestination(service, entity);
                        break;
                    }
                }
            }
        }
        return;
    }

    public void createRemotingDestination(JavaType service, JavaType entity) {
        Assert.notNull(service, "Remoting Destination Java Type required");
        Assert.notNull(entity, "Entity Java Type required");

        String resourceIdentifier = this.classpathOperations.getPhysicalLocationCanonicalPath(service, Path.SRC_MAIN_JAVA);

        // create annotation @RooFlexScaffold
        List<AnnotationAttributeValue<?>> rooFlexScaffoldAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        rooFlexScaffoldAttributes.add(new ClassAttributeValue(new JavaSymbolName("entity"), entity));
        AnnotationMetadata atRooFlexScaffold = new DefaultAnnotationMetadata(new JavaType(RooFlexScaffold.class.getName()), rooFlexScaffoldAttributes);

        // create annotation @RemotingDestination
        List<AnnotationAttributeValue<?>> remotingDestinationAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        AnnotationMetadata atRemotingDestination = new DefaultAnnotationMetadata(
            new JavaType("org.springframework.flex.remoting.RemotingDestination"), remotingDestinationAttributes);

        // create annotation @Service
        List<AnnotationAttributeValue<?>> serviceAttributes = new ArrayList<AnnotationAttributeValue<?>>();
        AnnotationMetadata atService = new DefaultAnnotationMetadata(new JavaType("org.springframework.stereotype.Service"), serviceAttributes);

        String declaredByMetadataId = PhysicalTypeIdentifier.createIdentifier(service, getPathResolver().getPath(resourceIdentifier));
        List<AnnotationMetadata> annotations = new ArrayList<AnnotationMetadata>();
        annotations.add(atRooFlexScaffold);
        annotations.add(atRemotingDestination);
        annotations.add(atService);
        ClassOrInterfaceTypeDetails details = new DefaultClassOrInterfaceTypeDetails(declaredByMetadataId, service, Modifier.PUBLIC,
            PhysicalTypeCategory.CLASS, annotations);

        this.classpathOperations.generateClassFile(details);

        ActionScriptType asType = ActionScriptMappingUtils.toActionScriptType(entity);

        // Trigger creation of corresponding ActionScript entities
        this.metadataService.get(ActionScriptEntityMetadata.createTypeIdentifier(asType, FlexPath.SRC_MAIN_FLEX));
    }

    private void createServicesConfig() {
        String servicesConfigFilename = "WEB-INF/flex/services-config.xml";

        if (this.fileManager.exists(getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, servicesConfigFilename))) {
            // file exists, so nothing to do
            return;
        }

        try {
            FileCopyUtils.copy(TemplateUtils.getTemplate(getClass(), "services-config-template.xml"), this.fileManager.createFile(
                getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, servicesConfigFilename)).getOutputStream());
        } catch (IOException e) {
            new IllegalStateException("Encountered an error during copying of resources for maven addon.", e);
        }
    }

    private void createFlexConfig() {

        String flexConfigFilename = "/WEB-INF/spring/flex-config.xml";

        try {
            if (!this.fileManager.exists(getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, flexConfigFilename))) {
                FileCopyUtils.copy(TemplateUtils.getTemplate(getClass(), "flex-config.xml"), this.fileManager.createFile(
                    getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, flexConfigFilename)).getOutputStream());
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        // TODO - possible to do this without hardcoding the path?
        // adjust MVC config to accommodate Spring Flex
        String mvcContextPath = getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/spring/webmvc-config.xml");
        MutableFile mvcContextMutableFile = null;

        Document mvcAppCtx;
        try {
            if (!this.fileManager.exists(mvcContextPath)) {
                this.webMvcOperations.installAllWebMvcArtifacts();
            }
            mvcContextMutableFile = this.fileManager.updateFile(mvcContextPath);
            mvcAppCtx = XmlUtils.getDocumentBuilder().parse(mvcContextMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element root = mvcAppCtx.getDocumentElement();

        if (null == XmlUtils.findFirstElement("/beans/import[@resource='flex-config.xml']", root)) {
            Element importFlex = mvcAppCtx.createElement("import");
            importFlex.setAttribute("resource", "flex-config.xml");
            root.appendChild(importFlex);
            XmlUtils.writeXml(mvcContextMutableFile.getOutputStream(), mvcAppCtx);
        }

        // TODO - possible to do this without hardcoding the path?
        // TODO - should really be updating the existing urlrewrite file, but it seems urlrewrite may go away so this is
        // just temporary anyway
        // modify URLRewrite config to allow simple static resources to be served properly
        String urlRewriteFileId = getPathResolver().getIdentifier(Path.SRC_MAIN_WEBAPP, "/WEB-INF/urlrewrite.xml");
        InputStreamReader templateReader = new InputStreamReader(TemplateUtils.getTemplate(getClass(), "urlrewrite-template.xml"));
        Assert.notNull(templateReader, "Could not acquire urlrewrite-template.xml file");
        try {
            this.fileManager.createOrUpdateTextFileIfRequired(urlRewriteFileId, FileCopyUtils.copyToString(templateReader));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void updateDependencies() {
        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(), "dependencies.xml");
        Assert.notNull(templateInputStream, "Could not acquire dependencies.xml file");
        Document dependencyDoc;
        try {
            dependencyDoc = XmlUtils.getDocumentBuilder().parse(templateInputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element dependenciesElement = (Element) dependencyDoc.getFirstChild();

        List<Element> flexDependencies = XmlUtils.findElements("/dependencies/springFlex/dependency", dependenciesElement);
        for (Element dependency : flexDependencies) {
            this.projectOperations.dependencyUpdate(new Dependency(dependency));
        }

        fixBrokenFlexDependency();

        this.projectOperations.updateProjectType(ProjectType.WAR);
    }

    // TODO - ProjectMetadata doesn't support all artifact types, in this case "pom" is needed for flex-framework - this
    // ultimately should be fixed in Roo itself
    private void fixBrokenFlexDependency() {
        String pomPath = getPathResolver().getIdentifier(Path.ROOT, "pom.xml");
        MutableFile pomMutableFile = null;

        Document pomDoc;
        try {
            pomMutableFile = this.fileManager.updateFile(pomPath);
            pomDoc = XmlUtils.getDocumentBuilder().parse(pomMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element typeElement = XmlUtils.findFirstElement("/project/dependencies/dependency[artifactId='flex-framework']/type",
            pomDoc.getDocumentElement());
        Assert.notNull(typeElement, "Could not find the flex-framework dependency type.");
        typeElement.setTextContent("pom");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlUtils.writeXml(XmlUtils.createIndentingTransformer(), byteArrayOutputStream, pomDoc);
        String mxmlContent = byteArrayOutputStream.toString();

        try {
            FileCopyUtils.copy(mxmlContent, new OutputStreamWriter(pomMutableFile.getOutputStream()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // TODO - ProjectMetadata doesn't support all artifact types, in this case "pom" is needed for flex-compiler - this
    // ultimately should be fixed in Roo itself
    // TODO - The plugin metamodel doesn't support configuration per-execution - this ultimately should be fixed in Roo
    // itself
    private void fixBrokenFlexPlugin() {
        String pomPath = getPathResolver().getIdentifier(Path.ROOT, "pom.xml");
        MutableFile pomMutableFile = null;

        Document pomDoc;
        try {
            pomMutableFile = this.fileManager.updateFile(pomPath);
            pomDoc = XmlUtils.getDocumentBuilder().parse(pomMutableFile.getInputStream());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element pluginDependencyElement = XmlUtils.findFirstElement(
            "/project/build/plugins/plugin[artifactId='flexmojos-maven-plugin']/dependencies/dependency", pomDoc.getDocumentElement());
        Assert.notNull(pluginDependencyElement, "Could not find the flexmojos-maven-plugin's dependency element.");
        Element newTypeNode = pomDoc.createElement("type");
        newTypeNode.setTextContent("pom");
        pluginDependencyElement.appendChild(newTypeNode);

        Element pluginExecutionElement = XmlUtils.findFirstElement(
            "/project/build/plugins/plugin[artifactId='flexmojos-maven-plugin']/executions/execution", pomDoc.getDocumentElement());
        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(), "pluginsFix.xml");
        Document configurationTemplate;
        try {
            configurationTemplate = XmlUtils.getDocumentBuilder().parse(templateInputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        Element configurationElement = XmlUtils.findFirstElement("/configuration", configurationTemplate.getDocumentElement());
        Assert.notNull(configurationElement, "flexmojos-maven-plugin configuration did not parse as expected.");

        pluginExecutionElement.appendChild(pomDoc.importNode(configurationElement, true));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        XmlUtils.writeXml(XmlUtils.createIndentingTransformer(), byteArrayOutputStream, pomDoc);
        String mxmlContent = byteArrayOutputStream.toString();

        try {
            FileCopyUtils.copy(mxmlContent, new OutputStreamWriter(pomMutableFile.getOutputStream()));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void configureFlexBuild() {
        ProjectMetadata projectMetadata = (ProjectMetadata) this.metadataService.get(ProjectMetadata.getProjectIdentifier());

        Repository externalRepository = new Repository("spring-external", "Spring External Repository", "http://maven.springframework.org/external");
        if (!projectMetadata.isRepositoryRegistered(externalRepository)) {
            this.projectOperations.addRepository(externalRepository);
        }
        
        Repository flexRepository = new Repository("flex", "Sonatype Flex Repo", "http://repository.sonatype.org/content/groups/flexgroup");
        if (!projectMetadata.isRepositoryRegistered(flexRepository)) {
            this.projectOperations.addRepository(flexRepository);
        }

        InputStream templateInputStream = TemplateUtils.getTemplate(getClass(), "plugins.xml");
        Assert.notNull(templateInputStream, "Could not acquire plugins.xml file");
        Document pluginDoc;
        try {
            pluginDoc = XmlUtils.getDocumentBuilder().parse(templateInputStream);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }

        Element pluginsElement = (Element) pluginDoc.getFirstChild();

        List<Element> flexPlugins = XmlUtils.findElements("/plugins/springFlex/plugin", pluginsElement);
        for (Element pluginElement : flexPlugins) {
            // TODO - this is a temporary hack - currently it's the only easy way to update an existing plugin
            Plugin flexPlugin = new Plugin(pluginElement);
            if (projectMetadata.isBuildPluginRegistered(flexPlugin)) {
                this.projectOperations.removeBuildPlugin(flexPlugin);
            }
            this.projectOperations.addBuildPlugin(flexPlugin);
        }

        fixBrokenFlexPlugin();

        String flexPropertiesFileId = getPathResolver().getIdentifier(Path.ROOT, ".flexProperties");
        if (!this.fileManager.exists(flexPropertiesFileId)) {
            StringTemplate flexPropertiesTemplate = this.templateGroup.getInstanceOf(TEMPLATE_PATH + "/flex_properties");
            flexPropertiesTemplate.setAttribute("projectName", projectMetadata.getProjectName());
            this.fileManager.createOrUpdateTextFileIfRequired(flexPropertiesFileId, flexPropertiesTemplate.toString());
        }

        String actionScriptPropertiesFiledId = getPathResolver().getIdentifier(Path.ROOT, ".actionScriptProperties");
        if (!this.fileManager.exists(actionScriptPropertiesFiledId)) {
            StringTemplate actionScriptPropertiesTemplate = this.templateGroup.getInstanceOf(TEMPLATE_PATH + "/actionscript_properties");
            actionScriptPropertiesTemplate.setAttribute("projectName", projectMetadata.getProjectName());
            actionScriptPropertiesTemplate.setAttribute("projectUUID", UUID.randomUUID());
            this.fileManager.createOrUpdateTextFileIfRequired(actionScriptPropertiesFiledId, actionScriptPropertiesTemplate.toString());
        }
    }

    /**
     * @return the path resolver or null if there is no user project
     */
    private PathResolver getPathResolver() {
        ProjectMetadata projectMetadata = (ProjectMetadata) this.metadataService.get(ProjectMetadata.getProjectIdentifier());
        if (projectMetadata == null) {
            return null;
        }
        return projectMetadata.getPathResolver();
    }

    /**
     * This method will copy the contents of a directory to another if the resource does not already exist in the target
     * directory
     * 
     * @param sourceAntPath directory
     * @param target directory
     */
    private void copyDirectoryContents(String sourceAntPath, String targetDirectory) {
        Assert.hasText(sourceAntPath, "Source path required");
        Assert.hasText(targetDirectory, "Target directory required");

        if (!targetDirectory.endsWith("/")) {
            targetDirectory += "/";
        }

        if (!this.fileManager.exists(targetDirectory)) {
            this.fileManager.createDirectory(targetDirectory);
        }

        String path = TemplateUtils.getTemplatePath(getClass(), sourceAntPath);
        Set<URL> urls = UrlFindingUtils.findMatchingClasspathResources(this.context.getBundleContext(), path);
        Assert.notNull(urls, "Could not search bundles for resources for Ant Path '" + path + "'");
        for (URL url : urls) {
            String fileName = url.getPath().substring(url.getPath().lastIndexOf("/") + 1);
            if (!this.fileManager.exists(targetDirectory + fileName)) {
                try {
                    FileCopyUtils.copy(url.openStream(), this.fileManager.createFile(targetDirectory + fileName).getOutputStream());
                } catch (IOException e) {
                    new IllegalStateException("Encountered an error during copying of resources for Flex addon.", e);
                }
            }
        }
    }
}