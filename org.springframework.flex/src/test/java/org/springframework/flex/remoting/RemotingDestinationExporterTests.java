package org.springframework.flex.remoting;

import static org.mockito.Mockito.*;

import java.util.Iterator;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.flex.core.AbstractMessageBrokerTests;
import org.springframework.util.CollectionUtils;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;
import flex.messaging.services.ServiceAdapter;
import flex.messaging.services.remoting.RemotingDestination;
import flex.messaging.services.remoting.adapters.JavaAdapter;
import flex.messaging.services.remoting.adapters.RemotingMethod;

public class RemotingDestinationExporterTests extends AbstractMessageBrokerTests {

	private static final String DEFAULT_SERVICE_ID = "myServiceExporter";

	RemotingDestinationExporter exporter;

	Object testService = new StubService();
	
	private @Mock BeanFactory beanFactory;

	public void setUp() throws Exception {
		if (getCurrentConfigPath() != getServicesConfigPath()){
			setDirty();
		}		
		configureExporter();
		MockitoAnnotations.initMocks(this);
		exporter.setBeanFactory(beanFactory);
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

	public void testDestinationRegisteredWithDestinationId() throws Exception {

		RemotingService remotingService = getRemotingService();

		String destinationId = "myService";
		exporter.setDestinationId(destinationId);
		exporter.afterPropertiesSet();

		assertNotNull("RemotingDestination not registered", remotingService.getDestination(destinationId));
	}
	
	public void testDestinationConfiguredWithSpringManagedDefaultAdapter() throws Exception {
		
		String adapterId = "java-object";
		ServiceAdapter springAdapter = new TestAdapter();
		springAdapter.setId(adapterId);
		when(beanFactory.containsBean(adapterId)).thenReturn(true);
		when(beanFactory.getBean(adapterId, ServiceAdapter.class)).thenReturn(springAdapter);
		
		RemotingService remotingService = getRemotingService();

		exporter.afterPropertiesSet();

		RemotingDestination remotingDestination = (RemotingDestination) remotingService
				.getDestination(DEFAULT_SERVICE_ID);
		
		assertSame(springAdapter, remotingDestination.getAdapter());
	}
	
	public void testDestinationConfiguredWithSpringManagedCustomAdapter() throws Exception {
		
		String adapterId = "my-adapter";
		ServiceAdapter springAdapter = new TestAdapter();
		springAdapter.setId(adapterId);
		when(beanFactory.containsBean(adapterId)).thenReturn(true);
		when(beanFactory.getBean(adapterId, ServiceAdapter.class)).thenReturn(springAdapter);
		
		RemotingService remotingService = getRemotingService();

		exporter.setServiceAdapter(adapterId);
		exporter.afterPropertiesSet();

		RemotingDestination remotingDestination = (RemotingDestination) remotingService
				.getDestination(DEFAULT_SERVICE_ID);
		
		assertSame(springAdapter, remotingDestination.getAdapter());
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
	
	@SuppressWarnings("unchecked")
	public void testDestinationConfiguredWithValidChannels() throws Exception {
		RemotingService remotingService = getRemotingService();
		
		String[] channelIds = new String[]{"my-secure-amf"};
		exporter.setChannels(channelIds);
		
		exporter.afterPropertiesSet();
		
		RemotingDestination remotingDestination = (RemotingDestination) remotingService
			.getDestination(DEFAULT_SERVICE_ID);
		assertTrue("Custom channels not set",remotingDestination.getChannels().containsAll(CollectionUtils.arrayToList(channelIds)));
		assertFalse("Default channel not overriden", remotingDestination.getChannels().contains("my-amf"));
	}
	
	public void testDestinationConfiguredWithInvalidChannels() throws Exception {
		String[] channelIds = new String[]{"my-fubar"};
		exporter.setChannels(channelIds);
		
		try {
			exporter.afterPropertiesSet();
			fail("Invalid channel not detected");
		} catch (IllegalArgumentException ex) {
			// Expected
		}
	}

	private RemotingService getRemotingService() throws Exception {
		MessageBroker broker = getMessageBroker();

		return (RemotingService) broker.getServiceByType(RemotingService.class.getName());
	}

	private void configureExporter() throws Exception {

		exporter = new RemotingDestinationExporter();
		exporter.setBeanName(DEFAULT_SERVICE_ID);
		exporter.setMessageBroker(getMessageBroker());
		exporter.setService(testService);
	}

	private static final class StubService {

		public String retreiveStringValue() {
			return "foo";
		}
	}
	
	private static final class TestAdapter extends JavaAdapter { }
}
