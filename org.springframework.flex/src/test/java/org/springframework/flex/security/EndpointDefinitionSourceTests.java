package org.springframework.flex.security;

import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.flex.security.EndpointDefinitionSource;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.util.AntUrlPathMatcher;

import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;
import junit.framework.TestCase;

public class EndpointDefinitionSourceTests extends TestCase {

	private @Mock Endpoint mockEndpoint;
	
	private EndpointDefinitionSource source;
	
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}
	
	@SuppressWarnings("unchecked")
	public void testGetAttributes_ForProtectedURL() {
		LinkedHashMap requestMap = new LinkedHashMap();
		requestMap.put(new RequestKey("**/messagebroker/**"), new ConfigAttributeDefinition("ROLE_USER"));
		source = new EndpointDefinitionSource(new AntUrlPathMatcher(), requestMap);
		
		when(mockEndpoint.getUrlForClient()).thenReturn("http://localhost:8080/app/spring/messagebroker/amf");
		
		ConfigAttributeDefinition def = source.getAttributes(mockEndpoint);
		
		assertTrue(def.getConfigAttributes().size() > 0);
	}
	
	@SuppressWarnings("unchecked")
	public void testGetAttributes_ForProtectedEndpointId() {
		HashMap endpointMap = new HashMap();
		endpointMap.put("foo", new ConfigAttributeDefinition("ROLE_USER"));
		source = new EndpointDefinitionSource(new AntUrlPathMatcher(), new LinkedHashMap(), endpointMap);
		
		when(mockEndpoint.getId()).thenReturn("foo");
		
		ConfigAttributeDefinition def = source.getAttributes(mockEndpoint);
		
		assertTrue(def.getConfigAttributes().size() > 0);
	}
	
	@SuppressWarnings("unchecked")
	public void testSupportsEndpoint() {
		source = new EndpointDefinitionSource(new AntUrlPathMatcher(), new LinkedHashMap());
		
		assertTrue(source.supports(Endpoint.class));
		assertTrue(source.supports(mockEndpoint.getClass()));
		assertTrue(source.supports(AMFEndpoint.class));
	}

}
