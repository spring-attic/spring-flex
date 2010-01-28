package org.springframework.flex.config.xml;

import java.util.ArrayList;
import java.util.List;

import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.core.MessageInterceptor;
import org.springframework.flex.security3.LoginMessageInterceptor;
import org.springframework.flex.security3.PerClientAuthenticationInterceptor;
import org.springframework.flex.security3.SecurityExceptionTranslator;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.access.SecurityConfig;
import org.springframework.security.web.access.intercept.RequestKey;
import org.springframework.security.web.util.AntUrlPathMatcher;
import org.springframework.util.StringUtils;


public class SpringSecurity3ConfigHelper implements SpringSecurityConfigHelper {

    private static final String LOGIN_COMMAND_CLASS_NAME = "org.springframework.flex.security3.SpringSecurityLoginCommand";
    
    private static final String ENDPOINT_INTERCEPTOR_CLASS_NAME = "org.springframework.flex.security3.EndpointInterceptor";

    private static final String ENDPOINT_DEFINITION_SOURCE_CLASS_NAME = "org.springframework.flex.security3.EndpointSecurityMetadataSource";
    
    private static final String SESSION_FIXATION_POST_PROCESSOR_CLASS_NAME = "org.springframework.flex.security3.SessionFixationProtectionPostProcessor";
    
    public String getAccessManagerId() {
        return "org.springframework.security.defaultMethodAccessManager";
    }

    public String getAuthenticationManagerId() {
        return "org.springframework.security.authenticationManager";
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
            String[] attrs = StringUtils.commaDelimitedListToStringArray(access);
            List<ConfigAttribute> config = new ArrayList<ConfigAttribute>(); 
            for (int i=0; i<attrs.length; i++) {
                config.add(new SecurityConfig(attrs[i]));
            }
            return config;
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
