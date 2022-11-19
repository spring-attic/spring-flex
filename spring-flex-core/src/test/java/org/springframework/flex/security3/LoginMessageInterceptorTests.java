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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class LoginMessageInterceptorTests {

    private CommandMessage inputMessage;

    private AcknowledgeMessage outputMessage;

    private LoginMessageInterceptor interceptor;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.interceptor = new LoginMessageInterceptor();
    }

    @Test
    public void postProcessPassThrough() {

        this.inputMessage = new CommandMessage(CommandMessage.CLIENT_PING_OPERATION);
        this.outputMessage = new AcknowledgeMessage();
        assertSame(this.outputMessage, this.interceptor.postProcess(null, this.inputMessage, this.outputMessage));
    }

    @Test
    @SuppressWarnings("rawtypes")
    public void postProcessSuccessfulLogin() {
        List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
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

    @Test
    public void preProcessPassThrough() {

        this.inputMessage = new CommandMessage(CommandMessage.LOGIN_OPERATION);

        assertSame(this.inputMessage, this.interceptor.preProcess(null, this.inputMessage));
    }

}
