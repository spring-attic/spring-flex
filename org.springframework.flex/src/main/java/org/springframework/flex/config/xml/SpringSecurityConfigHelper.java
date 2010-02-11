package org.springframework.flex.config.xml;

import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.MessageInterceptor;


interface SpringSecurityConfigHelper {

    String getAuthenticationManagerId();

    String getAccessManagerId();
    
    ExceptionTranslator getSecurityExceptionTranslator();
    
    MessageInterceptor getPerClientAuthenticationInterceptor();
    
    MessageInterceptor getLoginMessageInterceptor();
    
    Object parseConfigAttributes(String access);
    
    Object parseRequestKey(String requestPath);

    Object getPathMatcher();

    String getSessionFixationPostProcessorClassName();

    String getEndpointInterceptorClassName();

    String getEndpointDefinitionSourceClassName();

    String getLoginCommandClassName();

}
