package org.springframework.flex.messaging;

import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.beans.PropertyEditor;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.flex.config.json.JsonConfigMapPropertyEditor;
import org.springframework.flex.core.AbstractMessageBrokerTests;

import edu.emory.mathcs.backport.java.util.Arrays;
import flex.messaging.MessageDestination;
import flex.messaging.config.ConfigMap;
import flex.messaging.config.SecurityConstraint;
import flex.messaging.config.ThrottleSettings;
import flex.messaging.security.LoginManager;
import flex.messaging.services.MessageService;
import flex.messaging.services.messaging.adapters.ActionScriptAdapter;
import flex.messaging.services.messaging.adapters.MessagingAdapter;

public class MessageDestinationFactoryTests extends AbstractMessageBrokerTests {

	MessageDestinationFactory factory;
	MessageService service;
	
	@Mock private BeanFactory beanFactory;
	
	@Mock LoginManager loginManager;
	
	LoginManager originalLoginManager;
	
	public void setUp() throws Exception {
		
		if (!getServicesConfigPath().equals(getCurrentConfigPath())) {
			setDirty();
		}
		
		MockitoAnnotations.initMocks(this);
		service = (MessageService) getMessageBroker().getServiceByType(MessageService.class.getName());
		
		originalLoginManager = getMessageBroker().getLoginManager();
		getMessageBroker().setLoginManager(loginManager);
	}
	
	public void tearDown() throws Exception {
		getMessageBroker().setLoginManager(originalLoginManager);
	}
	
	public void testDefaultDestinationCreated() throws Exception {
		
		factory = new MessageDestinationFactory();
		factory.setBeanName("foo1");
		factory.setMessageBroker(getMessageBroker());
		factory.setBeanFactory(beanFactory);
		
		factory.afterPropertiesSet();
		
		MessageDestination destination = (MessageDestination) service.getDestination("foo1");
		assertNotNull(destination);
		assertEquals("foo1", destination.getId());
		assertTrue(destination.isStarted());
		assertNotNull(destination.getAdapter());
		assertTrue(destination.getAdapter() instanceof ActionScriptAdapter);
		assertTrue(destination.getAdapter().isStarted());
	}
	
	@SuppressWarnings("unchecked")
	public void testDestinationWithExplicitProperties() throws Exception {
		
		factory = new MessageDestinationFactory();
		factory.setBeanName("foo-factory");
		factory.setDestinationId("foo2");
		String[] channels = new String[] {"my-amf", "my-polling-amf"};
		factory.setChannels(channels);
		factory.setAllowSubtopics("true");
		factory.setClusterMessageRouting("broadcast");
		factory.setMessageTimeToLive("1");
		factory.setSubscriptionTimeoutMinutes("1");
		factory.setSubtopicSeparator("/");
		factory.setThrottleInboundMaxFrequency("500");
		factory.setThrottleInboundPolicy("ERROR");
		factory.setThrottleOutboundMaxFrequency("500");
		factory.setThrottleOutboundPolicy("IGNORE");
		
		factory.setMessageBroker(getMessageBroker());
		factory.setBeanFactory(beanFactory);
		
		factory.afterPropertiesSet();
		
		MessageDestination destination = (MessageDestination) service.getDestination("foo2");
		assertNotNull(destination);
		assertEquals("foo2", destination.getId());
		assertTrue(destination.getChannels().containsAll(Arrays.asList(channels)));
		assertTrue(destination.getServerSettings().getAllowSubtopics());
		assertTrue(destination.getServerSettings().isBroadcastRoutingMode());
		assertEquals(1, destination.getServerSettings().getMessageTTL());
		assertEquals(1, destination.getNetworkSettings().getSubscriptionTimeoutMinutes());
		assertEquals("/", destination.getServerSettings().getSubtopicSeparator());
		assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getIncomingDestinationFrequency());
		assertEquals(ThrottleSettings.POLICY_ERROR, destination.getNetworkSettings().getThrottleSettings().getInboundPolicy());
		assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getOutgoingDestinationFrequency());
		assertEquals(ThrottleSettings.POLICY_IGNORE, destination.getNetworkSettings().getThrottleSettings().getOutboundPolicy());
		
	}
	
	public void testDestinationWithSecurityConstraints() throws Exception {
		
		factory = new MessageDestinationFactory();
		factory.setBeanName("foo3");
		factory.setSendSecurityConstraint("spring-security-users");
		factory.setSubscribeSecurityConstraint("spring-security-users");
		
		factory.setMessageBroker(getMessageBroker());
		factory.setBeanFactory(beanFactory);
		
		factory.afterPropertiesSet();
		
		MessageDestination destination = (MessageDestination) service.getDestination("foo3");
		assertNotNull(destination);
		assertEquals("foo3", destination.getId());
		
		MessagingAdapter adapter = (MessagingAdapter) destination.getAdapter();
		adapter.getSecurityConstraintManager().assertSendAuthorization();
		adapter.getSecurityConstraintManager().assertSubscribeAuthorization();
		
		verify(loginManager, times(2)).checkConstraint(isA(SecurityConstraint.class));
	}
	
	@SuppressWarnings("unchecked")
	public void testDestinationWithJsonConfigMap() throws Exception {
		
		PropertyEditor editor = new JsonConfigMapPropertyEditor();
		editor.setAsText(readJsonFile());
		factory = new MessageDestinationFactory((ConfigMap) editor.getValue());
		factory.setBeanName("foo-factory");
		factory.setDestinationId("foo4");
		String[] channels = new String[] {"my-amf", "my-polling-amf"};
		factory.setChannels(channels);
		
		factory.setMessageBroker(getMessageBroker());
		factory.setBeanFactory(beanFactory);
		
		factory.afterPropertiesSet();
		
		MessageDestination destination = (MessageDestination) service.getDestination("foo4");
		assertNotNull(destination);
		assertEquals("foo4", destination.getId());
		assertTrue(destination.getChannels().containsAll(Arrays.asList(channels)));
		assertTrue(destination.getServerSettings().getAllowSubtopics());
		assertTrue(destination.getServerSettings().isBroadcastRoutingMode());
		assertEquals(1, destination.getServerSettings().getMessageTTL());
		assertEquals(1, destination.getNetworkSettings().getSubscriptionTimeoutMinutes());
		assertEquals("/", destination.getServerSettings().getSubtopicSeparator());
		assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getIncomingDestinationFrequency());
		assertEquals(ThrottleSettings.POLICY_ERROR, destination.getNetworkSettings().getThrottleSettings().getInboundPolicy());
		assertEquals(500, destination.getNetworkSettings().getThrottleSettings().getOutgoingDestinationFrequency());
		assertEquals(ThrottleSettings.POLICY_IGNORE, destination.getNetworkSettings().getThrottleSettings().getOutboundPolicy());
		
	}
	
	private String readJsonFile() throws Exception {
		Resource jsonFile = new DefaultResourceLoader().getResource("classpath:org/springframework/flex/messaging/MessageDestinationProps.json");
		BufferedReader br = new BufferedReader(new InputStreamReader(jsonFile.getInputStream()));
		StringBuilder builder = new StringBuilder();
		String line;
		while((line = br.readLine()) != null){
			builder.append(line);
		}
		return builder.toString();
	}

}
