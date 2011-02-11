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
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.core.io.ClassPathResource;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeCategory;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeIdentifier;
import org.springframework.flex.roo.addon.as.classpath.ASPhysicalTypeMetadataProvider;
import org.springframework.flex.roo.addon.as.classpath.details.ASConstructorMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASFieldMetadata;
import org.springframework.flex.roo.addon.as.classpath.details.ASMethodMetadata;
import org.springframework.flex.roo.addon.as.model.ASTypeVisibility;
import org.springframework.flex.roo.addon.as.model.ActionScriptSymbolName;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.file.monitor.event.FileDetails;
import org.springframework.roo.metadata.MetadataService;
import org.springframework.roo.process.manager.ActiveProcessManager;
import org.springframework.roo.process.manager.FileManager;
import org.springframework.roo.process.manager.ProcessManager;
import org.springframework.roo.process.manager.internal.DefaultFileManager;

public class As3ParserClassMetadataValidParsingTests {

	FileManager fileManager = new DefaultFileManager();
	
	@Mock
	MetadataService metadataService;
	
	@Mock
	ASPhysicalTypeMetadataProvider provider;
	
	@Mock
	ProcessManager processManager;
	
	@Mock
	FileDetails fileDetails;

	private String metadataId;

	private As3ParserClassMetadata metadata;

	private As3ParserMutableClassOrInterfaceTypeDetails details;
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(processManager.isDevelopmentMode()).thenReturn(true);
		ActiveProcessManager.setActiveProcessManager(processManager);
		String fileIdentifier = new ClassPathResource("com/foo/stuff/FooImpl.as").getFile().getCanonicalPath();
		metadataId = "MID:"+ASPhysicalTypeIdentifier.class.getName()+"#SRC_MAIN_FLEX?com.foo.stuff.FooImpl";
		
