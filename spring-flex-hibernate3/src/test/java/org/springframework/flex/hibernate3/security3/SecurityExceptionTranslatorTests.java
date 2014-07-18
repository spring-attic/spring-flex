/*
 * Copyright 2002-2009 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.hibernate3.security3;

import org.springframework.flex.security3.SecurityExceptionTranslator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;

import flex.messaging.MessageException;
import flex.messaging.security.SecurityException;

import junit.framework.TestCase;

public class SecurityExceptionTranslatorTests extends TestCase {

	private SecurityExceptionTranslator translator;

	@Override
	public void setUp() {
		this.translator = new SecurityExceptionTranslator();
	}

	public void testAccessDeniedException() {

		String error = "Access is denied";

		MessageException ex = this.translator.translate(new AccessDeniedException(error));
		assertTrue("Should be a SecurityException", ex instanceof SecurityException);
		assertEquals(error, ex.getMessage());
		assertEquals(SecurityException.CLIENT_AUTHORIZATION_CODE, ex.getCode());
		assertTrue(ex.getRootCause() instanceof AccessDeniedException);

	}

	public void testAuthorizationException() {

		String error = "Invalid authentication";
		MessageException ex = this.translator.translate(new AuthenticationCredentialsNotFoundException(error));
		assertTrue("Should be a SecurityException", ex instanceof SecurityException);
		assertEquals(error, ex.getMessage());
		assertEquals(SecurityException.CLIENT_AUTHENTICATION_CODE, ex.getCode());
		assertTrue(ex.getRootCause() instanceof AuthenticationException);

	}

	public void testUnknownExceptionPassthrough() {

		MessageException expected = new MessageException();
        assertNull(this.translator.translate(expected));
    }

}
