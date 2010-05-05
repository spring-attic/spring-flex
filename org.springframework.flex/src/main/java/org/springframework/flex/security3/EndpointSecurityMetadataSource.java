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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.access.intercept.RequestKey;
import org.springframework.security.web.util.UrlMatcher;
import org.springframework.util.Assert;

import flex.messaging.endpoints.Endpoint;

/**
 * 
 * Implementation of {@link SecurityMetadataSource} for BlazeDS {@link Endpoint}s.
 * 
 * <p>
 * This implementation is capable of securing Endpoints both by their channel id, and by their URL pattern.
 * 
 * @author Jeremy Grelle
 */

public class EndpointSecurityMetadataSource extends DefaultFilterInvocationSecurityMetadataSource {

    private Map<String, Collection<ConfigAttribute>> endpointMap = new HashMap<String, Collection<ConfigAttribute>>();

    /**
     * @see DefaultFilterInvocationDefinitionSource#DefaultFilterInvocationDefinitionSource(UrlMatcher, LinkedHashMap)
     */
    public EndpointSecurityMetadataSource(UrlMatcher urlMatcher, LinkedHashMap<RequestKey, Collection<ConfigAttribute>> requestMap) {
        super(urlMatcher, requestMap);
    }

    /**
     * Builds the internal request map from the supplied map, and stores the endpoint map for matching by channel id.
     * 
     * @param endpointMap map of <String, Collection<ConfigAttribute>>
     * @see DefaultFilterInvocationDefinitionSource#DefaultFilterInvocationDefinitionSource(UrlMatcher, LinkedHashMap)
     */
    public EndpointSecurityMetadataSource(UrlMatcher urlMatcher, LinkedHashMap<RequestKey, Collection<ConfigAttribute>> requestMap,
        HashMap<String, Collection<ConfigAttribute>> endpointMap) {
        super(urlMatcher, requestMap);
        Assert.notNull(endpointMap, "endpointMap cannot be null");
        this.endpointMap = endpointMap;
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (object == null || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be an Endpoint");
        }

        Endpoint endpoint = (Endpoint) object;
        Collection<ConfigAttribute> attributes = null;

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
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        Collection<ConfigAttribute> pathDefinitions = super.getAllConfigAttributes();
        List<ConfigAttribute> allDefinitions = new ArrayList<ConfigAttribute>();
        for (Collection<ConfigAttribute> attrValues : this.endpointMap.values()) {
            allDefinitions.addAll(attrValues);
        }
        allDefinitions.addAll(pathDefinitions);
        return Collections.unmodifiableCollection(allDefinitions);
    }

    /**
     * 
     * {@inheritDoc}
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return Endpoint.class.isAssignableFrom(clazz);
    }
}
