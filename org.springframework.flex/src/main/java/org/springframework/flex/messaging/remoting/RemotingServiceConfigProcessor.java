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

public class RemotingServiceConfigProcessor implements
		MessageBrokerConfigProcessor {

	private static final Log log = LogFactory
			.getLog(RemotingServiceConfigProcessor.class);

	private static final String DEFAULT_REMOTING_SERVICE_ID = "remoting-service";

	private String defaultAdapterId = "java-adapter";
	private String defaultAdapterClass = JavaAdapter.class.getName();
	private String[] defaultChannels;
	
	public void setDefaultAdapterId(String defaultAdapterId) {
		this.defaultAdapterId = defaultAdapterId;
	}

	public void setDefaultAdapterClass(String defaultAdapterClass) {
		this.defaultAdapterClass = defaultAdapterClass;
	}

	public void setDefaultChannels(String[] defaultChannels) {
		this.defaultChannels = StringUtils.trimArrayElements(defaultChannels);
	}

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
