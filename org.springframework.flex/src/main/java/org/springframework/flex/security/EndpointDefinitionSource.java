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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.ObjectDefinitionSource;
import org.springframework.security.intercept.web.DefaultFilterInvocationDefinitionSource;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.util.UrlMatcher;
import org.springframework.util.Assert;

import flex.messaging.endpoints.Endpoint;

/**
 * 
 * Implementation of {@link ObjectDefinitionSource} for BlazeDS {@link Endpoint}s.
 * 
 * <p>
 * This implementation is capable of securing Endpoints both by their channel id, and by their URL pattern.
 * 
 * @author Jeremy Grelle
 */

public class EndpointDefinitionSource extends DefaultFilterInvocationDefinitionSource {

    private Map<String, ConfigAttributeDefinition> endpointMap = new HashMap<String, ConfigAttributeDefinition>();

    /**
     * @see DefaultFilterInvocationDefinitionSource#DefaultFilterInvocationDefinitionSource(UrlMatcher, LinkedHashMap)
     */
    public EndpointDefinitionSource(UrlMatcher urlMatcher, LinkedHashMap<RequestKey, ConfigAttributeDefinition> requestMap) {
        super(urlMatcher, requestMap);
    }

    /**
     * Builds the internal request map from the supplied map, and stores the endpoint map for matching by channel id.
     * 
     * @param endpointMap map of <String, ConfigAttributeDefinition>
     * @see DefaultFilterInvocationDefinitionSource#DefaultFilterInvocationDefinitionSource(UrlMatcher, LinkedHashMap)
     */
    public EndpointDefinitionSource(UrlMatcher urlMatcher, LinkedHashMap<RequestKey, ConfigAttributeDefinition> requestMap,
        HashMap<String, ConfigAttributeDefinition> endpointMap) {
        super(urlMatcher, requestMap);
        Assert.notNull(endpointMap, "endpointMap cannot be null");
        this.endpointMap = endpointMap;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public ConfigAttributeDefinition getAttributes(Object object) throws IllegalArgumentException {
        if (object == null || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be an Endpoint");
        }

        Endpoint endpoint = (Endpoint) object;
        ConfigAttributeDefinition attributes = null;

        if (this.endpointMap.containsKey(endpoint.getId())) {
            attributes = this.endpointMap.get(endpoint.getId());
        } else {
            attributes = lookupAttributes(endpoint.getUrlForClient(), null);
        }
        return attributes;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public Collection getConfigAttributeDefinitions() {
        Collection pathDefinitions = super.getConfigAttributeDefinitions();
        LinkedHashSet allDefinitions = new LinkedHashSet();
        allDefinitions.addAll(this.endpointMap.values());
        allDefinitions.addAll(pathDefinitions);
        return Collections.unmodifiableCollection(allDefinitions);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean supports(Class clazz) {
        return Endpoint.class.isAssignableFrom(clazz);
    }
}
