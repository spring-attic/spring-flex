package org.springframework.flex.messaging.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Configures a {@link FlexRemotingServiceExporter} from a top-level <code>remote-service</code> tag.     
 * 
 * @author Jeremy Grelle
 */
public class RemoteServiceBeanDefinitionParser extends RemoteServiceExporterBeanDefinitionFactory implements BeanDefinitionParser{

	public BeanDefinition parse(Element element, ParserContext parserContext) {
		return parseInternal(element, parserContext, element.getAttribute(REF_ATTR)).getBeanDefinition();
	}
	
	protected void validateRemoteService(Element element,
			ParserContext parserContext) {
		if (!StringUtils.hasText(element.getAttribute(REF_ATTR))) {
			parserContext.getReaderContext().error("A bean reference is required when using remote-service as a top-level tag.", element);
		}
	}
}
