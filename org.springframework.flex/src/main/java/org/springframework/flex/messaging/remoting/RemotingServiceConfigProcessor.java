package org.springframework.flex.messaging.remoting;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.flex.messaging.config.MessageBrokerConfigProcessor;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

import flex.messaging.MessageBroker;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.services.RemotingService;
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
		MessageBrokerConfigProcessor {

	private static final Log log = LogFactory
			.getLog(RemotingServiceConfigProcessor.class);

	private static final String DEFAULT_REMOTING_SERVICE_ID = "remoting-service";

	private String defaultAdapterId = "java-object";
	private String defaultAdapterClass = JavaAdapter.class.getName();
	private String[] defaultChannels;
	
	/**
	 * Set the id for the default adapter to be installed in the RemotingService.  Defaults to "java-object".
	 * @param defaultAdapterId the id to set for the adapter
	 */
	public void setDefaultAdapterId(String defaultAdapterId) {
		this.defaultAdapterId = defaultAdapterId;
	}

	/**
	 * Set the fully qualified class name for the default adapter class to be installed in the RemotingService.  Defaults
	 * to "flex.messaging.services.remoting.adapters.JavaAdapter".
	 * @param defaultAdapterClass the class of the default adapter
	 */
	public void setDefaultAdapterClass(String defaultAdapterClass) {
		this.defaultAdapterClass = defaultAdapterClass;
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
			remotingService.registerAdapter(defaultAdapterId,defaultAdapterClass);
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

}
