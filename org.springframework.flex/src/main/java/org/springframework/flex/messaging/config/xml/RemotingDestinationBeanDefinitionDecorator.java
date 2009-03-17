package org.springframework.flex.messaging.config.xml;

import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.xml.BeanDefinitionDecorator;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Configures a {@link RemotingDestinationExporter} from a nested <code>remote-service</code> tag.     
 * 
 * @author Jeremy Grelle
 */
public class RemotingDestinationBeanDefinitionDecorator extends RemotingDestinationExporterBeanDefinitionFactory implements
		BeanDefinitionDecorator {

	public BeanDefinitionHolder decorate(Node node,
			BeanDefinitionHolder definition, ParserContext parserContext) {
		Element element = (Element) node;
		parseInternal(element, parserContext, definition.getBeanName());
		return definition;
	}
	
	protected void validateRemotingDestination(Element element,
			ParserContext parserContext) {
		if (StringUtils.hasText(element.getAttribute(REF_ATTR))) {
			parserContext.getReaderContext().error("ref attribute not allowed when using remoting-destination as a nested tag.", element);
		}
	}
}
