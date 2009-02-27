package org.springframework.flex.messaging.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.CollectionUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import flex.messaging.util.StringUtils;

public class MessageBrokerBeanDefinitionParser extends
		AbstractSingleBeanDefinitionParser {

	// --------------------------- Full qualified class names ----------------//
	private static final String MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME = "org.springframework.flex.messaging.MessageBrokerFactoryBean";
	private static final String MESSAGE_BROKER_HANDLER_ADAPTER_CLASS_NAME = "org.springframework.flex.messaging.servlet.MessageBrokerHandlerAdapter";
	private static final String DEFAULT_HANDLER_MAPPING_CLASS_NAME = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping";
	
	// --------------------------- XML Config Attributes ---------------------//
	private static final String CONFIGURATION_MANAGER_ATTR = "configuration-manager";
	private static final String SERVICES_CONFIG_PATH_ATTR = "services-config-path";

	// --------------------------- Bean Configuration Properties -------------//
	private static final String CONFIGURATION_MANAGER_PROPERTY = "configurationManager";
	private static final String SERVICES_CONFIG_PATH_PROPERTY = "servicesConfigPath";
	private static final String URL_MAP_PROPERTY = "urlMap";
	
	// --------------------------- XML Child Elements ------------------------//
	private static final String MAPPING_PATTERN_ELEMENT = "mapping-pattern";
	
	// --------------------------- Infrastructure Bean IDs -------------------//
	private static final String HANDLER_MAPPING_SUFFIX = "DefaultHandlerMapping";
	
	// --------------------------- Default Values ----------------------------//
	private static final Object DEFAULT_MAPPING_PATH = "/*";

	@Override
	protected String getBeanClassName(Element element) {
		return MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME;
	}

	@Override
	protected void doParse(Element element, ParserContext parserContext,
			BeanDefinitionBuilder builder) {

		CompositeComponentDefinition componentDefinition = new CompositeComponentDefinition(
				element.getLocalName(), parserContext.extractSource(element));
		parserContext.pushContainingComponent(componentDefinition);

		// Set the default ID if necessary
		if (StringUtils.isEmpty(element.getAttribute(ID_ATTRIBUTE))) {
			element.setAttribute(ID_ATTRIBUTE, BeanIds.MESSAGE_BROKER);
		}

		validateMessageBroker(element, parserContext);

		String servicesConfigPath = element
				.getAttribute(SERVICES_CONFIG_PATH_ATTR);
		if (!StringUtils.isEmpty(servicesConfigPath)) {
			builder.addPropertyValue(SERVICES_CONFIG_PATH_PROPERTY,
					servicesConfigPath);
		}

		String configMgr = element.getAttribute(CONFIGURATION_MANAGER_ATTR);
		if (!StringUtils.isEmpty(configMgr)) {
			builder.addPropertyReference(CONFIGURATION_MANAGER_PROPERTY,
					configMgr);
		}

		registerHandlerAdapterIfNecessary(element, parserContext);
		registerHandlerMappings(element, parserContext, DomUtils.getChildElementsByTagName(element, MAPPING_PATTERN_ELEMENT));

		parserContext.popAndRegisterContainingComponent();
	}

	private void registerHandlerAdapterIfNecessary(Element element,
			ParserContext parserContext) {
		//Make sure we only ever register one MessageBrokerHandlerAdapter 
		if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER))
		{
			BeanDefinitionBuilder handlerAdapterBuilder = BeanDefinitionBuilder
					.genericBeanDefinition(MESSAGE_BROKER_HANDLER_ADAPTER_CLASS_NAME);
			
			registerInfrastructureComponent(element, parserContext, handlerAdapterBuilder, BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void registerHandlerMappings(Element parent, ParserContext parserContext, List mappingPatternElements) {
		BeanDefinitionBuilder handlerMappingBuilder = BeanDefinitionBuilder
				.genericBeanDefinition(DEFAULT_HANDLER_MAPPING_CLASS_NAME);
		Map mappings = new HashMap();
		mappings.put(DEFAULT_MAPPING_PATH, parent.getAttribute(ID_ATTRIBUTE));
		handlerMappingBuilder.addPropertyValue(URL_MAP_PROPERTY, mappings);
		registerInfrastructureComponent(parent, parserContext, handlerMappingBuilder, parent.getAttribute(ID_ATTRIBUTE)+HANDLER_MAPPING_SUFFIX);
	}

	private void validateMessageBroker(Element element,
			ParserContext parserContext) {

		if (!StringUtils.isEmpty(element
				.getAttribute(SERVICES_CONFIG_PATH_ATTR))
				&& !StringUtils.isEmpty(element
						.getAttribute(CONFIGURATION_MANAGER_ATTR))) {
			parserContext
					.getReaderContext()
					.error(
							"The "
									+ SERVICES_CONFIG_PATH_ATTR
									+ " cannot be set when using a custom "
									+ CONFIGURATION_MANAGER_ATTR
									+ " reference.  Set the configurationPath on the custom ConfigurationManager instead.",
							element);

		}
	}
	
	private String registerInfrastructureComponent(Element element,
			ParserContext parserContext, BeanDefinitionBuilder componentBuilder) {
		String beanName = parserContext.getReaderContext().generateBeanName(
				componentBuilder.getRawBeanDefinition());
		registerInfrastructureComponent(element, parserContext, componentBuilder, beanName);
		return beanName;
	}

	private void registerInfrastructureComponent(Element element,
			ParserContext parserContext, BeanDefinitionBuilder componentBuilder, String beanName) {
		componentBuilder.getRawBeanDefinition().setSource(
				parserContext.extractSource(element));
		componentBuilder.getRawBeanDefinition().setRole(
				BeanDefinition.ROLE_INFRASTRUCTURE);
		parserContext.registerBeanComponent(new BeanComponentDefinition(
				componentBuilder.getBeanDefinition(), beanName));
	}

}
