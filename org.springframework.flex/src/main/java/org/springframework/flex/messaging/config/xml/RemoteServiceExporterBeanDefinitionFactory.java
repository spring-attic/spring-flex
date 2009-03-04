package org.springframework.flex.messaging.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.parsing.BeanComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.messaging.remoting.FlexRemotingServiceExporter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Shared base class for building {@link FlexRemotingServiceExporter} bean definitions.
 * 
 * @author Jeremy Grelle
 */
abstract class RemoteServiceExporterBeanDefinitionFactory {
	
	// --------------------------- Full qualified class names ----------------//
	protected static final String REMOTING_SERVICE_EXPORTER_CLASS_NAME = "org.springframework.flex.messaging.remoting.FlexRemotingServiceExporter";
		
	// --------------------------- XML Config Attributes ---------------------//
	protected static final String MESSAGE_BROKER_ATTR = "message-broker";
	protected static final String REF_ATTR = "ref";
	protected static final String SERVICE_ID_ATTR = "service-id";
	protected static final String CHANNELS_ATTR = "channels";
	protected static final String INCLUDE_METHODS_ATTR = "include-methods";
	protected static final String EXCLUDE_METHODS_ATTR = "exclude-methods";

	// --------------------------- Bean Configuration Properties -------------//
	protected static final String MESSAGE_BROKER_PROPERTY = "messageBroker";
	protected static final String SERVICE_PROPERTY = "service";
	protected static final String SERVICE_ID_PROPERTY = "serviceId";
	protected static final String CHANNELS_PROPERTY = "channels";
	protected static final String INCLUDE_METHODS_PROPERTY = "includeMethods";
	protected static final String EXCLUDE_METHODS_PROPERTY = "excludeMethods";
	
	protected BeanDefinitionHolder parseInternal(Element element, ParserContext parserContext, String exportedBeanReference) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(REMOTING_SERVICE_EXPORTER_CLASS_NAME);
		builder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		
		validateRemoteService(element, parserContext);
		
		String serviceId = element.getAttribute(SERVICE_ID_ATTR);
		String brokerId = element.getAttribute(MESSAGE_BROKER_ATTR);
		
		builder.addPropertyReference(SERVICE_PROPERTY, exportedBeanReference);

		serviceId = StringUtils.hasText(serviceId) ? serviceId : exportedBeanReference;
		builder.addPropertyValue(SERVICE_ID_PROPERTY, serviceId);
		
		brokerId = StringUtils.hasText(brokerId) ? brokerId : BeanIds.MESSAGE_BROKER; 
		builder.addPropertyReference(MESSAGE_BROKER_PROPERTY, brokerId);

		ParsingUtils.mapOptionalAttributes(element, builder, CHANNELS_ATTR, INCLUDE_METHODS_ATTR, EXCLUDE_METHODS_ATTR);
		
		String beanName = registerInfrastructureComponent(element, parserContext, builder);
		
		return new BeanDefinitionHolder(builder.getBeanDefinition(), beanName);
	}
	
	protected abstract void validateRemoteService(Element element, ParserContext parserContext);
	
	private String registerInfrastructureComponent(Element element, ParserContext parserContext,
			BeanDefinitionBuilder componentBuilder) {
		String beanName = parserContext.getReaderContext().generateBeanName(componentBuilder.getRawBeanDefinition());
		componentBuilder.getRawBeanDefinition().setSource(parserContext.extractSource(element));
		componentBuilder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		parserContext.registerBeanComponent(new BeanComponentDefinition(componentBuilder.getBeanDefinition(),
				beanName));
		return beanName;
	}
}
