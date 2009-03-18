package org.springframework.flex.security;

import junit.framework.TestCase;

import org.springframework.flex.security.SecurityExceptionTranslator;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationCredentialsNotFoundException;
import org.springframework.security.AuthenticationException;

import flex.messaging.MessageException;
import flex.messaging.security.SecurityException;

public class SecurityExceptionTranslatorTests extends TestCase {

	private SecurityExceptionTranslator translator;
	
	public void setUp() {
		translator = new SecurityExceptionTranslator();
	}
	
	public void testAuthorizationException() {
		
		String error = "Invalid authentication";
		MessageException ex = translator.translate(new AuthenticationCredentialsNotFoundException(error));
		assertTrue("Should be a SecurityException", ex instanceof SecurityException);		
		assertEquals(error, ex.getMessage());
		assertEquals(SecurityException.CLIENT_AUTHENTICATION_CODE, ex.getCode());
		assertTrue(ex.getRootCause() instanceof AuthenticationException);

	}

	public void testAccessDeniedException() {
		
		String error = "Access is denied";
		
		MessageException ex = translator.translate(new AccessDeniedException(error));
		assertTrue("Should be a SecurityException", ex instanceof SecurityException);	
		assertEquals(error, ex.getMessage());
		assertEquals(SecurityException.CLIENT_AUTHORIZATION_CODE, ex.getCode());
		assertTrue(ex.getRootCause() instanceof AccessDeniedException);
		
	}
	
	public void testUnknownExceptionPassthrough() {
		
		MessageException expected = new MessageException();
		assertNull(translator.translate(expected));
	}

}
