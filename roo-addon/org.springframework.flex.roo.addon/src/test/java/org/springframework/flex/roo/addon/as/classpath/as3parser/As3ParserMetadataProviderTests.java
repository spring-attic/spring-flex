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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.*;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeCategory;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifier;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASConstructorMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASClassOrInterfaceTypeDetails;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASPhysicalTypeMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASConstructorMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.DefaultASMethodMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASMethodMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.DefaultASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.MetaTagAttributeValue;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.ASMetaTagMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.metatag.StringAttributeValue;
import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.flex.roo.addon.mojos.FlexMojosPathResolver;
import org.springframework.flex.roo.addon.mojos.FlexPath;
import org.springframework.flex.roo.addon.mojos.FlexPathResolver;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.file.monitor.event.FileEvent;
import org.springframework.roo.file.monitor.event.FileOperation;
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.ActiveProcessManager;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.process.manager.internal.DefaultFileManager;
import org.springframework.roo.project.PathInformation;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import uk.co.badgersinfoil.metaas.ActionScriptFactory;
import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASInterfaceType;
import uk.co.badgersinfoil.metaas.dom.ASMetaTag;
import uk.co.badgersinfoil.metaas.dom.ASMethod;

public class As3ParserMetadataProviderTests {
	
	private static final Log log = LogFactory.getLog(As3ParserMetadataProviderTests.class);

	private TestFileManager fileManager;
	
	@Mock
	private MetadataService metadataService;
	
	@Mock
	private MetadataDependencyRegistry registry;
	
	@Mock
	private ProcessManager processManager;
		
	private FlexPathResolver pathResolver;

	private String metadataId;
	
	private As3ParserMetadataProvider provider;
	
	private ActionScriptFactory factory = new ActionScriptFactory();
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(processManager.isDevelopmentMode()).thenReturn(true);
		ActiveProcessManager.setActiveProcessManager(processManager);
		metadataId = "MID:"+ASPhysicalTypeIdentifier.class.getName()+"#SRC_MAIN_FLEX?com.foo.stuff.FooImpl";
		
		fileManager = new TestFileManager();
		pathResolver = new TestPathResolver();
		
