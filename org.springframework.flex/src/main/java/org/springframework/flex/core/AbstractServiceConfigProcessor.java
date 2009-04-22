package org.springframework.flex.core;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import flex.messaging.MessageBroker;
import flex.messaging.services.Service;
import flex.messaging.services.ServiceAdapter;

public abstract class AbstractServiceConfigProcessor implements
		MessageBrokerConfigProcessor, BeanFactoryAware {

	private String defaultAdapterId;
	private String[] defaultChannels;
	private BeanFactory beanFactory;
	
	public AbstractServiceConfigProcessor() {
		this.defaultAdapterId = getServiceAdapterId();
	}
	
	protected abstract void findDefaultChannel(MessageBroker broker,
			Service service);

	protected abstract String getServiceAdapterClassName();

	protected abstract String getServiceAdapterId();

	protected abstract String getServiceId();

	protected abstract String getServiceClassName();

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;
	}

	/**
	 * Set the id for the default adapter to be installed in the
	 * Service.
	 * 
	 * @param defaultAdapterId
	 *            the id to set for the adapter
	 */
	public void setDefaultAdapterId(String defaultAdapterId) {
		this.defaultAdapterId = defaultAdapterId;
	}

	/**
	 * Set the ids of the default channels to be set on the RemotingService. If
	 * not set, the application-wide defaults will be used. If no
	 * application-wide defaults can be found, a best guess will be made using
	 * the first available channel with an AMFEndpoint.
	 * 
	 * @param defaultChannels
	 *            the ids of the default channels for the RemotingService
	 */
	public void setDefaultChannels(String[] defaultChannels) {
		this.defaultChannels = StringUtils.trimArrayElements(defaultChannels);
	}

	/**
	 * Error checking is done on the started MessageBroker to ensure
	 * configuration was successful.
	 * 
	 * @see MessageBrokerConfigProcessor#processAfterStartup(MessageBroker)
	 */
	public MessageBroker processAfterStartup(MessageBroker broker) {
		// Eagerly detect possible problems with the Service
		Service service = broker.getServiceByType(getServiceClassName());
		Assert.notNull(service, "The MessageBroker with id '" + broker.getId()
				+ "' does not have a service of type "
				+ getServiceClassName() + " configured.");
		Assert.isTrue(service.isStarted(), "The Service with id '"
				+ service.getId() + "' of MessageBroker with id '"
				+ broker.getId() + "' was not started as expected.");
		return broker;
	}

	/**
	 * The MessageBroker is checked to see if the Service has already been
	 * configured via the BlazeDS XML config. If no existing Service is found,
	 * one will be installed using the defined configuration properties of this
	 * class.
	 * 
	 * @see MessageBrokerConfigProcessor#processBeforeStartup(MessageBroker)
	 */
	public MessageBroker processBeforeStartup(MessageBroker broker) {
		Service service = broker.getServiceByType(getServiceClassName());
		if (service == null) {
			service = broker.createService(getServiceId(), getServiceClassName());
			if (getServiceAdapterId().equals(defaultAdapterId)) {
				service.registerAdapter(getServiceAdapterId(),
						getServiceAdapterClassName());
			} else {
				Assert.isAssignable(ServiceAdapter.class, beanFactory
						.getType(defaultAdapterId),
						"A custom default adapter id must refer to a valid Spring bean that "
								+ "is a subclass of "
								+ ServiceAdapter.class.getName() + ".  ");
				service.registerAdapter(defaultAdapterId,
						CustomSpringAdapter.class.getName());
			}
			service.setDefaultAdapter(defaultAdapterId);
			if (!ObjectUtils.isEmpty(defaultChannels)) {
				addDefaultChannels(broker, service);
			} else {
				findDefaultChannel(broker, service);
			}
		}
		return broker;
	}

	/**
	 * Adds the default channels to the MessageBroker's RemotingService. The
	 * <code>defaultChannels</code> to will be validated to ensure they exist in
	 * the MessageBroker before they are set.
	 * 
	 * @param broker
	 *            the newly configured MessageBroker
	 * @param remotingService
	 *            the newly created RemotingService
	 */
	private void addDefaultChannels(MessageBroker broker, Service service) {
		List<String> defaultChannelList = new ArrayList<String>();
		for (String channelId : defaultChannels) {
			Assert.isTrue(broker.getChannelIds().contains(channelId),
					"The channel " + channelId
							+ " is not known to the MessageBroker "
							+ broker.getId()
							+ " and cannot be set as a default channel"
							+ " on the RemotingService");
			defaultChannelList.add(channelId);
		}
		service.setDefaultChannels(defaultChannelList);
	}

	/**
	 * This is simply a marker to denote that a Spring-managed adapter will be
	 * injected at the proper initialization point.
	 */
	protected static final class CustomSpringAdapter {
		public CustomSpringAdapter() {
			throw new UnsupportedOperationException(
					"This adapter class should never be instantiated directly by BlazeDS.  "
							+ "It is only a placeholder to denote that a Spring-managed adapter should be injected when a RemotingDestination is"
							+ "initialized.");
		}
	}

}
