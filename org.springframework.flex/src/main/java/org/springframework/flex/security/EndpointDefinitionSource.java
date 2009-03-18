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
	public EndpointDefinitionSource(UrlMatcher urlMatcher,
			LinkedHashMap<RequestKey, ConfigAttributeDefinition> requestMap) {
		super(urlMatcher, requestMap);
	}
	
	/**
	 * Builds the internal request map from the supplied map, and stores the endpoint map for matching by channel id.  
	 * @param endpointMap map of <String, ConfigAttributeDefinition>
	 * @see DefaultFilterInvocationDefinitionSource#DefaultFilterInvocationDefinitionSource(UrlMatcher, LinkedHashMap)
	 */
	public EndpointDefinitionSource(UrlMatcher urlMatcher,
			LinkedHashMap<RequestKey, ConfigAttributeDefinition> requestMap, HashMap<String, ConfigAttributeDefinition> endpointMap) {
		super(urlMatcher, requestMap);
		Assert.notNull(endpointMap, "endpointMap cannot be null");
		this.endpointMap = endpointMap;
	}

	public ConfigAttributeDefinition getAttributes(Object object)
			throws IllegalArgumentException {
		if ((object == null) || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be an Endpoint");
        }
		
		Endpoint endpoint = (Endpoint) object;
		ConfigAttributeDefinition attributes = null;
		
		if (endpointMap.containsKey(endpoint.getId())) {
			attributes = endpointMap.get(endpoint.getId());
		} else {
			attributes = lookupAttributes(endpoint.getUrlForClient(), null);
		}
        return attributes;
	}
	
	@SuppressWarnings("unchecked")
	public Collection getConfigAttributeDefinitions() {
		Collection pathDefinitions = super.getConfigAttributeDefinitions();
		LinkedHashSet allDefinitions = new LinkedHashSet();
		allDefinitions.addAll(endpointMap.values());
		allDefinitions.addAll(pathDefinitions);
        return Collections.unmodifiableCollection(allDefinitions);
    }

	@SuppressWarnings("unchecked")
	public boolean supports(Class clazz) {
		return Endpoint.class.isAssignableFrom(clazz);
	}
}
