package org.springframework.flex.config.xml;

import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.security.LoginMessageInterceptor;
import org.springframework.flex.security.PerClientAuthenticationInterceptor;
import org.springframework.flex.security.SecurityExceptionTranslator;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.util.AntUrlPathMatcher;
import org.springframework.util.StringUtils;


public class SpringSecurity2ConfigHelper implements SpringSecurityConfigHelper {

    private static final String LOGIN_COMMAND_CLASS_NAME = "org.springframework.flex.security.SpringSecurityLoginCommand";
    
    private static final String ENDPOINT_INTERCEPTOR_CLASS_NAME = "org.springframework.flex.security.EndpointInterceptor";

    private static final String ENDPOINT_DEFINITION_SOURCE_CLASS_NAME = "org.springframework.flex.security.EndpointDefinitionSource";
    
    private static final String SESSION_FIXATION_POST_PROCESSOR_CLASS_NAME = "org.springframework.flex.security.SessionFixationProtectionPostProcessor";
    
    public String getAccessManagerId() {
        return "_accessManager";
    }

    public String getAuthenticationManagerId() {
        return "_authenticationManager";
    }

    public MessageInterceptor getLoginMessageInterceptor() {
        return new LoginMessageInterceptor();
    }

    public MessageInterceptor getPerClientAuthenticationInterceptor() {
        return new PerClientAuthenticationInterceptor();
    }

    public ExceptionTranslator getSecurityExceptionTranslator() {
        return new SecurityExceptionTranslator();
    }

    public Object parseConfigAttributes(String access) {
        if (StringUtils.hasText(access)) {
            return new ConfigAttributeDefinition(StringUtils.commaDelimitedListToStringArray(access));
        } else {
            return null;
        }
    }

    public Object parseRequestKey(String requestPath) {
        return new RequestKey(requestPath);
    }

    public Object getPathMatcher() {
        return new AntUrlPathMatcher();
    }

    public String getEndpointDefinitionSourceClassName() {
        return ENDPOINT_DEFINITION_SOURCE_CLASS_NAME;
    }

    public String getEndpointInterceptorClassName() {
        return ENDPOINT_INTERCEPTOR_CLASS_NAME;
    }

    public String getLoginCommandClassName() {
        return LOGIN_COMMAND_CLASS_NAME;
    }

    public String getSessionFixationPostProcessorClassName() {
        return SESSION_FIXATION_POST_PROCESSOR_CLASS_NAME;
    }

}
