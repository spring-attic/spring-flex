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

import org.junit.After;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.util.matcher.RequestMatcher;

import flex.messaging.FlexContext;
import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;

public class EndpointSecurityMetadataSourceTests {

    private @Mock
    Endpoint mockEndpoint;
    
    private MockHttpServletRequest request;

    private EndpointSecurityMetadataSource source;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        this.request = new MockHttpServletRequest();
        FlexContext.setThreadLocalHttpRequest(this.request);
    }

    @After
    public void tearDown() throws Exception {
        FlexContext.clearThreadLocalObjects();        
    }

    @Test
    public void forProtectedEndpointId() {
        HashMap<String, Collection<ConfigAttribute>> endpointMap = new HashMap<String, Collection<ConfigAttribute>>();
        List<ConfigAttribute> attrs = new ArrayList<ConfigAttribute>();
        attrs.add(new SecurityConfig("ROLE_USER"));
        endpointMap.put("foo", attrs);
        this.source = new EndpointSecurityMetadataSource(new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>(), endpointMap);

        when(this.mockEndpoint.getId()).thenReturn("foo");

        Collection<ConfigAttribute> def = this.source.getAttributes(this.mockEndpoint);

        assertTrue(def.size() > 0);
    }

    @Test
    public void forProtectedURL() {
        List<ConfigAttribute> attrs = new ArrayList<ConfigAttribute>();
        attrs.add(new SecurityConfig("ROLE_USER"));
        LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();
        requestMap.put(new AntPathRequestMatcher("/messagebroker/**"), attrs);
        this.source = new EndpointSecurityMetadataSource(requestMap);

        this.request.setServletPath("/messagebroker");
        this.request.setPathInfo("/amf");
        
        Collection<ConfigAttribute> def = this.source.getAttributes(this.mockEndpoint);

        assertTrue(def.size() > 0);
    }

    @Test
    public void supportsEndpoint() {
        this.source = new EndpointSecurityMetadataSource(new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>());

        assertTrue(this.source.supports(Endpoint.class));
        assertTrue(this.source.supports(this.mockEndpoint.getClass()));
        assertTrue(this.source.supports(AMFEndpoint.class));
    }
}
