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

package org.springframework.flex.security3;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.event.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class FlexSessionInvalidatingAuthenticationListenerTests extends TestCase {

    private @Mock
    Authentication authentication;

    private @Mock
    RequestAttributes attributes;

    private final FlexSessionInvalidatingAuthenticationListener listener = new FlexSessionInvalidatingAuthenticationListener();

    @Override
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    public void testAuthenticationEvent_InvalidConfiguration() {

        RequestContextHolder.resetRequestAttributes();

        InteractiveAuthenticationSuccessEvent event = new InteractiveAuthenticationSuccessEvent(this.authentication, UsernamePasswordAuthenticationFilter.class);

        this.listener.onApplicationEvent(event);

        verify(this.attributes, never()).removeAttribute("__flexSession", RequestAttributes.SCOPE_SESSION);
    }

    public void testAuthenticationEvent_RemoveFlexSession() {

        RequestContextHolder.setRequestAttributes(this.attributes);

        InteractiveAuthenticationSuccessEvent event = new InteractiveAuthenticationSuccessEvent(this.authentication, UsernamePasswordAuthenticationFilter.class);

        this.listener.onApplicationEvent(event);

        verify(this.attributes).removeAttribute("__flexSession", RequestAttributes.SCOPE_SESSION);
    }

}
