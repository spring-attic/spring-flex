package org.springframework.flex.config.xml;



interface SpringSecurityConfigHelper {

    String getAuthenticationManagerId();

    String getAccessManagerId();
    
    String getSecurityExceptionTranslatorClassName();
    
    String getPerClientAuthenticationInterceptorClassName();
    
    String getLoginMessageInterceptorClassName();
    
    Object parseConfigAttributes(String access);
    
    Object parseRequestKey(String requestPath);

    Object getPathMatcher();

    String getSessionFixationPostProcessorClassName();

    String getEndpointInterceptorClassName();

    String getEndpointDefinitionSourceClassName();

    String getLoginCommandClassName();

}
