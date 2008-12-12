package org.springframework.flex.messaging;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class FlexRemotingServiceExporter implements FlexFactory,
		InitializingBean, DisposableBean, BeanNameAware {

	private String beanName;

	private Object service;

	private String serviceId;

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
		destination.setFactory(this);
		destination.start();

		Assert
				.isInstanceOf(JavaAdapter.class, destination.getAdapter(),
						"Spring beans exported as a RemotingDestination require a JavaAdapter.");

		configureIncludes(destination);
		configureExcludes(destination);
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
		for (int i = 0; i < methodNames.length; i++) {
			String name = methodNames[i];
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

	public void setIncludeMethods(String[] includeMethods) {
		this.includeMethods = includeMethods;
	}

	public void setExcludeMethods(String[] excludeMethods) {
		this.excludeMethods = excludeMethods;
	}

}
