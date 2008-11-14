package org.springframework.flex.messaging;

import java.util.Iterator;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class FlexRemotingServiceExporterTests extends AbstractMessageBrokerTests {

	private static final String DEFAULT_SERVICE_ID = "myServiceExporter";

	FlexRemotingServiceExporter exporter;

	Object testService = new StubService();

	public void setUp() throws Exception {
		configureExporter();
	}

	public void tearDown() throws Exception {
		exporter.destroy();
	}

	public void testDestinationRegisteredWithDefaultConfig() throws Exception {

		RemotingService remotingService = getRemotingService();

		exporter.afterPropertiesSet();

		RemotingDestination remotingDestination = (RemotingDestination) remotingService
				.getDestination(DEFAULT_SERVICE_ID);
		assertNotNull("RemotingDestination not registered", remotingDestination);
		assertTrue("RemotingDestination not started", remotingDestination.isStarted());
		assertEquals("Default adapter not set", "java-object", remotingDestination.getAdapter().getId());
		assertTrue("No channels set on destination", remotingDestination.getChannels().size() > 0);
	}

	public void testDestinationRegisteredWithServiceId() throws Exception {

		RemotingService remotingService = getRemotingService();

		String serviceId = "myService";
		exporter.setServiceId(serviceId);
		exporter.afterPropertiesSet();

		assertNotNull("RemotingDestination not registered", remotingService.getDestination(serviceId));
	}

	public void testDestinationConfiguredWithNullService() throws Exception {

		exporter.setService(null);
		try {
			exporter.afterPropertiesSet();
			fail("Invalid service not detected.");
		} catch (IllegalArgumentException ex) {
			// Expected
		}
	}

	public void testDestinationConfiguredWithNullMessageBroker() throws Exception {

		exporter.setMessageBroker(null);
		try {
			exporter.afterPropertiesSet();
			fail("Invalid MessageBroker not detected.");
		} catch (IllegalArgumentException ex) {
			// Expected
		}
	}

	public void testDestinationConfiguredWithValidIncludeMethods() throws Exception {

		RemotingService remotingService = getRemotingService();

		String methodName = "retreiveStringValue";
		exporter.setIncludeMethods(new String[] { methodName });
		exporter.afterPropertiesSet();

		assertTrue("The remoting destination was not configured with a JavaAdapter", remotingService
				.getDestination(DEFAULT_SERVICE_ID).getAdapter() instanceof JavaAdapter);
		JavaAdapter adapter = (JavaAdapter) remotingService.getDestination(DEFAULT_SERVICE_ID).getAdapter();
		Iterator<?> i = adapter.getIncludeMethodIterator();
		RemotingMethod method = (RemotingMethod) i.next();
		assertEquals("Include method not properly configured", methodName, method.getName());
		
	}
	
	public void testDestinationConfiguredWithInvalidIncludeMethods() throws Exception {

		String methodName = "retreiveStringValues";
		exporter.setIncludeMethods(new String[] { methodName });
		try {
			exporter.afterPropertiesSet();
			fail("Invalid include method not detected.");
		} catch (IllegalArgumentException ex) {
			// Expected
		}
	}
	
	public void testDestinationConfiguredWithValidExcludeMethods() throws Exception {

		RemotingService remotingService = getRemotingService();

		String methodName = "retreiveStringValue";
		exporter.setExcludeMethods(new String[] { methodName });
		exporter.afterPropertiesSet();

		assertTrue("The remoting destination was not configured with a JavaAdapter", remotingService
				.getDestination(DEFAULT_SERVICE_ID).getAdapter() instanceof JavaAdapter);
		JavaAdapter adapter = (JavaAdapter) remotingService.getDestination(DEFAULT_SERVICE_ID).getAdapter();
		Iterator<?> i = adapter.getExcludeMethodIterator();
		RemotingMethod method = (RemotingMethod) i.next();
		assertEquals("Exclude method not properly configured", methodName, method.getName());
		
	}
	
	public void testDestinationConfiguredWithInvalidExcludeMethods() throws Exception {

		String methodName = "retreiveStringValues";
		exporter.setExcludeMethods(new String[] { methodName });
		try {
			exporter.afterPropertiesSet();
			fail("Invalid exclude method not detected.");
		} catch (IllegalArgumentException ex) {
			// Expected
		}
	}

	private RemotingService getRemotingService() throws Exception {
		MessageBroker broker = getMessageBroker();

		return (RemotingService) broker.getServiceByType(RemotingService.class.getName());
	}

	private void configureExporter() throws Exception {

		exporter = new FlexRemotingServiceExporter();
		exporter.setBeanName(DEFAULT_SERVICE_ID);
		exporter.setMessageBroker(getMessageBroker());
		exporter.setService(testService);
	}

	private class StubService {

		public String retreiveStringValue() {
			return "foo";
		}
	}
}
