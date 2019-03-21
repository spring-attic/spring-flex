/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.config.xml;


/**
 * This interface defines the various integration points for Spring Security, originally provided as a way to provide 
 * support for both versions 2 and 3 of Spring Security concurrently.  The current version only provides support for 
 * Spring Security 3 and above, thus this interface is not strictly necessary, but is being kept in place in case it 
 * is needed again in the future with newer Spring Security versions.
 *
 * @author Jeremy Grelle
 */
public interface SpringSecurityConfigHelper {

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
