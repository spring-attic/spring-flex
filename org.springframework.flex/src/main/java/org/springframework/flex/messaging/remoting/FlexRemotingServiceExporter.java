package org.springframework.flex.messaging.remoting;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.flex.messaging.MessageBrokerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

/**
 * An exporter for exposing a Spring-managed bean to a Flex client for direct
 * remoting calls.
 * 
 * <p>
 * The exported service will be exposed to the Flex client as a BlazeDS remoting
 * service destination. By default, the service id of the destination will be
 * the same as the bean name of this exporter. This may be overridden using the
 * serviceId property.
 * </p>
 * 
 * <p>
 * The methods on the exported service that are exposed to the Flex client can
 * be controlled using the includeMethods and excludeMethods properties.
 * </p>
 * 
 * @see MessageBrokerFactoryBean
 * 
 * @author Jeremy Grelle
 */
public class FlexRemotingServiceExporter implements FlexFactory,
		InitializingBean, DisposableBean, BeanNameAware {

	private String beanName;

	private Object service;

	private String serviceId;

	private String[] channels;

	private MessageBroker broker;

	private String[] includeMethods;

	private String[] excludeMethods;

	public void setBeanName(String name) {
		this.beanName = name;
	}

	public void setMessageBroker(MessageBroker broker) {
		this.broker = broker;
	}

	public void setService(Object service) {
		this.service = service;
	}

	public void setServiceId(String serviceId) {
		this.serviceId = serviceId;
	}

	public void setChannelIds(String[] channels) {
		this.channels = StringUtils.trimArrayElements(channels);
	}
	
	public void setIncludeMethods(String[] includeMethods) {
		this.includeMethods = StringUtils.trimArrayElements(includeMethods);
	}

	public void setExcludeMethods(String[] excludeMethods) {
		this.excludeMethods = StringUtils.trimArrayElements(excludeMethods);
	}

	public FactoryInstance createFactoryInstance(String id, ConfigMap properties) {
		return new ServiceFactoryInstance(this, id, properties);
	}

	/**
	 * Lookup will be handled directly by the created FactoryInstance
	 * 
	 * @exclude
	 */
	public Object lookup(FactoryInstance instanceInfo) {
		throw new UnsupportedOperationException("FlexFactory.lookup");
	}

	/**
	 * @exclude
	 */
	public void initialize(String id, ConfigMap configMap) {
		// No-op
	}

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(service, "The 'service' property is required.");
		Assert.notNull(broker, "The 'messageBroker' property is required.");

		// Look up the remoting service
		RemotingService remotingService = (RemotingService) broker
				.getServiceByType(RemotingService.class.getName());
		Assert
				.notNull(remotingService,
						"Could not find a proper RemotingService in the Flex MessageBroker.");

		// Register and start the destination
		RemotingDestination destination = (RemotingDestination) remotingService
				.createDestination(getServiceId());

		configureChannels(destination);

		destination.setFactory(this);
		destination.start();

		Assert
				.isInstanceOf(JavaAdapter.class, destination.getAdapter(),
						"Spring beans exported as a RemotingDestination require a JavaAdapter.");

		configureIncludes(destination);
		configureExcludes(destination);
	}

	private void configureChannels(RemotingDestination destination) {
		if (ObjectUtils.isEmpty(channels)) {
			return;
		}
		
		for (String channelId : channels) {
			Assert
					.isTrue(
							broker.getChannelIds().contains(channelId),
							"The channel "
									+ channelId
									+ " is not known to the MessageBroker "
									+ broker.getId()
									+ " and cannot be set on the destination "
									+ getServiceId());
		}
		destination.setChannels(CollectionUtils.arrayToList(channels));
	}

	private void configureExcludes(RemotingDestination destination) {

		if (excludeMethods == null) {
			return;
		}

		JavaAdapter adapter = (JavaAdapter) destination.getAdapter();
		for (RemotingMethod method : getRemotingMethods(excludeMethods)) {
			adapter.addExcludeMethod(method);
		}
	}

	private void configureIncludes(RemotingDestination destination) {

		if (includeMethods == null) {
			return;
		}

		JavaAdapter adapter = (JavaAdapter) destination.getAdapter();
		for (RemotingMethod method : getRemotingMethods(includeMethods)) {
			adapter.addIncludeMethod(method);
		}
	}

	private List<RemotingMethod> getRemotingMethods(String[] methodNames) {
		List<RemotingMethod> remotingMethods = new ArrayList<RemotingMethod>();
		for (String name : methodNames) {
			Assert.isTrue(ClassUtils.hasAtLeastOneMethodWithName(service
					.getClass(), name), "Could not find method with name '"
					+ name + "' on the exported service of type "
					+ service.getClass());
			RemotingMethod method = new RemotingMethod();
			method.setName(name);
			remotingMethods.add(method);
		}
		return remotingMethods;
	}

	public void destroy() throws Exception {
		if (broker == null || !broker.isStarted()) {
			return;
		}

		RemotingService remotingService = (RemotingService) broker
				.getServiceByType(RemotingService.class.getName());

		if (remotingService == null) {
			return;
		}

		remotingService.removeDestination(getServiceId());
	}

	private String getServiceId() {
		return StringUtils.hasText(serviceId) ? serviceId : beanName;
	}

	private final class ServiceFactoryInstance extends FactoryInstance {
		public ServiceFactoryInstance(FlexFactory factory, String id,
				ConfigMap properties) {
			super(factory, id, properties);
		}

		@Override
		public Object lookup() {
			return service;
		}
	}
}
