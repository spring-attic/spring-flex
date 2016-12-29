/*
 * Copyright 2002-2011 the original author or authors.
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

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityMetadataSource;
import org.springframework.security.web.access.intercept.DefaultFilterInvocationSecurityMetadataSource;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import flex.messaging.FlexContext;
import flex.messaging.endpoints.Endpoint;

/**
 * 
 * Implementation of {@link SecurityMetadataSource} for BlazeDS {@link Endpoint Endpoints}.
 * 
 * <p>
 * This implementation is capable of securing Endpoints both by their channel id, and by their URL pattern.
 * 
 * @author Jeremy Grelle
 */

public class EndpointSecurityMetadataSource implements SecurityMetadataSource {
	
	private Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = new LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>>();

    private Map<String, Collection<ConfigAttribute>> endpointMap = new LinkedHashMap<String, Collection<ConfigAttribute>>();

    /**
     * @see DefaultFilterInvocationSecurityMetadataSource#DefaultFilterInvocationSecurityMetadataSource(UrlMatcher, LinkedHashMap)
     */
    public EndpointSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap) {
    	Assert.notNull(requestMap, "requestMap cannot be null");
        this.requestMap = requestMap;
    }

    /**
     * Builds the internal request map from the supplied map, and stores the endpoint map for matching by channel id.
     * 
     * @param endpointMap map of &lt;String, Collection&lt;ConfigAttribute&gt;&gt;
     * @see DefaultFilterInvocationSecurityMetadataSource#DefaultFilterInvocationSecurityMetadataSource(UrlMatcher, LinkedHashMap)
     */
    public EndpointSecurityMetadataSource(LinkedHashMap<RequestMatcher, Collection<ConfigAttribute>> requestMap,
        HashMap<String, Collection<ConfigAttribute>> endpointMap) {
    	this(requestMap);
        Assert.notNull(endpointMap, "endpointMap cannot be null");
        this.endpointMap = endpointMap;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Collection<ConfigAttribute> getAttributes(Object object) throws IllegalArgumentException {
        if (object == null || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be an Endpoint");
        }

        Endpoint endpoint = (Endpoint) object;
        Collection<ConfigAttribute> attributes = null;

        if (this.endpointMap.containsKey(endpoint.getId())) {
            attributes = this.endpointMap.get(endpoint.getId());
        } else {
        	final HttpServletRequest request = FlexContext.getHttpRequest();
        	if (request != null) {
	            for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : requestMap.entrySet()) {
	                if (entry.getKey().matches(request)) {
	                    return entry.getValue();
	                }
	            }
        	}
        }
        return attributes;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public Collection<ConfigAttribute> getAllConfigAttributes() {
        List<ConfigAttribute> allAttributes = new ArrayList<ConfigAttribute>();
        for (Map.Entry<String, Collection<ConfigAttribute>> entry : this.endpointMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        for (Map.Entry<RequestMatcher, Collection<ConfigAttribute>> entry : this.requestMap.entrySet()) {
            allAttributes.addAll(entry.getValue());
        }
        return Collections.unmodifiableCollection(allAttributes);
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean supports(Class<?> clazz) {
        return Endpoint.class.isAssignableFrom(clazz);
    }
}
