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

package org.springframework.flex.config;

/**
 * Default bean ids for beans configured with the XML namespace.
 * 
 * @author Jeremy Grelle
 * @author Rohit Kumar
 */
public abstract class BeanIds {

    public static final String MESSAGE_BROKER = "_messageBroker";

    public static final String FLEX_SESSION_AUTHENTICATION_LISTENER = "_flexSessionAuthenticationListener";

    public static final String SESSION_FIXATION_PROTECTION_CONFIGURER = "_sessionFixationProtectionConfigurer";

    public static final String REMOTING_ANNOTATION_PROCESSOR = "_flexRemotingAnnotationPostProcessor";
    
    public static final String HIBERNATE_SERIALIZATION_PROCESSOR = "_hibernateSerializationConfigPostProcessor";

    public static final String MESSAGE_BROKER_HANDLER_ADAPTER = "_messageBrokerHandlerAdapter";

    public static final String HANDLER_MAPPING_SUFFIX = "DefaultHandlerMapping";

    public static final String LOGIN_COMMAND_SUFFIX = "LoginCommand";
    
    public static final String SECURITY_CONFIG_POST_PROCESSOR = "_loginCommandPostProcessor";

    public static final String ENDPOINT_PROCESSOR_SUFFIX = "EndpointProcessor";

    public static final String REMOTING_PROCESSOR_SUFFIX = "RemotingProcessor";

    public static final String MESSAGING_PROCESSOR_SUFFIX = "MessagingProcessor";

    public static final String DATASERVICES_CONFIG_PROCESSOR_SUFFIX = "DataServicesConfigProcessor";
    
    public static final String LOGIN_INTERCEPTOR_SUFFIX = "LoginInterceptor";

    public static final String JSON_CONFIG_MAP_EDITOR_CONFIGURER = "_jsonConfigMapEditorConfigurer";
}
