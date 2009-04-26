package org.springframework.flex.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * A helper class for mapping XML config element attributes to BeanDefinition properties by convention.
 * 
 * <p>
 * The convention used is to transform hyphenated attributes of the form foo-bar-baz to camel-case properties.
 *  
 * @author Jeremy Grelle
 */
abstract class ParsingUtils {
	
	public static int countProvidedAttributeValues(Element element, String... values) {
		int count = 0;
		for (String s : values) {
			if (StringUtils.hasText(element.getAttribute(s))) {
				count++;
			}
		}
		return count;
	}
	
	public static void mapAllAttributes(Element element, BeanDefinitionBuilder builder) {
		new SimpleBeanDefinitionParser().mapToBuilder(element, builder);
	}
	
	public static void mapOptionalAttributes(Element element, BeanDefinitionBuilder builder, String...attrs) {
		for (String attr : attrs) {
			String value = element.getAttribute(attr);
			if (StringUtils.hasText(value)) {
				String propertyName = Conventions.attributeNameToPropertyName(attr);
				Assert.state(StringUtils.hasText(propertyName),
						"Illegal property name returned from 'Conventions.attributeNameToPropertyName(String)': cannot be null or empty.");
				builder.addPropertyValue(propertyName, value);
			}
		}
	}
	
	public static void mapRequiredAttributes(Element element, BeanDefinitionBuilder builder, String...attrs) {
		for (String attr : attrs) {
			String value = element.getAttribute(attr);
			Assert.isTrue(StringUtils.hasText(value), "The '"+attr+"' attribute is required.");
			String propertyName = Conventions.attributeNameToPropertyName(attr);
			Assert.state(StringUtils.hasText(propertyName),
					"Illegal property name returned from 'Conventions.attributeNameToPropertyName(String)': cannot be null or empty.");
			builder.addPropertyValue(propertyName, value);
		
		}
	}
	
	public static void mapOptionalBeanRefAttributes(Element element, BeanDefinitionBuilder builder, String...attrs) {
		for (String attr : attrs) {
			String value = element.getAttribute(attr);
			if (StringUtils.hasText(value)) {
				String propertyName = Conventions.attributeNameToPropertyName(attr);
				Assert.state(StringUtils.hasText(propertyName),
						"Illegal property name returned from 'Conventions.attributeNameToPropertyName(String)': cannot be null or empty.");
				builder.addPropertyReference(propertyName, value);
			}
		}
	}
	
	public static void mapRequiredBeanRefAttributes(Element element, BeanDefinitionBuilder builder, String...attrs) {
		for (String attr : attrs) {
			String value = element.getAttribute(attr);
			Assert.isTrue(StringUtils.hasText(value), "The '"+attr+"' attribute is required.");
			String propertyName = Conventions.attributeNameToPropertyName(attr);
			Assert.state(StringUtils.hasText(propertyName),
					"Illegal property name returned from 'Conventions.attributeNameToPropertyName(String)': cannot be null or empty.");
			builder.addPropertyReference(propertyName, value);
		
		}
	}
	
	public static String registerInfrastructureComponent(Element element,
			ParserContext parserContext, BeanDefinitionBuilder componentBuilder) {
		String beanName = parserContext.getReaderContext().generateBeanName(
				componentBuilder.getRawBeanDefinition());
		ParsingUtils.registerInfrastructureComponent(element, parserContext, componentBuilder, beanName);
		return beanName;
	}

	public static void registerInfrastructureComponent(Element element,
			ParserContext parserContext, BeanDefinitionBuilder componentBuilder, String beanName) {
		componentBuilder.getRawBeanDefinition().setSource(
				parserContext.extractSource(element));
		componentBuilder.getRawBeanDefinition().setRole(
				BeanDefinition.ROLE_INFRASTRUCTURE);
		parserContext.registerBeanComponent(new BeanComponentDefinition(
				componentBuilder.getBeanDefinition(), beanName));
	}
	
	private static class SimpleBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {
		
		protected void mapToBuilder(Element element, BeanDefinitionBuilder builder) {
			this.doParse(element, builder);
		}
	}
	
}
