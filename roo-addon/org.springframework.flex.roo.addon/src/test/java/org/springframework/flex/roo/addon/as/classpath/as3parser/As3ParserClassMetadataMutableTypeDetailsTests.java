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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifier;
import org.springframework.flex.roo.addon.as.classpath.details.ASMutableClassOrInterfaceTypeDetails;
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
import org.springframework.roo.metadata.MetadataDependencyRegistry;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.ActiveProcessManager;
import org.springframework.roo.process.manager.MutableFile;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.process.manager.internal.DefaultFileManager;
import org.springframework.roo.project.PathInformation;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.StringUtils;

import uk.co.badgersinfoil.metaas.ActionScriptFactory;
import uk.co.badgersinfoil.metaas.dom.ASClassType;
import uk.co.badgersinfoil.metaas.dom.ASCompilationUnit;
import uk.co.badgersinfoil.metaas.dom.ASMetaTag;
import uk.co.badgersinfoil.metaas.dom.ASMethod;


public class As3ParserClassMetadataMutableTypeDetailsTests {

	private static final Log log = LogFactory.getLog(As3ParserClassMetadataMutableTypeDetailsTests.class);

	private TestFileManager fileManager;
	
	@Mock
	private MetadataService metadataService;
	
	@Mock
	private MetadataDependencyRegistry registry;
	
	@Mock
	private ProcessManager processManager;
	
	@Mock
	private MutableFile updateFile;
		
	private FlexPathResolver pathResolver;

	private String metadataId;
	
	private As3ParserMetadataProvider provider;
	
	private ActionScriptFactory factory = new ActionScriptFactory();

	private As3ParserClassMetadata metadata;

	private ASMutableClassOrInterfaceTypeDetails details;
	
	private ByteArrayOutputStream outputStream;
	
	private String lastFile;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(processManager.isDevelopmentMode()).thenReturn(true);
		ActiveProcessManager.setActiveProcessManager(processManager);
		String fileIdentifier = new ClassPathResource("com/foo/stuff/FooImpl.as").getFile().getCanonicalPath();
		metadataId = "MID:"+ASPhysicalTypeIdentifier.class.getName()+"#SRC_MAIN_FLEX?com.foo.stuff.FooImpl";
		
		fileManager = new TestFileManager();
		pathResolver = new TestPathResolver();
		
		provider = new As3ParserMetadataProvider();
		ReflectionTestUtils.setField(provider, "fileManager", fileManager);
		ReflectionTestUtils.setField(provider, "metadataService", metadataService);
		ReflectionTestUtils.setField(provider, "metadataDependencyRegistry", registry);
		ReflectionTestUtils.setField(provider, "pathResolver", pathResolver);
		
		metadata = new As3ParserClassMetadata(fileManager, fileIdentifier, metadataId, metadataService, provider);
		assertNotNull(metadata);
		assertNotNull(metadata.getPhysicalTypeDetails());
		details = (ASMutableClassOrInterfaceTypeDetails) metadata.getPhysicalTypeDetails();
		
		lastFile = "";
		outputStream = new ByteArrayOutputStream();
		