		metadata = new As3ParserClassMetadata(fileManager, fileIdentifier, metadataId, metadataService, provider);
		assertNotNull(metadata);
		assertNotNull(metadata.getPhysicalTypeDetails());
		details = (As3ParserMutableClassOrInterfaceTypeDetails) metadata.getPhysicalTypeDetails();
	}

	@Test
	public void testPackageAndClassDetails() {
		//Check the package and class details
		assertNotNull(details.getName());
		assertEquals("com.foo.stuff", details.getName().getPackage().getFullyQualifiedPackageName());
		assertEquals(details.getCompilationUnitPackage(), details.getName().getPackage());
		assertEquals("com.foo.stuff.FooImpl", details.getName().getFullyQualifiedTypeName());
		assertEquals("FooImpl", details.getName().getSimpleTypeName());
		assertEquals(ASPhysicalTypeCategory.CLASS, details.getPhysicalTypeCategory());
		assertEquals(2, details.getDeclaredFields().size());
		assertEquals(3, details.getDeclaredMethods().size());
		assertEquals(1, details.getImplementsTypes().size());
		assertEquals("com.foo.Foo", details.getImplementsTypes().get(0).getFullyQualifiedTypeName());
		assertEquals(1, details.getExtendsTypes().size());
		assertEquals("com.foo.Alpha", details.getExtendsTypes().get(0).getFullyQualifiedTypeName());
	}
	
	@Test
	public void testImports() {
		assertEquals(3, details.getImports().size());
		assertEquals("com.foo.Alpha", details.getImports().get(0));
		assertEquals("com.foo.Bar", details.getImports().get(1));
		assertEquals("com.foo.Foo", details.getImports().get(2));
	}
	
	@Test
	public void testClassLevelMetadata() {
		assertEquals(2, details.getTypeMetaTags().size());
		assertEquals("ClassLevelTag1", details.getTypeMetaTags().get(0).getName());
		assertEquals(0,details.getTypeMetaTags().get(0).getAttributeNames().size());
		assertEquals("ClassLevelTag2", details.getTypeMetaTags().get(1).getName());
		assertEquals(1,details.getTypeMetaTags().get(1).getAttributeNames().size());
		assertEquals("bar",details.getTypeMetaTags().get(1).getAttribute(new ActionScriptSymbolName("foo")).getValue());
	}
	
	@Test
	public void testImplicitTypeFieldWithMetatag() {
		ASFieldMetadata field = details.getDeclaredFields().get(0);
		assertEquals(metadataId, field.getDeclaredByMetadataId());
		assertEquals("field1", field.getFieldName().getSymbolName());
		assertEquals("Field1", field.getFieldName().getSymbolNameCapitalisedFirstLetter());
		assertEquals(ASTypeVisibility.PUBLIC, field.getVisibility());
		assertEquals("String", field.getFieldType().getFullyQualifiedTypeName());
		assertEquals(1, field.getMetaTags().size());
		assertEquals("FieldLevelTag1", field.getMetaTags().get(0).getName());
	}
	
	@Test 
	public void testImportedTypeField() {
		ASFieldMetadata field = details.getDeclaredFields().get(1);
		assertEquals(metadataId, field.getDeclaredByMetadataId());
		assertEquals("field2", field.getFieldName().getSymbolName());
		assertEquals("Field2", field.getFieldName().getSymbolNameCapitalisedFirstLetter());
		assertEquals(ASTypeVisibility.PRIVATE, field.getVisibility());
		assertEquals("com.foo.Bar", field.getFieldType().getFullyQualifiedTypeName());
		assertEquals(0, field.getMetaTags().size());
	}
	
	@Test
	public void testConstructors() {
		assertNotNull(details.getDeclaredConstructor());
		ASConstructorMetadata constructor = details.getDeclaredConstructor();
		assertEquals(metadataId, constructor.getDeclaredByMetadataId());
	}
	
	@Test
	public void testPrivateVoidMethodWithMetatag() {
		ASMethodMetadata method = details.getDeclaredMethods().get(0);
		assertEquals(metadataId, method.getDeclaredByMetadataId());
		assertEquals("method1", method.getMethodName().getSymbolName());
		assertEquals(ASTypeVisibility.PRIVATE, method.getVisibility());
		assertEquals(0, method.getParameterNames().size());
		assertEquals(ActionScriptType.VOID_TYPE, method.getReturnType());
		assertEquals(1, method.getMetaTags().size());
		assertEquals("MethodLevelTag1", method.getMetaTags().get(0).getName());
	}
	
	@Test
	public void testPublicFactoryMethod() {
		ASMethodMetadata method = details.getDeclaredMethods().get(1);
		assertEquals(metadataId, method.getDeclaredByMetadataId());
		assertEquals("fooFactory", method.getMethodName().getSymbolName());
		assertEquals(ASTypeVisibility.PUBLIC, method.getVisibility());
		assertEquals(0, method.getParameterNames().size());
		assertEquals("com.foo.stuff.FooImpl", method.getReturnType().getFullyQualifiedTypeName());
		assertEquals(0, method.getMetaTags().size());
	}
	
	@Test
	public void testPublicVoidMethodWithParams() {
		ASMethodMetadata method = details.getDeclaredMethods().get(2);
		assertEquals(metadataId, method.getDeclaredByMetadataId());
		assertEquals("calculateStuff", method.getMethodName().getSymbolName());
		assertEquals(ASTypeVisibility.PUBLIC, method.getVisibility());
		assertEquals(2, method.getParameterNames().size());
		assertEquals("bar", method.getParameterNames().get(0).getSymbolName());
		assertEquals("String", method.getParameterTypes().get(0).getFullyQualifiedTypeName());
		assertEquals("baz", method.getParameterNames().get(1).getSymbolName());
		assertEquals("com.foo.Bar", method.getParameterTypes().get(1).getFullyQualifiedTypeName());
		assertEquals("String", method.getReturnType().getFullyQualifiedTypeName());
		assertEquals(0, method.getMetaTags().size());
	}
}
