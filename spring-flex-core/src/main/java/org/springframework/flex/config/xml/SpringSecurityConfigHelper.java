package org.springframework.flex.config.xml;



interface SpringSecurityConfigHelper {

    String getAuthenticationManagerId();

    String getAccessManagerId();
    
    String getSecurityExceptionTranslatorClassName();
    
    String getPerClientAuthenticationInterceptorClassName();
    
    String getLoginMessageInterceptorClassName();
    
    Object parseConfigAttributes(String access);

    String getEndpointInterceptorClassName();

    String getEndpointDefinitionSourceClassName();

    String getLoginCommandClassName();

	String getSecurityConfigPostProcessorClassName();

}
