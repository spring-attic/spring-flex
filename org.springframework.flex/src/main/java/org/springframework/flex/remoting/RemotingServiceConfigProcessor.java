package org.springframework.flex.remoting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.services.RemotingService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.remoting.adapters.JavaAdapter;

/**
 * {@link MessageBrokerConfigProcessor} implementation that installs a default RemotingService if one has not already
 * been configured through the BlazeDS XML configuration.
 * 
 * <p>
 * Using this processor makes the traditional <code>remoting-config.xml</code> file in BlazeDS XML configuration unnecessary
 * when exclusively using Spring to configure Flex remoting destinations.
 * 
 * <p>
 * This processor is installed automatically when using the <code>message-broker</code> tag in the xml namespace configuration.  Its
 * settings can be customized using the <code>remoting-service</code> child tag.  See the XSD docs for more detail.
 * 
 * @author Jeremy Grelle
 */
public class RemotingServiceConfigProcessor implements
		MessageBrokerConfigProcessor, BeanFactoryAware {

	private static final Log log = LogFactory
			.getLog(RemotingServiceConfigProcessor.class);

	private static final String DEFAULT_REMOTING_SERVICE_ID = "remoting-service";
	
	private static final String DEFAULT_ADAPTER_CLASS = JavaAdapter.class.getName();
	
	private static final String DEFAULT_DEFAULT_ADAPTER_ID = "java-object"; 

	private String defaultAdapterId = DEFAULT_DEFAULT_ADAPTER_ID;
	
	private String[] defaultChannels;
	
	private BeanFactory beanFactory;
	
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		this.beanFactory = beanFactory;		
	}
	
	/**
	 * Set the id for the default adapter to be installed in the RemotingService.  Defaults to "java-object".
	 * @param defaultAdapterId the id to set for the adapter
	 */
	public void setDefaultAdapterId(String defaultAdapterId) {
		this.defaultAdapterId = defaultAdapterId;
	}

	/**
	 * Set the ids of the default channels to be set on the RemotingService.  If not set, the application-wide defaults will
	 * be used.  If no application-wide defaults can be found, a best guess will be made using the first available channel
	 * with an AMFEndpoint.
	 * @param defaultChannels the ids of the default channels for the RemotingService 
	 */
	public void setDefaultChannels(String[] defaultChannels) {
		this.defaultChannels = StringUtils.trimArrayElements(defaultChannels);
	}

	/**
	 * Error checking is done on the started MessageBroker to ensure configuration was successful.
	 * @see MessageBrokerConfigProcessor#processAfterStartup(MessageBroker)
	 */
	public MessageBroker processAfterStartup(MessageBroker broker) {
		// Eagerly detect possible problems with the RemotingService
		RemotingService remotingService = (RemotingService) broker
				.getServiceByType(RemotingService.class.getName());
		Assert.notNull(remotingService, "The MessageBroker with id '"
				+ broker.getId()
				+ "' does not have a RemotingService configured.");
		Assert.isTrue(remotingService.isStarted(),
				"The RemotingService of MessageBroker with id '"
						+ broker.getId() + "' was not started as expected.");
		return broker;
	}

	/**
	 * The MessageBroker is checked to see if the RemotingService has already been configured via the BlazeDS XML config.  If no existing
	 * RemotingService is found, one will be installed using the defined configuration properties of this class.
	 * @see MessageBrokerConfigProcessor#processBeforeStartup(MessageBroker)
	 */
	public MessageBroker processBeforeStartup(MessageBroker broker) {
		RemotingService remotingService = (RemotingService) broker
				.getServiceByType(RemotingService.class.getName());
		if (remotingService == null) {
			remotingService = (RemotingService) broker.createService(
					DEFAULT_REMOTING_SERVICE_ID, RemotingService.class
							.getName());
			if (DEFAULT_DEFAULT_ADAPTER_ID.equals(defaultAdapterId)) {
				remotingService.registerAdapter(defaultAdapterId, DEFAULT_ADAPTER_CLASS);
			} else {
				Assert.isAssignable(ServiceAdapter.class, beanFactory.getType(defaultAdapterId), "A custom default adapter id must refer to a valid Spring bean that " +
						"is a subclass of "+ServiceAdapter.class.getName()+".  ");
				remotingService.registerAdapter(defaultAdapterId,CustomSpringAdapter.class.getName());
			}
			remotingService.setDefaultAdapter(defaultAdapterId);
			if (!ObjectUtils.isEmpty(defaultChannels)) {
				addDefaultChannels(broker, remotingService);
			} else {
				findDefaultChannel(broker, remotingService);
			}
		}
		return broker;
	}
	
	/**
	 * Adds the default channels to the MessageBroker's RemotingService.  The <code>defaultChannels</code>
	 * to will be validated to ensure they exist in the MessageBroker before they are set.
	 * @param broker the newly configured MessageBroker
	 * @param remotingService the newly created RemotingService
	 */
	private void addDefaultChannels(MessageBroker broker, RemotingService remotingService) {
		List<String> defaultChannelList = new ArrayList<String>();
		for (String channelId : defaultChannels) {
			Assert
					.isTrue(
							broker.getChannelIds().contains(channelId),
							"The channel "
									+ channelId
									+ " is not known to the MessageBroker "
									+ broker.getId()
									+ " and cannot be set as a default channel" 
									+ " on the RemotingService");
			defaultChannelList.add(channelId);
		}
		remotingService.setDefaultChannels(defaultChannelList);
	}

	/**
	 * Try to find a sensible default AMF channel for the default
	 * RemotingService
	 * 
	 * If a application-level default is set on the MessageBroker, that will be
	 * used. Otherwise will use the first AMFEndpoint from services-config.xml
	 * that it finds.
	 * 
	 * @param broker
	 * @param remotingService
	 */
	@SuppressWarnings("unchecked")
	private void findDefaultChannel(MessageBroker broker,
			RemotingService remotingService) {
		if (!CollectionUtils.isEmpty(broker.getDefaultChannels())) {
			return;
		}

		Iterator channels = broker.getChannelIds().iterator();
		while (channels.hasNext()) {
			Endpoint endpoint = broker.getEndpoint((String) channels.next());
			if (endpoint instanceof AMFEndpoint) {
				remotingService.addDefaultChannel(endpoint.getId());
				return;
			}
		}
		log
				.warn("No appropriate default channels were detected for the RemotingService.  "
						+ "The channels must be explicitly set on any exported service.");
	}
	
	/**
	 * This is simply a marker to denote that a Spring-managed adapter will be injected at the
	 * proper initialization point.
	 */
	private static final class CustomSpringAdapter {
		public CustomSpringAdapter() {
			throw new UnsupportedOperationException("This adapter class should never be instantiated directly by BlazeDS.  " +
					"It is only a placeholder to denote that a Spring-managed adapter should be injected when a RemotingDestination is" +
					"initialized.");
		}
	}

}