		when(updateFile.getOutputStream()).thenReturn(outputStream);
	}
	
	@After
	public void logFileContents() {
		if (StringUtils.hasText(lastFile)){
			if (log.isDebugEnabled()) {
				log.debug("\n"+lastFile);
			}
		}
	}
	
	@Test
	public void testAddSimpleField() throws UnsupportedEncodingException {
		
		ASFieldMetadata fieldMetadata = new DefaultASFieldMetadata(metadataId, new ActionScriptType("String"), 
				new ActionScriptSymbolName("name"), ASTypeVisibility.PRIVATE, null, null);
		
		details.addField(fieldMetadata);
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		assertTrue(compUnit.getType() instanceof ASClassType);
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getField("name"));
	}
	
	@Test
	public void testAddSimpleFieldWithInitializer() throws UnsupportedEncodingException {
		
		ASFieldMetadata fieldMetadata = new DefaultASFieldMetadata(metadataId, ActionScriptType.NUMBER_TYPE, 
				new ActionScriptSymbolName("id"), ASTypeVisibility.PRIVATE, "-1", null);
		
		details.addField(fieldMetadata);
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		assertTrue(compUnit.getType() instanceof ASClassType);
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getField("id"));
		assertEquals("-1", clazz.getField("id").getInitializer().toString());
	}
	
	@Test
	public void testAddComplexField() throws UnsupportedEncodingException {
		
		List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();
		ASMetaTagMetadata metaTag = new DefaultASMetaTagMetadata("Bindable", null);
		metaTags.add(metaTag);
		
		ASFieldMetadata fieldMetadata = new DefaultASFieldMetadata(metadataId, new ActionScriptType("com.foo.other.Baz"), 
				new ActionScriptSymbolName("baz"), ASTypeVisibility.PRIVATE, null, metaTags);
		
		details.addField(fieldMetadata);
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		assertTrue(compUnit.getType() instanceof ASClassType);
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getField("baz"));
		assertEquals("Baz",clazz.getField("baz").getType());
		assertTrue(compUnit.getPackage().findImports().contains("com.foo.other.Baz"));
	}
	
	@Test 
	public void testRemoveField() throws UnsupportedEncodingException {
		
		details.removeField(new ActionScriptSymbolName("field1"));
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		assertTrue(compUnit.getType() instanceof ASClassType);
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNull(clazz.getField("field1"));
	}
	
	@Test
	public void testAddSimpleMethod() throws UnsupportedEncodingException {
		
		ASMethodMetadata method = new DefaultASMethodMetadata(metadataId, new ActionScriptSymbolName("doStuff"), ActionScriptType.VOID_TYPE, ASTypeVisibility.PUBLIC);
		
		details.addMethod(method);
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		ASClassType clazz = (ASClassType) compUnit.getType();
		assertNotNull(clazz.getMethod("doStuff"));
		ASMethod result = clazz.getMethod("doStuff");
		assertEquals("void", result.getType());
		assertEquals(0, result.getArgs().size());
	}
	
	@Test
	public void testAddComplexMethod() throws UnsupportedEncodingException { 
		List<ASMetaTagMetadata> metaTags = new ArrayList<ASMetaTagMetadata>();
		ASMetaTagMetadata metaTag = new DefaultASMetaTagMetadata("MagicalMetadata", null);
		metaTags.add(metaTag);
		
		List<ActionScriptType> paramTypes = new ArrayList<ActionScriptType>();
		paramTypes.add(new ActionScriptType("String"));
		paramTypes.add(new ActionScriptType("com.foo.other.Bar"));
		
		List<ActionScriptSymbolName> paramNames = new ArrayList<ActionScriptSymbolName>();
		paramNames.add(new ActionScriptSymbolName("arg1"));
		paramNames.add(new ActionScriptSymbolName("arg2"));
		
		ASMethodMetadata method = new DefaultASMethodMetadata(metadataId, new ActionScriptSymbolName("doStuff"), new ActionScriptType("com.foo.smothered.Covered"), 
				ASTypeVisibility.PRIVATE, "", metaTags, paramTypes, paramNames);
		
		details.addMethod(method);
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
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
	public void testAddTypeMetaTag() throws UnsupportedEncodingException {
		List <MetaTagAttributeValue<?>> attributes = new ArrayList<MetaTagAttributeValue<?>>();
		attributes.add(new StringAttributeValue(new ActionScriptSymbolName("alias"), "com.foo.stuff.domain.FooImpl"));
		ASMetaTagMetadata metaTag = new DefaultASMetaTagMetadata("RemoteClass", attributes);
		
		details.addTypeMetaTag(metaTag);
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		assertNotNull(compUnit.getType().getFirstMetatag("RemoteClass"));
		ASMetaTag tag = compUnit.getType().getFirstMetatag("RemoteClass");
		assertEquals("com.foo.stuff.domain.FooImpl", tag.getParamValue("alias"));
	}
	
	@Test
	@Ignore
	public void testRemoveTypeMetatag() throws UnsupportedEncodingException {
		details.removeTypeMetaTag("ClassLevelTag1");
		
		readLastFile();
		assertTrue(StringUtils.hasText(lastFile));
		ASCompilationUnit compUnit = factory.newParser().parse(new StringReader(lastFile));
		assertNull(compUnit.getType().getFirstMetatag("ClassLevelTag1"));
	}
	
	private void readLastFile() throws UnsupportedEncodingException {
		this.lastFile = this.outputStream.toString(Charset.defaultCharset().toString());
	}
	
	private static class TestPathResolver extends FlexMojosPathResolver {
		
		public TestPathResolver() throws IOException {
			File file = new ClassPathResource("").getFile();
			getPathInformation().add(new PathInformation(FlexPath.SRC_MAIN_FLEX, true, file));
			init();
		}
	}
	
	private class TestFileManager extends DefaultFileManager {
		@Override
		public MutableFile updateFile(String fileIdentifier) {
			return updateFile;
		}
	}
}
