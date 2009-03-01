package org.springframework.flex.messaging.config;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class RemoteServiceBeanDefinitionDecorator extends RemoteServiceExporterBeanDefinitionFactory implements
		BeanDefinitionDecorator {

	public BeanDefinitionHolder decorate(Node node,
			BeanDefinitionHolder definition, ParserContext parserContext) {
		Element element = (Element) node;
		parseInternal(element, parserContext, definition.getBeanName());
		return definition;
	}
	
	protected void validateRemoteService(Element element,
			ParserContext parserContext) {
		if (StringUtils.hasText(element.getAttribute(REF_ATTR))) {
			parserContext.getReaderContext().error("ref attribute not allowed when using remote-service as a nested tag.", element);
		}
	}
}
