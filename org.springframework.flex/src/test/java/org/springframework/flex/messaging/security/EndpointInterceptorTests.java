package org.springframework.flex.messaging.security;

import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.AccessDecisionManager;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.Authentication;
import org.springframework.security.AuthenticationException;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.MockAuthenticationManager;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
import org.springframework.security.util.AntUrlPathMatcher;
import org.springframework.security.vote.AffirmativeBased;
import org.springframework.security.vote.RoleVoter;

import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;

public class EndpointInterceptorTests extends TestCase {

	private MockAuthenticationManager mgr = new MockAuthenticationManager();
	private AccessDecisionManager adm = new AffirmativeBased();
	
	@Mock private AbstractEndpoint endpoint; 
	@Mock private Message inMessage;
	@Mock private Message outMessage;
	private AbstractEndpoint advisedEndpoint;
	
	@SuppressWarnings("unchecked")
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		LinkedHashMap requestMap = new LinkedHashMap();
		requestMap.put(new RequestKey("**/messagebroker/amf"),
				new ConfigAttributeDefinition("ROLE_USER"));
		EndpointDefinitionSource source = new EndpointDefinitionSource(new AntUrlPathMatcher(),
				requestMap);
		
		List voters = new ArrayList();
		voters.add(new RoleVoter());
		((AffirmativeBased) adm).setDecisionVoters(voters);
		
		EndpointInterceptor interceptor;
		interceptor = new EndpointInterceptor();
		interceptor.setAuthenticationManager(mgr);
		interceptor.setAccessDecisionManager(adm);
		interceptor.setObjectDefinitionSource(source);
		
		ProxyFactory factory = new ProxyFactory();
		factory.setProxyTargetClass(true);
		factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(interceptor));
		factory.setTarget(endpoint);
		advisedEndpoint = (AbstractEndpoint) factory.getProxy();
	}
	
	public void tearDown() {
		SecurityContextHolder.getContext().setAuthentication(null);
	}
	
	public void testServiceUnauthenticated() throws Exception {

		when(endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amf");
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail("An AuthenticationException should be thrown");
		} catch (AuthenticationException ex) {
			//expected
		}
	}
	
	public void testServiceUnauthorized() throws Exception {

		when(endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amf");
		
		Authentication auth = new UsernamePasswordAuthenticationToken("foo",
				"bar", new GrantedAuthority[] {});
		SecurityContextHolder.getContext().setAuthentication(auth);

		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail("An AccessDeniedException should be thrown");
		} catch (AccessDeniedException ex) {
			//expected
		}
	} 
	
	public void testServiceAuthorized() throws Exception {
		when(endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amf");
		when(endpoint.serviceMessage(inMessage)).thenReturn(outMessage);
		
		Authentication auth = new UsernamePasswordAuthenticationToken("foo",
				"bar", new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER")});
		SecurityContextHolder.getContext().setAuthentication(auth);

		Message result = advisedEndpoint.serviceMessage(inMessage);
		
		assertSame(outMessage, result);	
	}
	
	public void testServiceUnsecured() throws Exception {
		when(endpoint.getUrlForClient()).thenReturn("http://foo.com/bar/spring/messagebroker/amfpolling");
		when(endpoint.serviceMessage(inMessage)).thenReturn(outMessage);

		Message result = advisedEndpoint.serviceMessage(inMessage);
		
		assertSame(outMessage, result);	
	}
	
	
}
