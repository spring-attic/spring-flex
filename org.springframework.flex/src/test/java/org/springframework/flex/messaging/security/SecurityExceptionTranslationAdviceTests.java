package org.springframework.flex.messaging.security;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.AuthenticationException;

import flex.messaging.MessageException;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;
import flex.messaging.security.SecurityException;

public class SecurityExceptionTranslationAdviceTests extends TestCase{
	
	@Mock private AbstractEndpoint endpoint; 
	@Mock private Message inMessage;
	@Mock private Message outMessage;
	private AbstractEndpoint advisedEndpoint;
	
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		ProxyFactory factory = new ProxyFactory();
		factory.setProxyTargetClass(true);
		factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(new SecurityExceptionTranslationAdvice()));
		factory.setTarget(endpoint);
		advisedEndpoint = (AbstractEndpoint) factory.getProxy();
	}
	
	public void testAuthorizationException() {
		
		String error = "Invalid authentication";
		when(endpoint.serviceMessage(inMessage)).thenThrow(new AuthenticationCredentialsNotFoundException(error));
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail();
		} catch (SecurityException ex) {
			assertEquals(error, ex.getMessage());
			assertEquals(SecurityException.CLIENT_AUTHENTICATION_CODE, ex.getCode());
			assertTrue(ex.getRootCause() instanceof AuthenticationException);
		}
	}
	
	public void testAccessDeniedException() {
		
		String error = "Access is denied";
		
		when(endpoint.serviceMessage(inMessage)).thenThrow(new AccessDeniedException(error));
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail();
		} catch (SecurityException ex) {
			assertEquals(error, ex.getMessage());
			assertEquals(SecurityException.CLIENT_AUTHORIZATION_CODE, ex.getCode());
			assertTrue(ex.getRootCause() instanceof AccessDeniedException);
		}
	}
	
	public void testUnknownExceptionPassthrough() {
		
		MessageException expected = new MessageException();
		when(endpoint.serviceMessage(inMessage)).thenThrow(expected);
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail();
		} catch (MessageException ex) {
			assertSame(expected, ex);
		}
	}
	
	public void testNormalReturnPassthrough() {
		
		when(endpoint.serviceMessage(inMessage)).thenReturn(outMessage);
		
		Message result = advisedEndpoint.serviceMessage(inMessage);
		
		assertSame(outMessage, result);
	}

}
