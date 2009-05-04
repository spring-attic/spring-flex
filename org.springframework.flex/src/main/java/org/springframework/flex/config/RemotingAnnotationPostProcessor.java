package org.springframework.flex.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.flex.remoting.RemotingDestination;
import org.springframework.flex.remoting.RemotingDestinationExporter;
import org.springframework.flex.remoting.RemotingExclude;
import org.springframework.flex.remoting.RemotingInclude;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

/**
 * {@link BeanFactoryPostProcessor} implementation that searches the {@link BeanFactory} for
 * beans annotated with {@link RemotingDestination} and adds a corresponding {@link RemotingDestinationExporter}
 * bean definition according to the attributes of the RemotingDestination annotation and any methods found to
 * be marked with either the {@link RemotingInclude} or {@link RemotingExclude} annotation.
 * 
 * <p>
 * This processor will be enabled automatically when using the message-broker tag of the xml config namespace.
 *
 * @author Jeremy Grelle
 */
public class RemotingAnnotationPostProcessor implements
		BeanFactoryPostProcessor {

	private static final Log log = LogFactory.getLog(RemotingAnnotationPostProcessor.class);
	
	// --------------------------- Bean Configuration Properties -------------//
	private static final String MESSAGE_BROKER_PROPERTY = "messageBroker";
	private static final String SERVICE_PROPERTY = "service";
	private static final String DESTINATION_ID_PROPERTY = "destinationId";
	private static final String CHANNELS_PROPERTY = "channels";
	private static final String INCLUDE_METHODS_PROPERTY = "includeMethods";
	private static final String EXCLUDE_METHODS_PROPERTY = "excludeMethods";
	private static final String SERVICE_ADAPTER_PROPERTY = "serviceAdapter";
	
	
	public void postProcessBeanFactory(
			ConfigurableListableBeanFactory beanFactory) throws BeansException {
		
		Set<RemotingDestinationMetadata> remoteBeans = findRemotingDestinations(beanFactory);
		
		if (remoteBeans.size() > 0) {
			Assert.isInstanceOf(BeanDefinitionRegistry.class, beanFactory, 
					"In order for services to be exported via the @RemotingDestination annotation, the current BeanFactory must be a BeanDefinitionRegistry.");
		}
		
		BeanDefinitionRegistry registry = (BeanDefinitionRegistry) beanFactory;
		
		for (RemotingDestinationMetadata remotingDestinationConfig : remoteBeans) {
			
			BeanDefinitionBuilder exporterBuilder = BeanDefinitionBuilder.rootBeanDefinition(RemotingDestinationExporter.class);
			exporterBuilder.getRawBeanDefinition().setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
			
			RemotingDestination remotingDestination = remotingDestinationConfig.getRemotingDestination();
			
			String messageBrokerId = StringUtils.hasText(remotingDestination.messageBroker()) ? remotingDestination.messageBroker() : BeanIds.MESSAGE_BROKER;
			String destinationId = StringUtils.hasText(remotingDestination.value()) ? remotingDestination.value() : remotingDestinationConfig.getBeanName();
			
			exporterBuilder.addPropertyReference(MESSAGE_BROKER_PROPERTY, messageBrokerId);
			exporterBuilder.addPropertyReference(SERVICE_PROPERTY, remotingDestinationConfig.getBeanName());
			exporterBuilder.addPropertyValue(DESTINATION_ID_PROPERTY, destinationId);
			exporterBuilder.addPropertyValue(CHANNELS_PROPERTY, remotingDestination.channels());
			exporterBuilder.addPropertyValue(INCLUDE_METHODS_PROPERTY, remotingDestinationConfig.getIncludeMethods());
			exporterBuilder.addPropertyValue(EXCLUDE_METHODS_PROPERTY, remotingDestinationConfig.getExcludeMethods());
			exporterBuilder.addPropertyValue(SERVICE_ADAPTER_PROPERTY, remotingDestination.serviceAdapter());
			
			BeanDefinitionReaderUtils.registerWithGeneratedName(exporterBuilder.getBeanDefinition(), registry);
		}

	}


	/**
	 * Helper that searches the BeanFactory for beans annotated with @RemotingDestination, being careful
	 * not to force eager creation of the beans.
	 * @param beanFactory - the BeanFactory to search
	 * @return - a set of collected RemotingDestinationMetadata
	 */
	private Set<RemotingDestinationMetadata> findRemotingDestinations(
			ConfigurableListableBeanFactory beanFactory) {
		Set<RemotingDestinationMetadata> remotingDestinations = new HashSet<RemotingDestinationMetadata>();
		for (String beanName : beanFactory.getBeanDefinitionNames()) {
			Class<?> handlerType = beanFactory.getType(beanName);
			RemotingDestination remotingDestination = null;
			if (handlerType != null) {
				remotingDestination = AnnotationUtils.findAnnotation(handlerType, RemotingDestination.class);
			} else {
				if (log.isDebugEnabled()) {
					log.debug("Could not get type of bean '"+beanName+"' from bean factory.");
				}
			}
			if (remotingDestination != null) {
				remotingDestinations.add(new RemotingDestinationMetadata(remotingDestination, beanName, handlerType));
			} else {
				BeanDefinition bd = beanFactory.getMergedBeanDefinition(beanName);
				if (bd instanceof AbstractBeanDefinition) {
					AbstractBeanDefinition abd = (AbstractBeanDefinition) bd;
					if (abd.hasBeanClass()) {
						Class<?> beanClass = abd.getBeanClass();
						remotingDestination = AnnotationUtils.findAnnotation(beanClass, RemotingDestination.class);
						if (remotingDestination != null) {
							remotingDestinations.add(new RemotingDestinationMetadata(remotingDestination, beanName, beanClass));
						}
					}
				}
			}
		}
		return remotingDestinations;
	}
	
}
