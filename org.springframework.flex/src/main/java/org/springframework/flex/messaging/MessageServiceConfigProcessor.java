package org.springframework.flex.messaging;

import java.util.Iterator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.flex.config.MessageBrokerConfigProcessor;
import org.springframework.flex.core.AbstractServiceConfigProcessor;
import org.springframework.util.CollectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.config.ConfigMap;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.services.MessageService;
import flex.messaging.services.Service;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;

/**
 * {@link MessageBrokerConfigProcessor} implementation that installs a default
 * MessageService if one has not already been configured through the BlazeDS
 * XML configuration.
 * 
 * <p>
 * Using this processor makes the traditional <code>messaging-config.xml</code>
 * file in BlazeDS XML configuration unnecessary when exclusively using Spring
 * to configure Flex messaging destinations.
 * 
 * <p>
 * This processor is installed automatically when using the
 * <code>message-broker</code> tag in the xml namespace configuration. Its
 * settings can be customized using the <code>message-service</code> child tag.
 * See the XSD docs for more detail.
 * 
 * @author Jeremy Grelle
 */
public class MessageServiceConfigProcessor extends
		AbstractServiceConfigProcessor {

	private static final Log log = LogFactory.getLog(MessageServiceConfigProcessor.class);
	
	/**
	 * Tries to find a sensible default AMF channel for the default
	 * MessageService
	 * 
	 * If a application-level default is set on the MessageBroker, that will be
	 * used. Otherwise will use the first AMFEndpoint from services-config.xml
	 * that it finds with polling enabled.
	 * 
	 * @param broker
	 * @param service
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void findDefaultChannel(MessageBroker broker, Service service) {
		if (!CollectionUtils.isEmpty(broker.getDefaultChannels())) {
			return;
		}

		Iterator channels = broker.getChannelIds().iterator();
		while (channels.hasNext()) {
			Endpoint endpoint = broker.getEndpoint((String) channels.next());
			if (endpoint instanceof AMFEndpoint && isPollingEnabled(endpoint)) {
				service.addDefaultChannel(endpoint.getId());
				return;
			}
		}
		log
				.warn("No appropriate default channels were detected for the RemotingService.  "
						+ "The channels must be explicitly set on any exported service.");
	}

	private boolean isPollingEnabled(Endpoint endpoint) {
		ConfigMap endpointConfig = endpoint.describeEndpoint().getPropertyAsMap("properties", null);
		if (endpointConfig != null && endpointConfig.getPropertyAsMap("polling-enabled", null) != null) {
			return endpointConfig.getPropertyAsMap("polling-enabled", null).getPropertyAsBoolean("", false);
		}
		return false;
	}

	@Override
	protected String getServiceAdapterClassName() {
		return ActionScriptAdapter.class.getName();
	}

	@Override
	protected String getServiceAdapterId() {
		return "actionscript";
	}

	@Override
	protected String getServiceClassName() {
		return MessageService.class.getName();
	}

	@Override
	protected String getServiceId() {
		return "message-service";
	}

}
