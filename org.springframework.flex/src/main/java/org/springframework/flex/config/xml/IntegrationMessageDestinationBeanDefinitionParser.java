package org.springframework.flex.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class IntegrationMessageDestinationBeanDefinitionParser extends
		AbstractMessageDestinationBeanDefinitionParser {

	// --------------------------- Full qualified class names ----------------//
	private static final String INTEGRATION_ADAPTER_CLASS_NAME = "org.springframework.flex.messaging.integration.IntegrationAdapter";

	// --------------------------- XML Config Attributes ---------------------//
	private static final String MESSAGE_CHANNEL_ATTR = "message-channel";
	
	// --------------------------- Bean Configuration Properties -------------//
	private static final String SERVICE_ADAPTER_PROPERTY = "serviceAdapter";
	
	@Override
	protected void parseAdapter(Element element, ParserContext parserContext,
			BeanDefinitionBuilder destinationBuilder) {
		BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(INTEGRATION_ADAPTER_CLASS_NAME);
		
		ParsingUtils.mapRequiredBeanRefAttributes(element, adapterBuilder, MESSAGE_CHANNEL_ATTR);
		
		String serviceAdapterId = ParsingUtils.registerInfrastructureComponent(element, parserContext, adapterBuilder);
		destinationBuilder.addPropertyValue(SERVICE_ADAPTER_PROPERTY, serviceAdapterId);
	}

}
