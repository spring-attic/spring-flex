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

import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.LinkedHashMap;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.util.AntUrlPathMatcher;

import flex.messaging.endpoints.AMFEndpoint;
import flex.messaging.endpoints.Endpoint;

public class EndpointDefinitionSourceTests extends TestCase {

    private @Mock
    Endpoint mockEndpoint;

    private EndpointDefinitionSource source;

    @SuppressWarnings("unchecked")
    public void testGetAttributes_ForProtectedEndpointId() {
        HashMap endpointMap = new HashMap();
        endpointMap.put("foo", new ConfigAttributeDefinition("ROLE_USER"));
        this.source = new EndpointDefinitionSource(new AntUrlPathMatcher(), new LinkedHashMap(), endpointMap);

        when(this.mockEndpoint.getId()).thenReturn("foo");

        ConfigAttributeDefinition def = this.source.getAttributes(this.mockEndpoint);

        assertTrue(def.getConfigAttributes().size() > 0);
    }

    @SuppressWarnings("unchecked")
    public void testGetAttributes_ForProtectedURL() {
        LinkedHashMap requestMap = new LinkedHashMap();
        requestMap.put(new RequestKey("**/messagebroker/**"), new ConfigAttributeDefinition("ROLE_USER"));
        this.source = new EndpointDefinitionSource(new AntUrlPathMatcher(), requestMap);

        when(this.mockEndpoint.getUrlForClient()).thenReturn("http://localhost:8080/app/spring/messagebroker/amf");

        ConfigAttributeDefinition def = this.source.getAttributes(this.mockEndpoint);

        assertTrue(def.getConfigAttributes().size() > 0);
    }

    @SuppressWarnings("unchecked")
    public void testSupportsEndpoint() {
        this.source = new EndpointDefinitionSource(new AntUrlPathMatcher(), new LinkedHashMap());

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
