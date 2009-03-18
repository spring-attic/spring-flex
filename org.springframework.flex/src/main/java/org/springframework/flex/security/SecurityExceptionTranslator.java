package org.springframework.flex.security;

import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.security.AccessDeniedException;
import org.springframework.security.AuthenticationException;
import org.springframework.util.ClassUtils;

import flex.messaging.MessageException;
import flex.messaging.security.SecurityException;

/**
 * Translates SpringSecurityExceptions to appropriate BlazeDS SecurityExceptions.
 * 
 * @author Jeremy Grelle
 */
public class SecurityExceptionTranslator implements ExceptionTranslator{

	public MessageException translate(Throwable t) {
		if (t instanceof AuthenticationException) {
			SecurityException se = new SecurityException();
			se.setCode(SecurityException.CLIENT_AUTHENTICATION_CODE);
			se.setMessage(t.getLocalizedMessage());
			se.setRootCause(t);
			return se;
		} else if (t instanceof AccessDeniedException) {
			SecurityException se = new SecurityException();
			se.setCode(SecurityException.CLIENT_AUTHORIZATION_CODE);
			se.setMessage(t.getLocalizedMessage());
			se.setRootCause(t);
			return se;
		}
		return null;
	}

	public boolean handles(Class<?> clazz) {
		return ClassUtils.isAssignable(AuthenticationException.class, clazz) ||
			ClassUtils.isAssignable(AccessDeniedException.class, clazz);
	}

}
