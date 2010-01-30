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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.RequestKey;
import org.springframework.security.web.util.AntUrlPathMatcher;

import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;

public class EndpointSecurityMetadataSourceTests extends TestCase {

    private @Mock
    Endpoint mockEndpoint;

    private EndpointSecurityMetadataSource source;

    public void testGetAttributes_ForProtectedEndpointId() {
        HashMap<String, Collection<ConfigAttribute>> endpointMap = new HashMap<String, Collection<ConfigAttribute>>();
        List<ConfigAttribute> attrs = new ArrayList<ConfigAttribute>();
        attrs.add(new SecurityConfig("ROLE_USER"));
        endpointMap.put("foo", attrs);
        this.source = new EndpointSecurityMetadataSource(new AntUrlPathMatcher(), new LinkedHashMap<RequestKey, Collection<ConfigAttribute>>(), endpointMap);

        when(this.mockEndpoint.getId()).thenReturn("foo");

        Collection<ConfigAttribute> def = this.source.getAttributes(this.mockEndpoint);

        assertTrue(def.size() > 0);
    }

    public void testGetAttributes_ForProtectedURL() {
        List<ConfigAttribute> attrs = new ArrayList<ConfigAttribute>();
        attrs.add(new SecurityConfig("ROLE_USER"));
        LinkedHashMap<RequestKey, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestKey, Collection<ConfigAttribute>>();
        requestMap.put(new RequestKey("**/messagebroker/**"), attrs);
        this.source = new EndpointSecurityMetadataSource(new AntUrlPathMatcher(), requestMap);

        when(this.mockEndpoint.getUrlForClient()).thenReturn("http://localhost:8080/app/spring/messagebroker/amf");

        Collection<ConfigAttribute> def = this.source.getAttributes(this.mockEndpoint);

        assertTrue(def.size() > 0);
    }

    public void testSupportsEndpoint() {
        this.source = new EndpointSecurityMetadataSource(new AntUrlPathMatcher(), new LinkedHashMap<RequestKey, Collection<ConfigAttribute>>());

        assertTrue(this.source.supports(Endpoint.class));
        assertTrue(this.source.supports(this.mockEndpoint.getClass()));
        assertTrue(this.source.supports(AMFEndpoint.class));
    }

    @Override
    protected void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
