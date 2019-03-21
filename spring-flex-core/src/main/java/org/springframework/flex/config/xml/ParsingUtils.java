/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSimpleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.core.Conventions;
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

    static int countProvidedAttributeValues(Element element, String... values) {
        int count = 0;
        for (String s : values) {
            if (StringUtils.hasText(element.getAttribute(s))) {
                count++;
            }
        }
        return count;
    }

    static void mapAllAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
        new SimpleBeanDefinitionParser().mapToBuilder(element, parserContext, builder);
    }

    static void mapOptionalAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String... attrs) {
        for (String attr : attrs) {
            String value = element.getAttribute(attr);
            if (StringUtils.hasText(value)) {
                String propertyName = Conventions.attributeNameToPropertyName(attr);
                if (validateProperty(element, parserContext, propertyName, attr)) {
                    builder.addPropertyValue(propertyName, value);
                }
            }
        }
    }

    static void mapOptionalBeanRefAttributes(Element element, BeanDefinitionBuilder builder, ParserContext parserContext, String... attrs) {
        for (String attr : attrs) {
            String value = element.getAttribute(attr);
            if (StringUtils.hasText(value)) {
                String propertyName = Conventions.attributeNameToPropertyName(attr);
                if (validateProperty(element, parserContext, propertyName, attr)) {
                    builder.addPropertyReference(propertyName, value);
                }
            }
        }
    }

    static void mapRequiredAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String... attrs) {
        for (String attr : attrs) {
            String value = element.getAttribute(attr);
            if(!validateRequiredAttribute(element,parserContext,attr)) {
                return;
            }
            String propertyName = Conventions.attributeNameToPropertyName(attr);
            if (validateProperty(element, parserContext, propertyName, attr)) {
                builder.addPropertyValue(propertyName, value);
            }
        }
    }

    static void mapRequiredBeanRefAttributes(Element element, ParserContext parserContext, BeanDefinitionBuilder builder, String... attrs) {
        for (String attr : attrs) {
            String value = element.getAttribute(attr);
            if(!validateRequiredAttribute(element,parserContext,attr)) {
                return;
            }
            String propertyName = Conventions.attributeNameToPropertyName(attr);
            if (validateProperty(element, parserContext, propertyName, attr)) {
                builder.addPropertyReference(propertyName, value);
            }
        }
    }

    static String registerInfrastructureComponent(Element element, ParserContext parserContext, BeanDefinitionBuilder componentBuilder) {
        String beanName = parserContext.getReaderContext().generateBeanName(componentBuilder.getRawBeanDefinition());
        ParsingUtils.registerInfrastructureComponent(element, parserContext, componentBuilder, beanName);
        return beanName;
    }

    static void registerInfrastructureComponent(Element element, ParserContext parserContext, BeanDefinitionBuilder componentBuilder, String beanName) {
        componentBuilder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
        componentBuilder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
        parserContext.registerBeanComponent(new BeanComponentDefinition(componentBuilder.getBeanDefinition(), beanName));
    }
    
    private static boolean validateRequiredAttribute(Element element, ParserContext parserContext, String attr) {
        if (!StringUtils.hasText(attr)) {
            parserContext.getReaderContext().error("The '" + attr + "' attribute is required.", parserContext.extractSource(element));
            return false;
        }
        return true;
    }

    private static boolean validateProperty(Element element, ParserContext parserContext, String propertyName, String attr) {
        if (!StringUtils.hasText(propertyName)) {
            parserContext.getReaderContext().error(
                "Illegal property name trying to convert from attribute '" + attr + "' : cannot be null or empty.",
                parserContext.extractSource(element));
            return false;
        }
        return true;
    }

    private static class SimpleBeanDefinitionParser extends AbstractSimpleBeanDefinitionParser {

        protected void mapToBuilder(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {
            try {
                this.doParse(element, parserContext, builder);
            } catch (Exception ex) {
                parserContext.getReaderContext().error(ex.getMessage(), parserContext.extractSource(element), ex);
            }
        }
    }

}
