package org.springframework.flex.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class MessageDestinationBeanDefinitionParser extends
		AbstractMessageDestinationBeanDefinitionParser {

	// --------------------------- XML Config Attributes ---------------------//
	private static final String SERVICE_ADAPTER_ATTR = "service-adapter";
	
	@Override
	protected void parseAdapter(Element element, ParserContext parserContext,
			BeanDefinitionBuilder destinationBuilder) {
		
		ParsingUtils.mapOptionalAttributes(element, destinationBuilder, SERVICE_ADAPTER_ATTR);
	}

}
