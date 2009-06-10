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
public class SecurityExceptionTranslator implements ExceptionTranslator {

    /**
     * 
     * {@inheritDoc}
     */
    public boolean handles(Class<?> clazz) {
        return ClassUtils.isAssignable(AuthenticationException.class, clazz) || ClassUtils.isAssignable(AccessDeniedException.class, clazz);
    }

    /**
     * 
     * {@inheritDoc}
     */
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

}
