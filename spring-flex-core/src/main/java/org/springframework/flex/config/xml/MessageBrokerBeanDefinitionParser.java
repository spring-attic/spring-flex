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

package org.springframework.flex.config.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.flex.config.RuntimeEnvironment;
import org.springframework.flex.security3.AntPathRequestMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} implementation for the <code>message-broker</code> tag that configures a Spring-managed
 * MessageBroker
 * 
 * @author Jeremy Grelle
 * @author Rohit Kumar
 */
public class MessageBrokerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    // --------------------------- Full qualified class names ----------------//
    private static final String MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME = "org.springframework.flex.core.MessageBrokerFactoryBean";

    private static final String MESSAGE_BROKER_HANDLER_ADAPTER_CLASS_NAME = "org.springframework.flex.servlet.MessageBrokerHandlerAdapter";

    private static final String DEFAULT_HANDLER_MAPPING_CLASS_NAME = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping";

    private static final String ENDPOINT_PROCESSOR_CLASS_NAME = "org.springframework.flex.core.EndpointConfigProcessor";

    private static final String EXCEPTION_TRANSLATION_CLASS_NAME = "org.springframework.flex.core.ExceptionTranslationAdvice";

    private static final String MESSAGE_INTERCEPTION_CLASS_NAME = "org.springframework.flex.core.MessageInterceptionAdvice";

    private static final String SERVICE_MESSAGE_ADVISOR_CLASS_NAME = "org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor";

    private static final String REMOTING_PROCESSOR_CLASS_NAME = "org.springframework.flex.remoting.RemotingServiceConfigProcessor";

    private static final String MESSAGING_PROCESSOR_CLASS_NAME = "org.springframework.flex.messaging.MessageServiceConfigProcessor";

    private static final String DATASERVICES_PROCESSOR_CLASS_NAME = "flex.springintegration.core.DataServicesConfigProcessor";

    private static final String REMOTING_ANNOTATION_PROCESSOR_CLASS_NAME = "org.springframework.flex.config.RemotingAnnotationPostProcessor";

    private static final String HIBERNATE_CONFIG_PROCESSOR_CLASS_NAME = "org.springframework.flex.config.HibernateSerializationConfigPostProcessor";

    private static final String CUSTOM_EDITOR_CONFIGURER_CLASS_NAME = "org.springframework.beans.factory.config.CustomEditorConfigurer";

    private static final String JSON_CONFIG_MAP_EDITOR_CLASS_NAME = "org.springframework.flex.config.json.JsonConfigMapPropertyEditor";
    
    private static final String LOGIN_COMMAND_PROCESSOR_CLASS_NAME = "org.springframework.flex.core.LoginCommandConfigProcessor";

    private static final String CONFIG_MAP_CLASS_NAME = "flex.messaging.config.ConfigMap";

    // --------------------------- XML Config Attributes ---------------------//
    private static final String CONFIGURATION_MANAGER_ATTR = "configuration-manager";
    
    private static final String EXCEPTION_LOGGER_ATTR = "exception-logger";

    private static final String SERVICES_CONFIG_PATH_ATTR = "services-config-path";

    private static final String MAPPING_ORDER_ATTR = "mapping-order";

    private static final String DISABLE_DEFAULT_MAPPING_ATTR = "disable-default-mapping";

    private static final String PATTERN_ATTR = "pattern";

    private static final String REF_ATTR = "ref";
    
    private static final String POSITION_ATTR = "position";
    
    private static final String BEFORE_ATTR = "before";
    
    private static final String AFTER_ATTR = "after";

    private static final String AUTH_MANAGER_ATTR = "authentication-manager";

    private static final String ACCESS_MANAGER_ATTR = "access-decision-manager";
    
    private static final String LOGIN_COMMAND_ATTR = "login-command";

    private static final String INVALIDATE_HTTP_SESSION_ATTR = "invalidate-http-session";
    
    private static final String PER_CLIENT_AUTHENTICATION_ATTR = "per-client-authentication";

    private static final String ACCESS_ATTR = "access";

    private static final String CHANNEL_ATTR = "channel";

    // --------------------------- Bean Configuration Properties -------------//
    private static final String URL_MAP_PROPERTY = "urlMap";

    private static final String ORDER_PROPERTY = "order";

    private static final String CONFIG_PROCESSORS_PROPERTY = "configProcessors";
    
    private static final String INVALIDATE_HTTP_SESSION_PROPERTY = "invalidateHttpSession";

    private static final String PER_CLIENT_AUTHENTICATION_PROPERTY = "perClientAuthentication";

    private static final String AUTH_MANAGER_PROPERTY = "authenticationManager";

    private static final String ACCESS_MANAGER_PROPERTY = "accessDecisionManager";

    private static final String OBJECT_DEF_SOURCE_PROPERTY = "objectDefinitionSource";

    private static final String EXCEPTION_TRANSLATORS_PROPERTY = "exceptionTranslators";

    private static final String MESSAGE_INTERCEPTORS_PROPERTY = "messageInterceptors";

    private static final String CUSTOM_EDITORS_PROPERTY = "customEditors";

    // --------------------------- XML Child Elements ------------------------//
    private static final String MAPPING_PATTERN_ELEMENT = "mapping";

    private static final String CONFIG_PROCESSOR_ELEMENT = "config-processor";

    private static final String EXCEPTION_TRANSLATOR_ELEMENT = "exception-translator";

    private static final String MESSAGE_INTERCEPTOR_ELEMENT = "message-interceptor";

    private static final String SECURED_ELEMENT = "secured";

    private static final String SECURED_CHANNEL_ELEMENT = "secured-channel";

    private static final String SECURED_ENDPOINT_PATH_ELEMENT = "secured-endpoint-path";

    private static final String REMOTING_SERVICE_ELEMENT = "remoting-service";

    private static final String MESSAGE_SERVICE_ELEMENT = "message-service";

    // --------------------------- Default Values ----------------------------//
    private static final String DEFAULT_MAPPING_PATH = "/*";

    private final SpringSecurityConfigHelper securityHelper = SpringSecurityConfigResolver.resolve();

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        CompositeComponentDefinition componentDefinition = new CompositeComponentDefinition(element.getLocalName(),
            parserContext.extractSource(element));
        parserContext.pushContainingComponent(componentDefinition);

        Object source = parserContext.extractSource(element);

        // Initialize the config processors set
        ManagedSet<RuntimeBeanReference> configProcessors = new ManagedSet<RuntimeBeanReference>();
        configProcessors.setSource(source);

        // Initialize the AOP advisors list
        ManagedList<RuntimeBeanReference> advisors = new ManagedList<RuntimeBeanReference>();
        advisors.setSource(source);

        // Initialize the exception translators set
        ManagedSet<RuntimeBeanReference> translators = new ManagedSet<RuntimeBeanReference>();
        translators.setSource(source);

        // Initialize the message interceptors map
        Map<Integer, RuntimeBeanReference> interceptors = new TreeMap<Integer, RuntimeBeanReference>();

        // Set the default ID if necessary
        if (!StringUtils.hasText(element.getAttribute(ID_ATTRIBUTE))) {
            element.setAttribute(ID_ATTRIBUTE, BeanIds.MESSAGE_BROKER);
        }

        validateMessageBroker(element, parserContext);

        ParsingUtils.mapOptionalAttributes(element, parserContext, builder, SERVICES_CONFIG_PATH_ATTR);

        ParsingUtils.mapOptionalBeanRefAttributes(element, builder, parserContext, CONFIGURATION_MANAGER_ATTR);

        registerHandlerAdapterIfNecessary(element, parserContext);

        if (!Boolean.parseBoolean(element.getAttribute(DISABLE_DEFAULT_MAPPING_ATTR))) {
            registerHandlerMappings(element, parserContext, DomUtils.getChildElementsByTagName(element, MAPPING_PATTERN_ELEMENT));
        }

        registerCustomConfigProcessors(parserContext, configProcessors, DomUtils.getChildElementsByTagName(element, CONFIG_PROCESSOR_ELEMENT));

        registerConfigMapEditorIfNecessary(element, parserContext);

        configureMessageService(element, parserContext, configProcessors, DomUtils.getChildElementByTagName(element, MESSAGE_SERVICE_ELEMENT));

        configureRemotingService(element, parserContext, configProcessors, DomUtils.getChildElementByTagName(element, REMOTING_SERVICE_ELEMENT));

        registerExceptionTranslation(element, parserContext, advisors, translators, DomUtils.getChildElementsByTagName(element,
            EXCEPTION_TRANSLATOR_ELEMENT));

        configureCustomInterceptors(element, parserContext, advisors, interceptors, DomUtils.getChildElementsByTagName(element,
            MESSAGE_INTERCEPTOR_ELEMENT));
        
        configureSecurity(element, parserContext, configProcessors, advisors, translators, interceptors, DomUtils.getChildElementByTagName(element,
            SECURED_ELEMENT));

        // Register Data Services specific Configuration processor
        registerDataServicesConfigProcessorIfRequired(parserContext, configProcessors, translators, interceptors, element, element.getAttribute(ID_ATTRIBUTE));

        ManagedSet<RuntimeBeanReference> managedInterceptors = new ManagedSet<RuntimeBeanReference>();
        managedInterceptors.setSource(source);
        managedInterceptors.addAll(interceptors.values());
        registerMessageInterceptionAdvice(element, parserContext, advisors, managedInterceptors);

        registerEndpointProcessor(parserContext, configProcessors, advisors, element, element.getAttribute(ID_ATTRIBUTE));

        if (!configProcessors.isEmpty()) {
            builder.addPropertyValue(CONFIG_PROCESSORS_PROPERTY, configProcessors);
        }

        parserContext.popAndRegisterContainingComponent();
    }

    @Override
    protected String getBeanClassName(Element element) {
        return MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME;
    }

    private void configureMessageService(Element parent, ParserContext parserContext, ManagedSet<RuntimeBeanReference> configProcessors,
        Element messageServiceElement) {
        Element source = messageServiceElement != null ? messageServiceElement : parent;

        BeanDefinitionBuilder messagingProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(MESSAGING_PROCESSOR_CLASS_NAME);

        if (messageServiceElement != null) {
            ParsingUtils.mapAllAttributes(messageServiceElement, parserContext, messagingProcessorBuilder);
        }

        String brokerId = parent.getAttribute(ID_ATTRIBUTE);

        ParsingUtils.registerInfrastructureComponent(source, parserContext, messagingProcessorBuilder, brokerId + BeanIds.MESSAGING_PROCESSOR_SUFFIX);
        configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.MESSAGING_PROCESSOR_SUFFIX));
    }

    private void configureRemotingService(Element parent, ParserContext parserContext, ManagedSet<RuntimeBeanReference> configProcessors, Element remotingServiceElement) {
        Element source = remotingServiceElement != null ? remotingServiceElement : parent;

        BeanDefinitionBuilder remotingProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(REMOTING_PROCESSOR_CLASS_NAME);

        if (remotingServiceElement != null) {
            ParsingUtils.mapAllAttributes(remotingServiceElement, parserContext, remotingProcessorBuilder);
        }

        String brokerId = parent.getAttribute(ID_ATTRIBUTE);

        ParsingUtils.registerInfrastructureComponent(source, parserContext, remotingProcessorBuilder, brokerId + BeanIds.REMOTING_PROCESSOR_SUFFIX);
        configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.REMOTING_PROCESSOR_SUFFIX));

        registerFlexRemotingAnnotationPostProcessorIfNecessary(source, parserContext);

        registerHibernateSerializationConfigPostProcessorIfNecessary(source, parserContext);
    }

    private void configureSecurity(Element parent, ParserContext parserContext, ManagedSet<RuntimeBeanReference> configProcessors, ManagedList<RuntimeBeanReference> advisors,
        ManagedSet<RuntimeBeanReference> translators, Map<Integer, RuntimeBeanReference> interceptors, Element securedElement) {

        if (securedElement == null) {
            return;
        }

        boolean invalidateHttpSession = Boolean.parseBoolean(securedElement.getAttribute(INVALIDATE_HTTP_SESSION_ATTR));
        boolean perClientAuthentication = Boolean.parseBoolean(securedElement.getAttribute(PER_CLIENT_AUTHENTICATION_ATTR));

        String authManager = securedElement.getAttribute(AUTH_MANAGER_ATTR);
        if (!StringUtils.hasText(authManager)) {
            authManager = securityHelper.getAuthenticationManagerId();
        }

        String accessManager = securedElement.getAttribute(ACCESS_MANAGER_ATTR);
        if (!StringUtils.hasText(accessManager)) {
            accessManager = securityHelper.getAccessManagerId();
        }

        registerSecurityConfigPostProcessorIfNecessary(parserContext, securedElement);

        String brokerId = parent.getAttribute(ID_ATTRIBUTE);
        registerLoginCommand(brokerId, parserContext, configProcessors, securedElement, authManager, invalidateHttpSession, perClientAuthentication);

        BeanDefinitionBuilder exceptionTranslatorBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getSecurityExceptionTranslatorClassName());
        String exceptionTranslatorBeanId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, exceptionTranslatorBuilder);
        translators.add(new RuntimeBeanReference(exceptionTranslatorBeanId));
        
        // Register PerClientAuthenticationInterceptor as necessary
        BeanDefinitionBuilder perClientInterceptorBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getPerClientAuthenticationInterceptorClassName());
        String perClientInterceptorBeanId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, perClientInterceptorBuilder);
        if (perClientAuthentication && !interceptors.containsKey(MessageInterceptors.PER_CLIENT_AUTH_INTERCEPTOR.getOrder())) {
            interceptors.put(MessageInterceptors.PER_CLIENT_AUTH_INTERCEPTOR.getOrder(), new RuntimeBeanReference(perClientInterceptorBeanId));
        }
        
        // Register LoginMessageInterceptor
        if (!interceptors.containsKey(MessageInterceptors.LOGIN_MESSAGE_INTERCEPTOR.getOrder())) {
	        BeanDefinitionBuilder loginInterceptorBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getLoginMessageInterceptorClassName());
	        String loginInterceptorBeanId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, loginInterceptorBuilder);
	        interceptors.put(MessageInterceptors.LOGIN_MESSAGE_INTERCEPTOR.getOrder(), new RuntimeBeanReference(loginInterceptorBeanId));
        }

        registerEndpointInterceptorIfNecessary(securedElement, parserContext, interceptors, authManager, accessManager);
    }

    private void registerConfigMapEditorIfNecessary(Element source, ParserContext parserContext) {
        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.JSON_CONFIG_MAP_EDITOR_CONFIGURER)) {
            BeanDefinitionBuilder configurerBuilder = BeanDefinitionBuilder.genericBeanDefinition(CUSTOM_EDITOR_CONFIGURER_CLASS_NAME);
            ManagedMap<String, String> editors = new ManagedMap<String, String>();
            editors.put(CONFIG_MAP_CLASS_NAME, JSON_CONFIG_MAP_EDITOR_CLASS_NAME);
            configurerBuilder.addPropertyValue(CUSTOM_EDITORS_PROPERTY, editors);
            ParsingUtils.registerInfrastructureComponent(source, parserContext, configurerBuilder, BeanIds.JSON_CONFIG_MAP_EDITOR_CONFIGURER);
        }

    }

    private void registerCustomConfigProcessors(ParserContext parserContext, Set<RuntimeBeanReference> configProcessors, List<Element> configProcessorElements) {
        if (!CollectionUtils.isEmpty(configProcessorElements)) {
            Iterator<Element> i = configProcessorElements.iterator();
            while (i.hasNext()) {
                Element configProcessorElement = i.next();
                configProcessors.add(new RuntimeBeanReference(configProcessorElement.getAttribute(REF_ATTR)));
            }
        }
    }

    private void registerEndpointInterceptorIfNecessary(Element securedElement, ParserContext parserContext, Map<Integer, RuntimeBeanReference> interceptors,
        String authManager, String accessManager) {
        if (securedElement.hasChildNodes() && !interceptors.containsKey(MessageInterceptors.ENDPOINT_INTERCEPTOR.getOrder())) {
            BeanDefinitionBuilder interceptorBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getEndpointInterceptorClassName());
            interceptorBuilder.addPropertyReference(AUTH_MANAGER_PROPERTY, authManager);
            if (StringUtils.hasText(accessManager)) {
                interceptorBuilder.addPropertyReference(ACCESS_MANAGER_PROPERTY, accessManager);
            }

            BeanDefinitionBuilder endpointDefSourceBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getEndpointDefinitionSourceClassName());

            HashMap<String, Object> endpointMap = new HashMap<String, Object>();
            List<Element> securedChannelElements = DomUtils.getChildElementsByTagName(securedElement, SECURED_CHANNEL_ELEMENT);
            if (!CollectionUtils.isEmpty(securedChannelElements)) {
                Iterator<Element> i = securedChannelElements.iterator();
                while (i.hasNext()) {
                    Element securedChannel = i.next();
                    String access = securedChannel.getAttribute(ACCESS_ATTR);
                    String channel = securedChannel.getAttribute(CHANNEL_ATTR);
                    Object attributeDefinition = securityHelper.parseConfigAttributes(access);
                    endpointMap.put(channel, attributeDefinition);
                }
            }

            LinkedHashMap<Object, Object> requestMap = new LinkedHashMap<Object, Object>();
            List<Element> securedEndpointPathElements = DomUtils.getChildElementsByTagName(securedElement, SECURED_ENDPOINT_PATH_ELEMENT);
            if (!CollectionUtils.isEmpty(securedEndpointPathElements)) {
                Iterator<Element> i = securedEndpointPathElements.iterator();
                while (i.hasNext()) {
                    Element securedPath = i.next();
                    requestMap.put(new AntPathRequestMatcher(securedPath.getAttribute(PATTERN_ATTR)),
                        securityHelper.parseConfigAttributes(securedPath.getAttribute(ACCESS_ATTR)));
                }
            }
            
            endpointDefSourceBuilder.addConstructorArgValue(requestMap);
            endpointDefSourceBuilder.addConstructorArgValue(endpointMap);

            String endpointDefSourceId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, endpointDefSourceBuilder);
            interceptorBuilder.addPropertyReference(OBJECT_DEF_SOURCE_PROPERTY, endpointDefSourceId);
            String interceptorId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, interceptorBuilder);
            interceptors.put(MessageInterceptors.ENDPOINT_INTERCEPTOR.getOrder(), new RuntimeBeanReference(interceptorId));
        }
    }

    private void registerDataServicesConfigProcessorIfRequired(ParserContext parserContext, ManagedSet<RuntimeBeanReference> configProcessors, 
        ManagedSet<RuntimeBeanReference> translators, Map<Integer, RuntimeBeanReference> interceptors, Element securedElement, String brokerId) {        
        if (RuntimeEnvironment.isLCDS() && (!interceptors.isEmpty() || !translators.isEmpty())) {
            BeanDefinitionBuilder lcdsConfigProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(DATASERVICES_PROCESSOR_CLASS_NAME);
            lcdsConfigProcessorBuilder.addPropertyValue(EXCEPTION_TRANSLATORS_PROPERTY, translators);
            
            //NIO Endpoints require the PerClientAuthenticationInterceptor
            if (!interceptors.containsKey(MessageInterceptors.PER_CLIENT_AUTH_INTERCEPTOR.getOrder())) {
            	BeanDefinitionBuilder perClientInterceptorBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getPerClientAuthenticationInterceptorClassName());
                String perClientInterceptorBeanId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, perClientInterceptorBuilder);
                interceptors.put(MessageInterceptors.PER_CLIENT_AUTH_INTERCEPTOR.getOrder(), new RuntimeBeanReference(perClientInterceptorBeanId));
            }
            
            ManagedSet<RuntimeBeanReference> managedInterceptors = new ManagedSet<RuntimeBeanReference>();
            managedInterceptors.addAll(interceptors.values());
            managedInterceptors.setSource(parserContext.extractSource(securedElement));
            
            lcdsConfigProcessorBuilder.addPropertyValue(MESSAGE_INTERCEPTORS_PROPERTY, managedInterceptors);
            ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, lcdsConfigProcessorBuilder, brokerId
                    + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX);
            configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.DATASERVICES_CONFIG_PROCESSOR_SUFFIX));
        }
    }

    private void registerEndpointProcessor(ParserContext parserContext, ManagedSet<RuntimeBeanReference> configProcessors, ManagedList<RuntimeBeanReference> advisors, Element securedElement,
        String brokerId) {
        BeanDefinitionBuilder endpointProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(ENDPOINT_PROCESSOR_CLASS_NAME);
        endpointProcessorBuilder.addConstructorArgValue(advisors);
        ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, endpointProcessorBuilder, brokerId
            + BeanIds.ENDPOINT_PROCESSOR_SUFFIX);
        configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.ENDPOINT_PROCESSOR_SUFFIX));
    }

    private void registerExceptionTranslation(Element element, ParserContext parserContext, ManagedList<RuntimeBeanReference> advisors, ManagedSet<RuntimeBeanReference> translators,
        List<Element> exceptionTranslatorElements) {

        if (!CollectionUtils.isEmpty(exceptionTranslatorElements)) {
            Iterator<Element> i = exceptionTranslatorElements.iterator();
            while (i.hasNext()) {
                Element exceptionTranslatorElement = i.next();
                translators.add(new RuntimeBeanReference(exceptionTranslatorElement.getAttribute(REF_ATTR)));
            }
        }

        BeanDefinitionBuilder advisorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SERVICE_MESSAGE_ADVISOR_CLASS_NAME);
        BeanDefinitionBuilder exceptionTranslationBuilder = BeanDefinitionBuilder.genericBeanDefinition(EXCEPTION_TRANSLATION_CLASS_NAME);
        exceptionTranslationBuilder.addPropertyValue(EXCEPTION_TRANSLATORS_PROPERTY, translators);
        ParsingUtils.mapOptionalBeanRefAttributes(element, exceptionTranslationBuilder, parserContext, EXCEPTION_LOGGER_ATTR);
        String exceptionTranslationId = ParsingUtils.registerInfrastructureComponent(element, parserContext, exceptionTranslationBuilder);
        advisorBuilder.addConstructorArgReference(exceptionTranslationId);
        String advisorId = ParsingUtils.registerInfrastructureComponent(element, parserContext, advisorBuilder);
        advisors.add(new RuntimeBeanReference(advisorId));
    }

    private void registerFlexRemotingAnnotationPostProcessorIfNecessary(Element source, ParserContext parserContext) {
        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.REMOTING_ANNOTATION_PROCESSOR)) {
            BeanDefinitionBuilder processorBuilder = BeanDefinitionBuilder.genericBeanDefinition(REMOTING_ANNOTATION_PROCESSOR_CLASS_NAME);
            ParsingUtils.registerInfrastructureComponent(source, parserContext, processorBuilder, BeanIds.REMOTING_ANNOTATION_PROCESSOR);
        }
    }

    private void registerHibernateSerializationConfigPostProcessorIfNecessary(Element source, ParserContext parserContext) {
        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.HIBERNATE_SERIALIZATION_PROCESSOR)) {
            BeanDefinitionBuilder processorBuilder = BeanDefinitionBuilder.genericBeanDefinition(HIBERNATE_CONFIG_PROCESSOR_CLASS_NAME);
            ParsingUtils.registerInfrastructureComponent(source, parserContext, processorBuilder, BeanIds.HIBERNATE_SERIALIZATION_PROCESSOR);
        }
    }

    private void registerHandlerAdapterIfNecessary(Element element, ParserContext parserContext) {
        // Make sure we only ever register one MessageBrokerHandlerAdapter
        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER)) {
            BeanDefinitionBuilder handlerAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(MESSAGE_BROKER_HANDLER_ADAPTER_CLASS_NAME);

            ParsingUtils.registerInfrastructureComponent(element, parserContext, handlerAdapterBuilder, BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER);
        }
    }

    private void registerHandlerMappings(Element parent, ParserContext parserContext, List<Element> mappingPatternElements) {
        BeanDefinitionBuilder handlerMappingBuilder = BeanDefinitionBuilder.genericBeanDefinition(DEFAULT_HANDLER_MAPPING_CLASS_NAME);

        if (StringUtils.hasText(parent.getAttribute(MAPPING_ORDER_ATTR))) {
            handlerMappingBuilder.addPropertyValue(ORDER_PROPERTY, Integer.parseInt(parent.getAttribute(MAPPING_ORDER_ATTR)));
        }

        Map<String, String> mappings = new HashMap<String, String>();
        if (CollectionUtils.isEmpty(mappingPatternElements)) {
            mappings.put(DEFAULT_MAPPING_PATH, parent.getAttribute(ID_ATTRIBUTE));
        } else {
            Iterator<Element> i = mappingPatternElements.iterator();
            while (i.hasNext()) {
                Element mappingElement = i.next();
                mappings.put(mappingElement.getAttribute(PATTERN_ATTR), parent.getAttribute(ID_ATTRIBUTE));
            }
        }

        handlerMappingBuilder.addPropertyValue(URL_MAP_PROPERTY, mappings);
        ParsingUtils.registerInfrastructureComponent(parent, parserContext, handlerMappingBuilder, parent.getAttribute(ID_ATTRIBUTE)
            + BeanIds.HANDLER_MAPPING_SUFFIX);
    }

    private void registerLoginCommand(String brokerId, ParserContext parserContext, ManagedSet<RuntimeBeanReference> configProcessors, Element securedElement,
        String authManager, boolean invalidateHttpSession, boolean perClientAuthentication) {

    	String loginCommandId = securedElement.getAttribute(LOGIN_COMMAND_ATTR);
    	if (!StringUtils.hasText(loginCommandId)) {
    		loginCommandId = brokerId + BeanIds.LOGIN_COMMAND_SUFFIX;

            BeanDefinitionBuilder loginCommandBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getLoginCommandClassName());
            loginCommandBuilder.addConstructorArgReference(authManager);
            loginCommandBuilder.addPropertyValue(PER_CLIENT_AUTHENTICATION_PROPERTY, perClientAuthentication);
            loginCommandBuilder.getRawBeanDefinition().setAttribute(INVALIDATE_HTTP_SESSION_PROPERTY, invalidateHttpSession);

            ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, loginCommandBuilder, loginCommandId);
    	}
    	
        BeanDefinitionBuilder loginCommandProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(LOGIN_COMMAND_PROCESSOR_CLASS_NAME);
        loginCommandProcessorBuilder.addConstructorArgReference(loginCommandId);
        loginCommandProcessorBuilder.addPropertyValue(PER_CLIENT_AUTHENTICATION_PROPERTY, perClientAuthentication);
        
        String loginCommandProcessorId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, loginCommandProcessorBuilder);
        
        configProcessors.add(new RuntimeBeanReference(loginCommandProcessorId));
    }
    
    private void registerSecurityConfigPostProcessorIfNecessary(ParserContext parserContext, Element securedElement) {
    	if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.SECURITY_CONFIG_POST_PROCESSOR)) {
	    	BeanDefinitionBuilder securityConfigPostProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(securityHelper.getSecurityConfigPostProcessorClassName());
	    	securityConfigPostProcessorBuilder.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
	    	securityConfigPostProcessorBuilder.setDependencyCheck(AbstractBeanDefinition.DEPENDENCY_CHECK_NONE);
	    	ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, securityConfigPostProcessorBuilder, BeanIds.SECURITY_CONFIG_POST_PROCESSOR);
    	}
    }
    
    private void configureCustomInterceptors(Element element, ParserContext parserContext, ManagedList<RuntimeBeanReference> advisors, Map<Integer, RuntimeBeanReference> interceptors,
            List<Element> messageInterceptorElements) {
    	if (!CollectionUtils.isEmpty(messageInterceptorElements)) {
            Iterator<Element> i = messageInterceptorElements.iterator();
            while (i.hasNext()) {
                Element messageInterceptorElement = i.next();
                int key = MessageInterceptors.FIRST.getOrder();
                if (messageInterceptorElement.hasAttribute(POSITION_ATTR)) {
                	key = MessageInterceptors.valueOf(messageInterceptorElement.getAttribute(POSITION_ATTR)).getOrder();
                } else if (messageInterceptorElement.hasAttribute(AFTER_ATTR)) {
                	key = MessageInterceptors.valueOf(messageInterceptorElement.getAttribute(AFTER_ATTR)).getOrder() + 1;
                } else if (messageInterceptorElement.hasAttribute(BEFORE_ATTR)) {
                	key = MessageInterceptors.valueOf(messageInterceptorElement.getAttribute(BEFORE_ATTR)).getOrder() - 50;
                }
                while (interceptors.get(key) != null) {
            		key++;
            	}
                interceptors.put(key, new RuntimeBeanReference(messageInterceptorElement.getAttribute(REF_ATTR)));
            }
        }
    }

    private void registerMessageInterceptionAdvice(Element element, ParserContext parserContext, ManagedList<RuntimeBeanReference> advisors, ManagedSet<RuntimeBeanReference> interceptors) {
        BeanDefinitionBuilder advisorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SERVICE_MESSAGE_ADVISOR_CLASS_NAME);
        BeanDefinitionBuilder messageInterceptionBuilder = BeanDefinitionBuilder.genericBeanDefinition(MESSAGE_INTERCEPTION_CLASS_NAME);
        messageInterceptionBuilder.addPropertyValue(MESSAGE_INTERCEPTORS_PROPERTY, interceptors);
        String messageInterceptionId = ParsingUtils.registerInfrastructureComponent(element, parserContext, messageInterceptionBuilder);
        advisorBuilder.addConstructorArgReference(messageInterceptionId);
        String advisorId = ParsingUtils.registerInfrastructureComponent(element, parserContext, advisorBuilder);
        advisors.add(new RuntimeBeanReference(advisorId));
    }

    private void validateMessageBroker(Element element, ParserContext parserContext) {

        if (!FlexConfigurationManager.DEFAULT_CONFIG_PATH.equals(element.getAttribute(SERVICES_CONFIG_PATH_ATTR))
            && StringUtils.hasText(element.getAttribute(CONFIGURATION_MANAGER_ATTR))) {
            parserContext.getReaderContext().error(
                "The " + SERVICES_CONFIG_PATH_ATTR + " cannot be set when using a custom " + CONFIGURATION_MANAGER_ATTR
                    + " reference.  Set the configurationPath on the custom ConfigurationManager instead.", element);

        }
    }
}
