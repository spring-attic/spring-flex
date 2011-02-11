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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.stringtemplate.StringTemplate;
import org.antlr.stringtemplate.StringTemplateGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.flex.roo.addon.FlexScaffoldMetadata;
import org.springframework.flex.roo.addon.as.model.ActionScriptType;
import org.springframework.roo.classpath.details.FieldMetadata;
import org.springframework.roo.classpath.details.FieldMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.AnnotationAttributeValue;
import org.springframework.roo.classpath.details.annotations.AnnotationMetadataBuilder;
import org.springframework.roo.classpath.details.annotations.ClassAttributeValue;
import org.springframework.roo.classpath.details.annotations.IntegerAttributeValue;
import org.springframework.roo.model.JavaSymbolName;
import org.springframework.roo.model.JavaType;
import org.springframework.roo.support.util.XmlUtils;
import org.xml.sax.SAXException;

public class FormTemplateTests {

	private static final Log log = LogFactory.getLog(FormTemplateTests.class);
	
	@Mock
	private FlexScaffoldMetadata flexScaffoldMetadata;
	
	private StringTemplateGroup templateGroup = new StringTemplateGroup("viewTemplateTestsGroup");
	
	@Before
	public void setUp() throws Exception {
		initMocks(this);
		when(flexScaffoldMetadata.getEntityReference()).thenReturn("person");
	}
	
	@Test
	public void testFormWithTextFieldNoValidations() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, new JavaSymbolName("name"), JavaType.STRING_OBJECT, null).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertFalse(result.contains("mx:StringValidator"));
		assertTrue(result.contains("s:TextInput"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithTextFieldSingleValidation() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		AnnotationMetadataBuilder annotation = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"), Collections.EMPTY_LIST); 
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, Collections.singletonList(annotation), new JavaSymbolName("name"), JavaType.STRING_OBJECT).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertTrue(result.contains("mx:StringValidator"));
		assertTrue(result.contains("s:TextInput"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithTextFieldMultipleValidations() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		AnnotationMetadataBuilder annotation1 = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"), Collections.EMPTY_LIST);
		List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
		attrs.add(new IntegerAttributeValue(new JavaSymbolName("min"), 2));
		AnnotationMetadataBuilder annotation2 = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.Size"), attrs);
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations.add(annotation1);
		annotations.add(annotation2);
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, annotations, new JavaSymbolName("name"), JavaType.STRING_OBJECT).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertTrue(result.contains("mx:StringValidator"));
		assertTrue(result.contains("s:TextInput"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithNumberFieldNoValidations() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, new JavaSymbolName("age"), JavaType.INT_OBJECT, null).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertFalse(result.contains("mx:NumberValidator"));
		assertTrue(result.contains("s:TextInput"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithNumberFieldSingleValidation() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		AnnotationMetadataBuilder annotation = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"), Collections.EMPTY_LIST); 
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, Collections.singletonList(annotation), new JavaSymbolName("age"), JavaType.INT_OBJECT).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertTrue(result.contains("mx:NumberValidator"));
		assertTrue(result.contains("s:TextInput"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithNumberFieldMultipleValidations() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		AnnotationMetadataBuilder annotation1 = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"), Collections.EMPTY_LIST);
		List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
		attrs.add(new IntegerAttributeValue(new JavaSymbolName("value"), 2));
		AnnotationMetadataBuilder annotation2 = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.Min"), attrs);
		List<AnnotationMetadataBuilder> annotations = new ArrayList<AnnotationMetadataBuilder>();
		annotations.add(annotation1);
		annotations.add(annotation2);
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, annotations, new JavaSymbolName("name"), JavaType.INT_OBJECT).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertTrue(result.contains("mx:NumberValidator"));
		assertTrue(result.contains("s:TextInput"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithDateFieldNoValidations() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, new JavaSymbolName("birthday"), new JavaType("java.util.Date"), null).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertFalse(result.contains("mx:DateValidator"));
		assertTrue(result.contains("mx:DateField"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithDateFieldSingleValidation() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		AnnotationMetadataBuilder annotation = new AnnotationMetadataBuilder(new JavaType("javax.validation.constraints.NotNull"), Collections.EMPTY_LIST); 
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, Collections.singletonList(annotation), new JavaSymbolName("birthday"), new JavaType("java.util.Date")).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertTrue(result.contains("mx:DateValidator"));
		assertTrue(result.contains("mx:DateField"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
	
	@Test
	public void testFormWithOneToOneRelationship() throws SAXException, IOException {
		ActionScriptType entityType = new ActionScriptType("com.foo.Person");
		StringTemplate listViewTemplate = templateGroup.getInstanceOf("org/springframework/flex/roo/addon/ui/entity_form");
		listViewTemplate.setAttribute("entityType", entityType);
		listViewTemplate.setAttribute("flexScaffoldMetadata", flexScaffoldMetadata);
		
		List<FieldMetadata> elegibleFields = new ArrayList<FieldMetadata>();
		List<AnnotationAttributeValue<?>> attrs = new ArrayList<AnnotationAttributeValue<?>>();
		attrs.add(new ClassAttributeValue(new JavaSymbolName("targetEntity"), new JavaType("com.foo.Address")));
		AnnotationMetadataBuilder annotation = new AnnotationMetadataBuilder(new JavaType("javax.persistence.OneToOne"), attrs);
		FieldMetadata field = new FieldMetadataBuilder("MID:person#1", Modifier.PRIVATE, Collections.singletonList(annotation), new JavaSymbolName("currentAddress"), new JavaType("com.foo.Address")).build();
		elegibleFields.add(field);
		listViewTemplate.setAttribute("fields", FlexUIMetadataProvider.wrapFields(elegibleFields));
		listViewTemplate.setAttribute("relatedTypes", Collections.singletonList(new FlexUIMetadataProvider.RelatedTypeWrapper(new ActionScriptType("com.foo.Address"), elegibleFields, true)));
		Map<String, String> labelFields = new HashMap<String, String>();
		labelFields.put("currentAddress", "street");
		listViewTemplate.setAttribute("labelFields", labelFields);
		
		String result = listViewTemplate.toString();
		log.debug(result);
		
		assertTrue(result.contains("import com.foo.Address;"));
		assertTrue(result.contains("person.currentAddress = currentAddressInput.selectedItem;"));
		
		ByteArrayInputStream stream = new ByteArrayInputStream(result.getBytes("UTF-8"));
		XmlUtils.getDocumentBuilder().parse(stream);
	}
}
