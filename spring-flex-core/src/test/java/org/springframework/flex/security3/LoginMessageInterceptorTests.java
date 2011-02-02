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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContextHolder;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class LoginMessageInterceptorTests extends TestCase {

    private CommandMessage inputMessage;

    private AcknowledgeMessage outputMessage;

    private LoginMessageInterceptor interceptor;

    public final void testPostProcessPassThrough() {

        this.inputMessage = new CommandMessage(CommandMessage.CLIENT_PING_OPERATION);
        this.outputMessage = new AcknowledgeMessage();
        assertSame(this.outputMessage, this.interceptor.postProcess(null, this.inputMessage, this.outputMessage));
    }

    @SuppressWarnings("rawtypes")
	public final void testPostProcessSuccessfulLogin() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new GrantedAuthorityImpl("ROLE_USER"));
        Authentication auth = new UsernamePasswordAuthenticationToken("foo", "bar", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);

        this.inputMessage = new CommandMessage(CommandMessage.LOGIN_OPERATION);
        this.outputMessage = new AcknowledgeMessage();
        this.outputMessage.setBody("success");

        Message result = this.interceptor.postProcess(null, this.inputMessage, this.outputMessage);

        assertTrue(result.getBody() instanceof Map);
        Map authResult = (Map) result.getBody();
        assertEquals("foo", authResult.get("name"));
        assertEquals("ROLE_USER", ((String[]) authResult.get("authorities"))[0]);
    }

    public final void testPreProcessPassThrough() {

        this.inputMessage = new CommandMessage(CommandMessage.LOGIN_OPERATION);

        assertSame(this.inputMessage, this.interceptor.preProcess(null, this.inputMessage));
    }

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.interceptor = new LoginMessageInterceptor();
    }

}