		provider = new As3ParserMetadataProvider();
		ReflectionTestUtils.setField(provider, "fileManager", fileManager);
		ReflectionTestUtils.setField(provider, "metadataService", metadataService);
		ReflectionTestUtils.setField(provider, "metadataDependencyRegistry", registry);
		ReflectionTestUtils.setField(provider, "pathResolver", pathResolver);
	}
	
	@After
	public void logFileContents() {
		if (StringUtils.hasText(fileManager.lastFile)){
			if (log.isDebugEnabled()) {
				log.debug("\n"+fileManager.lastFile);
			}
		}
	}
	
	@Test
	public void testGetWithValidIdentifier() {
		As3ParserClassMetadata metadata = (As3ParserClassMetadata) provider.get(metadataId);
		assertNotNull(metadata);
		assertNotNull(metadata.getPhysicalTypeDetails());
	}
	
	@Test
	public void testCreatePhysicalType_Class() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		assertEquals("com.foo.stuff", compUnit.getPackageName());
		assertTrue(compUnit.getType() instanceof ASClassType);
		assertEquals("FooImpl", compUnit.getType().getName());
	}
	
	@Test
	public void testCreatePhysicalType_Interface() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.Foo"), 
				ASPhysicalTypeCategory.INTERFACE, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		assertEquals("com.foo.stuff", compUnit.getPackageName());
		assertTrue(compUnit.getType() instanceof ASInterfaceType);
		assertEquals("Foo", compUnit.getType().getName());
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithTypeMetaTag() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List <MetaTagAttributeValue<?>> attributes = new ArrayList<MetaTagAttributeValue<?>>();
		attributes.add(new StringAttributeValue(new ActionScriptSymbolName("alias"), "com.foo.stuff.domain.FooImpl"));
		ASMetaTagMetadata remoteClassTag = new DefaultASMetaTagMetadata("RemoteClass", attributes);
		List<ASMetaTagMetadata> typeMetaTags = new ArrayList<ASMetaTagMetadata>();
		typeMetaTags.add(remoteClassTag);
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, typeMetaTags);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		assertNotNull(compUnit.getType().getFirstMetatag("RemoteClass"));
		ASMetaTag tag = compUnit.getType().getFirstMetatag("RemoteClass");
		assertEquals("com.foo.stuff.domain.FooImpl", tag.getParamValue("alias"));
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithSuperclass() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List<ActionScriptType> extendsTypes = new ArrayList<ActionScriptType>();
		extendsTypes.add(new ActionScriptType("com.foo.other.Parent"));
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null, null, null, null, extendsTypes, null, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		assertEquals("com.foo.stuff", compUnit.getPackageName());
		assertTrue(compUnit.getType() instanceof ASClassType);
		assertEquals("FooImpl", compUnit.getType().getName());
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertEquals("Parent", clazz.getSuperclass());
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Parent"));
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithInterfaces() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List<ActionScriptType> implementsTypes = new ArrayList<ActionScriptType>();
		implementsTypes.add(new ActionScriptType("com.foo.other.Parent"));
		implementsTypes.add(new ActionScriptType("com.foo.other.Sibling"));
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null, null, null, null, null, implementsTypes, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		assertEquals("com.foo.stuff", compUnit.getPackageName());
		assertTrue(compUnit.getType() instanceof ASClassType);
		assertEquals("FooImpl", compUnit.getType().getName());
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertEquals("Parent", clazz.getImplementedInterfaces().get(0));
		assertEquals("Sibling", clazz.getImplementedInterfaces().get(1));
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Parent"));
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Sibling"));
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithSimpleConstructor() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		ASConstructorMetadata constructor = new DefaultASConstructorMetadata(metadataId);
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null, constructor, null, null, null, null, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getMethod("FooImpl"));
		assertNull(clazz.getMethod("FooImpl").getType());
		assertEquals(0, clazz.getMethod("FooImpl").getArgs().size());
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithComplexConstructor() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();
		ASMetaTagMetadata metaTag = new DefaultASMetaTagMetadata("Autowired", null);
		metaTags.add(metaTag);
		
		List<ActionScriptType> paramTypes = new ArrayList<ActionScriptType>();
		paramTypes.add(new ActionScriptType("String"));
		paramTypes.add(new ActionScriptType("com.foo.other.Bar"));
		
		List<ActionScriptSymbolName> paramNames = new ArrayList<ActionScriptSymbolName>();
		paramNames.add(new ActionScriptSymbolName("arg1"));
		paramNames.add(new ActionScriptSymbolName("arg2"));
		
		ASConstructorMetadata constructor = new DefaultASConstructorMetadata(metadataId, null, metaTags, paramTypes, paramNames, ASTypeVisibility.PRIVATE);
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null, constructor, null, null, null, null, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getMethod("FooImpl"));
		ASMethod result = clazz.getMethod("FooImpl");
		assertEquals(1, result.getMetaTagsWithName("Autowired").size());
		assertEquals(2, result.getArgs().size());
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Bar"));
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithSimpleMethod() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List<ASMethodMetadata> methods = new ArrayList<ASMethodMetadata>();
		ASMethodMetadata method = new DefaultASMethodMetadata(metadataId, new ActionScriptSymbolName("doStuff"), ActionScriptType.VOID_TYPE, ASTypeVisibility.PUBLIC);
		methods.add(method);
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null, null, methods, null, null, null, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getMethod("doStuff"));
		ASMethod result = clazz.getMethod("doStuff");
		assertEquals("void", result.getType());
		assertEquals(0, result.getArgs().size());
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithComplexMethod() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();
		ASMetaTagMetadata metaTag = new DefaultASMetaTagMetadata("MagicalMetadata", null);
		metaTags.add(metaTag);
		
		List<ActionScriptType> paramTypes = new ArrayList<ActionScriptType>();
		paramTypes.add(new ActionScriptType("String"));
		paramTypes.add(new ActionScriptType("com.foo.other.Bar"));
		
		List<ActionScriptSymbolName> paramNames = new ArrayList<ActionScriptSymbolName>();
		paramNames.add(new ActionScriptSymbolName("arg1"));
		paramNames.add(new ActionScriptSymbolName("arg2"));
		
		List<ASMethodMetadata> methods = new ArrayList<ASMethodMetadata>();
		ASMethodMetadata method = new DefaultASMethodMetadata(metadataId, new ActionScriptSymbolName("doStuff"), new ActionScriptType("com.foo.smothered.Covered"), 
				ASTypeVisibility.PRIVATE, "", metaTags, paramTypes, paramNames);
		methods.add(method);
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, null, null, methods, null, null, null, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getMethod("doStuff"));
		ASMethod result = clazz.getMethod("doStuff");
		assertEquals("Covered", result.getType());
		assertEquals(1, result.getMetaTagsWithName("MagicalMetadata").size());
		assertEquals(2, result.getArgs().size());
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Bar"));
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.smothered.Covered"));
	}
	
	@Test
	public void testCreatePhysicalType_ClassWithFields() throws IOException {
		String fileIdentifier = new ClassPathResource("").getFile().getCanonicalPath()+"com/foo/stuff/FooImpl.as";
		
		List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();
		ASMetaTagMetadata bindableTag = new DefaultASMetaTagMetadata("Bindable", null);
		metaTags.add(bindableTag);
		
		List<ASFieldMetadata> fields = new ArrayList<ASFieldMetadata>();
		ASFieldMetadata field1 = new DefaultASFieldMetadata(metadataId, new ActionScriptType("String"), new ActionScriptSymbolName("name"), null, null, metaTags);
		ASFieldMetadata field2 = new DefaultASFieldMetadata(metadataId, new ActionScriptType("com.foo.other.Brother"), new ActionScriptSymbolName("brother"), ASTypeVisibility.PROTECTED, null, null);
		ASFieldMetadata field3 = new DefaultASFieldMetadata(metadataId, new ActionScriptType("com.foo.stuff.Stuff"), new ActionScriptSymbolName("stuff"), ASTypeVisibility.PRIVATE, null, null);
		fields.add(field1);
		fields.add(field2);
		fields.add(field3);
		
		ASPhysicalTypeDetails details = new DefaultASClassOrInterfaceTypeDetails(metadataId, new ActionScriptType("com.foo.stuff.FooImpl"), 
				ASPhysicalTypeCategory.CLASS, fields, null, null, null, null, null, null);
		ASPhysicalTypeMetadata type = new DefaultASPhysicalTypeMetadata(metadataId, fileIdentifier, details);
		provider.createPhysicalType(type);
		assertTrue(StringUtils.hasText(fileManager.lastFile));
		
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(fileManager.lastFile));
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertEquals(3, clazz.getFields().size());
		assertNotNull(clazz.getField("name").getMetaTagsWithName("Bindable"));
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Brother"));
		assertFalse(compUnit.getPackage().findImports().contains("com.foo.stuff.Stuff"));
	}
	
	@Test
	public void testFindIdentifier() {
		
		String expectedId = "MID:"+ASPhysicalTypeIdentifier.class.getName()+"#SRC_MAIN_FLEX?com.foo.stuff.FooImpl";
		String id = provider.findIdentifier(new ActionScriptType("com.foo.stuff.FooImpl"));
		
		assertTrue(StringUtils.hasText(id));
		assertEquals(expectedId, id);
	}
	
	@Test
	public void testOnFileEvent() throws IOException {
		
		String expectedId = "MID:"+ASPhysicalTypeIdentifier.class.getName()+"#SRC_MAIN_FLEX?com.foo.stuff.FooImpl";
		
		FileDetails details = new FileDetails(new ClassPathResource("com/foo/stuff/FooImpl.as").getFile(), new Date().getTime());
		
		provider.onFileEvent(new FileEvent(details, FileOperation.CREATED, null));
		
		verify(metadataService).evict(expectedId);
		verify(registry).notifyDownstream(expectedId);
	}
	
	private static class TestPathResolver extends FlexMojosPathResolver {
		
		public TestPathResolver() throws IOException {
			File file = new ClassPathResource("").getFile();
			getPathInformation().add(new PathInformation(FlexPath.SRC_MAIN_FLEX, true, file));
			init();
		}
	}
	
	private static class TestFileManager extends DefaultFileManager {

		public String lastFile;
		
		@Override
		public void createOrUpdateTextFileIfRequired(String fileIdentifier,
				String newContents) {
			lastFile = newContents;
		}
	}
}
