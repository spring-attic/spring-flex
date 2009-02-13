package org.springframework.flex.messaging.security;

import flex.messaging.endpoints.Endpoint;

import java.util.LinkedHashMap;

import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.ObjectDefinitionSource;
import org.springframework.security.intercept.web.DefaultFilterInvocationDefinitionSource;
import org.springframework.security.util.UrlMatcher;

/**
 * 
 * Implementation of {@link ObjectDefinitionSource} for BlazeDS {@link Endpoint}s.  
 * 
 * @author Jeremy Grelle
 */
public class EndpointInvocationDefinitionSource extends DefaultFilterInvocationDefinitionSource {

	public EndpointInvocationDefinitionSource(UrlMatcher urlMatcher,
			LinkedHashMap requestMap) {
		super(urlMatcher, requestMap);
	}

	public ConfigAttributeDefinition getAttributes(Object object)
			throws IllegalArgumentException {
		if ((object == null) || !this.supports(object.getClass())) {
            throw new IllegalArgumentException("Object must be an Endpoint");
        }

        String url = ((Endpoint) object).getUrlForClient();

        return lookupAttributes(url, null);
	}

	public boolean supports(Class clazz) {
		return Endpoint.class.isAssignableFrom(clazz);
	}

}
