package org.springframework.flex.remoting;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.flex.core.AbstractDestinationFactory;
import org.springframework.flex.core.MessageBrokerFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import flex.messaging.Destination;
import flex.messaging.FactoryInstance;
import flex.messaging.FlexFactory;
import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.services.RemotingService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

/**
 * An exporter for exposing a Spring-managed bean to a Flex client for direct
 * remoting calls.
 * 
 * <p>
 * The exported service will be exposed to the Flex client as a BlazeDS remoting
 * service destination. By default, the destination id will be the same as the
 * bean name of this exporter. This may be overridden using the serviceId property.
 * <i>Note that this convention is slightly different from that employed by the
 * <code>remote-service</code> xml config tag. See the xsd docs for details.</i>
 * 
 * <p>
 * The methods on the exported service that are exposed to the Flex client can
 * be controlled using the includeMethods and excludeMethods properties.
 * </p>
 * 
 * @see MessageBrokerFactoryBean
 * 
 * @author Jeremy Grelle
 * @author Mark Fisher
 */
public class RemotingDestinationExporter extends AbstractDestinationFactory implements FlexFactory, BeanFactoryAware {

	private static final Log log = LogFactory.getLog(RemotingDestinationExporter.class);
	
	private Object service;

	private String[] includeMethods;

	private String[] excludeMethods;
	
	private BeanFactory beanFactory;
	
	private String serviceAdapter;

	public void setService(Object service) {
		this.service = service;
	}
	
	public void setServiceAdapter(String serviceAdapter) {
		this.serviceAdapter = serviceAdapter;
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
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	@Override
	protected Destination createDestination(String destinationId, MessageBroker broker) {
		Assert.notNull(service, "The 'service' property is required.");

		// Look up the remoting service
		RemotingService remotingService = (RemotingService) broker
				.getServiceByType(RemotingService.class.getName());
		Assert.notNull(remotingService,
						"Could not find a proper RemotingService in the Flex MessageBroker.");

		// Register and start the destination
		RemotingDestination destination = (RemotingDestination) remotingService
				.createDestination(destinationId);

		destination.setFactory(this);
		
		String adapterId = StringUtils.hasText(serviceAdapter) ? serviceAdapter : remotingService.getDefaultAdapter();
		if (beanFactory.containsBean(adapterId)) {
			ServiceAdapter adapter = (ServiceAdapter) beanFactory.getBean(adapterId, ServiceAdapter.class);
			destination.setAdapter(adapter);
		}
		
		if (log.isInfoEnabled()) {
			log.info("Created remoting destination with id '"+destinationId+"'");
		}
		
		return destination;
	}

	@Override
	protected void initializeDestination(Destination destination) {
		destination.start();

		Assert.isInstanceOf(ServiceAdapter.class, destination.getAdapter(),
				"Spring beans exported as a RemotingDestination require a ServiceAdapter.");

		configureIncludes(destination);
		configureExcludes(destination);
		
		if (log.isInfoEnabled()) {
			log.info("Remoting destination '"+destination.getId()+"' has been started started successfully.");
		}
	}

	@Override
	protected void destroyDestination(String destinationId, MessageBroker broker) {
		RemotingService remotingService = (RemotingService) broker
				.getServiceByType(RemotingService.class.getName());

		if (remotingService == null) {
			return;
		}
		
		if (log.isInfoEnabled()) {
			log.info("Removing remoting destination '"+destinationId+"'");
		}

		remotingService.removeDestination(destinationId);
	}

	private void configureExcludes(Destination destination) {

		if (excludeMethods == null) {
			return;
		}

		JavaAdapter adapter = (JavaAdapter) destination.getAdapter();
		for (RemotingMethod method : getRemotingMethods(excludeMethods)) {
			adapter.addExcludeMethod(method);
		}
	}

	private void configureIncludes(Destination destination) {

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
