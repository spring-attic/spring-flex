package org.springframework.flex.config.xml;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.remoting.RemotingDestinationExporter;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * Shared base class for building {@link RemotingDestinationExporter} bean definitions.
 * 
 * @author Jeremy Grelle
 */
abstract class RemotingDestinationExporterBeanDefinitionFactory {
	
	// --------------------------- Full qualified class names ----------------//
	protected static final String REMOTING_DESTINATION_EXPORTER_CLASS_NAME = "org.springframework.flex.remoting.RemotingDestinationExporter";
		
	// --------------------------- XML Config Attributes ---------------------//
	protected static final String MESSAGE_BROKER_ATTR = "message-broker";
	protected static final String REF_ATTR = "ref";
	protected static final String DESTINATION_ID_ATTR = "destination-id";
	protected static final String CHANNELS_ATTR = "channels";
	protected static final String INCLUDE_METHODS_ATTR = "include-methods";
	protected static final String EXCLUDE_METHODS_ATTR = "exclude-methods";

	// --------------------------- Bean Configuration Properties -------------//
	protected static final String MESSAGE_BROKER_PROPERTY = "messageBroker";
	protected static final String SERVICE_PROPERTY = "service";
	protected static final String DESTINATION_ID_PROPERTY = "destinationId";
	protected static final String CHANNELS_PROPERTY = "channels";
	protected static final String INCLUDE_METHODS_PROPERTY = "includeMethods";
	protected static final String EXCLUDE_METHODS_PROPERTY = "excludeMethods";
	
	protected BeanDefinitionHolder parseInternal(Element element, ParserContext parserContext, String exportedBeanReference) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(REMOTING_DESTINATION_EXPORTER_CLASS_NAME);
		builder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
		
		validateRemotingDestination(element, parserContext);
		
		String destinationId = element.getAttribute(DESTINATION_ID_ATTR);
		String brokerId = element.getAttribute(MESSAGE_BROKER_ATTR);
		
		builder.addPropertyReference(SERVICE_PROPERTY, exportedBeanReference);

		destinationId = StringUtils.hasText(destinationId) ? destinationId : exportedBeanReference;
		builder.addPropertyValue(DESTINATION_ID_PROPERTY, destinationId);
		
		brokerId = StringUtils.hasText(brokerId) ? brokerId : BeanIds.MESSAGE_BROKER; 
		builder.addPropertyReference(MESSAGE_BROKER_PROPERTY, brokerId);

		ParsingUtils.mapOptionalAttributes(element, builder, CHANNELS_ATTR, INCLUDE_METHODS_ATTR, EXCLUDE_METHODS_ATTR);
		
		String beanName = ParsingUtils.registerInfrastructureComponent(element, parserContext, builder);
		
		return new BeanDefinitionHolder(builder.getBeanDefinition(), beanName);
	}
	
	protected abstract void validateRemotingDestination(Element element, ParserContext parserContext);
}
