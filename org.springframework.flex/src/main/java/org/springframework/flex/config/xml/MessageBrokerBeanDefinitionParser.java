/*
 * Copyright 2002-2009 the original author or authors.
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

import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.parsing.CompositeComponentDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.ManagedSet;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.flex.config.BeanIds;
import org.springframework.flex.config.FlexConfigurationManager;
import org.springframework.flex.security.LoginMessageInterceptor;
import org.springframework.flex.security.PerClientAuthenticationInterceptor;
import org.springframework.flex.security.SecurityExceptionTranslator;
import org.springframework.security.ConfigAttributeDefinition;
import org.springframework.security.intercept.web.RequestKey;
import org.springframework.security.util.AntUrlPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * {@link BeanDefinitionParser} implementation for the <code>message-broker</code> tag that configures a Spring-managed
 * MessageBroker
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerBeanDefinitionParser extends AbstractSingleBeanDefinitionParser {

    // --------------------------- Full qualified class names ----------------//
    private static final String MESSAGE_BROKER_FACTORY_BEAN_CLASS_NAME = "org.springframework.flex.core.MessageBrokerFactoryBean";

    private static final String MESSAGE_BROKER_HANDLER_ADAPTER_CLASS_NAME = "org.springframework.flex.servlet.MessageBrokerHandlerAdapter";

    private static final String DEFAULT_HANDLER_MAPPING_CLASS_NAME = "org.springframework.web.servlet.handler.SimpleUrlHandlerMapping";

    private static final String LOGIN_COMMAND_CLASS_NAME = "org.springframework.flex.security.SpringSecurityLoginCommand";

    private static final String ENDPOINT_PROCESSOR_CLASS_NAME = "org.springframework.flex.core.EndpointConfigProcessor";

    private static final String EXCEPTION_TRANSLATION_CLASS_NAME = "org.springframework.flex.core.ExceptionTranslationAdvice";

    private static final String MESSAGE_INTERCEPTION_CLASS_NAME = "org.springframework.flex.core.MessageInterceptionAdvice";

    private static final String ENDPOINT_INTERCEPTOR_CLASS_NAME = "org.springframework.flex.security.EndpointInterceptor";

    private static final String SERVICE_MESSAGE_ADVISOR_CLASS_NAME = "org.springframework.flex.core.EndpointServiceMessagePointcutAdvisor";

    private static final String ENDPOINT_DEFINITION_SOURCE_CLASS_NAME = "org.springframework.flex.security.EndpointDefinitionSource";

    private static final String REMOTING_PROCESSOR_CLASS_NAME = "org.springframework.flex.remoting.RemotingServiceConfigProcessor";

    private static final String MESSAGING_PROCESSOR_CLASS_NAME = "org.springframework.flex.messaging.MessageServiceConfigProcessor";

    private static final String SESSION_FIXATION_CONFIGURER_CLASS_NAME = "org.springframework.flex.config.SessionFixationProtectionConfigurer";

    private static final String REMOTING_ANNOTATION_PROCESSOR_CLASS_NAME = "org.springframework.flex.config.RemotingAnnotationPostProcessor";

    private static final String CUSTOM_EDITOR_CONFIGURER_CLASS_NAME = "org.springframework.beans.factory.config.CustomEditorConfigurer";

    private static final String JSON_CONFIG_MAP_EDITOR_CLASS_NAME = "org.springframework.flex.config.json.JsonConfigMapPropertyEditor";

    private static final String CONFIG_MAP_CLASS_NAME = "flex.messaging.config.ConfigMap";

    // --------------------------- XML Config Attributes ---------------------//
    private static final String CONFIGURATION_MANAGER_ATTR = "configuration-manager";

    private static final String SERVICES_CONFIG_PATH_ATTR = "services-config-path";

    private static final String MAPPING_ORDER_ATTR = "mapping-order";

    private static final String DISABLE_DEFAULT_MAPPING_ATTR = "disable-default-mapping";

    private static final String PATTERN_ATTR = "pattern";

    private static final String REF_ATTR = "ref";

    private static final String AUTH_MANAGER_ATTR = "authentication-manager";

    private static final String ACCESS_MANAGER_ATTR = "access-decision-manager";

    private static final String PER_CLIENT_AUTHENTICATION_ATTR = "per-client-authentication";

    private static final String ACCESS_ATTR = "access";

    private static final String CHANNEL_ATTR = "channel";

    // --------------------------- Bean Configuration Properties -------------//
    private static final String URL_MAP_PROPERTY = "urlMap";

    private static final String ORDER_PROPERTY = "order";

    private static final String CONFIG_PROCESSORS_PROPERTY = "configProcessors";

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

    @Override
    protected void doParse(Element element, ParserContext parserContext, BeanDefinitionBuilder builder) {

        CompositeComponentDefinition componentDefinition = new CompositeComponentDefinition(element.getLocalName(),
            parserContext.extractSource(element));
        parserContext.pushContainingComponent(componentDefinition);

        Object source = parserContext.extractSource(element);

        // Initialize the config processors set
        ManagedSet configProcessors = new ManagedSet();
        configProcessors.setSource(source);

        // Initialize the AOP advisors list
        ManagedList advisors = new ManagedList();
        advisors.setSource(source);

        // Initialize the exception translators set
        ManagedSet translators = new ManagedSet();
        translators.setSource(source);

        // Initialize the message interceptors set
        ManagedSet interceptors = new ManagedSet();
        interceptors.setSource(source);

        // Set the default ID if necessary
        if (!StringUtils.hasText(element.getAttribute(ID_ATTRIBUTE))) {
            element.setAttribute(ID_ATTRIBUTE, BeanIds.MESSAGE_BROKER);
        }

        validateMessageBroker(element, parserContext);

        ParsingUtils.mapOptionalAttributes(element, builder, SERVICES_CONFIG_PATH_ATTR);

        ParsingUtils.mapOptionalBeanRefAttributes(element, builder, CONFIGURATION_MANAGER_ATTR);

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

        registerMessageInterception(element, parserContext, advisors, interceptors, DomUtils.getChildElementsByTagName(element,
            MESSAGE_INTERCEPTOR_ELEMENT));

        configureSecurity(element, parserContext, configProcessors, advisors, translators, interceptors, DomUtils.getChildElementByTagName(element,
            SECURED_ELEMENT));

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

    @SuppressWarnings("unchecked")
    private void configureMessageService(Element parent, ParserContext parserContext, ManagedSet configProcessors, Element messageServiceElement) {
        Element source = messageServiceElement != null ? messageServiceElement : parent;

        BeanDefinitionBuilder messagingProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(MESSAGING_PROCESSOR_CLASS_NAME);

        if (messageServiceElement != null) {
            ParsingUtils.mapAllAttributes(messageServiceElement, messagingProcessorBuilder);
        }

        String brokerId = parent.getAttribute(ID_ATTRIBUTE);

        ParsingUtils.registerInfrastructureComponent(source, parserContext, messagingProcessorBuilder, brokerId + BeanIds.MESSAGING_PROCESSOR_SUFFIX);
        configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.MESSAGING_PROCESSOR_SUFFIX));

        registerFlexRemotingAnnotationPostProcessorIfNecessary(source, parserContext);
    }

    @SuppressWarnings("unchecked")
    private void configureRemotingService(Element parent, ParserContext parserContext, ManagedSet configProcessors, Element remotingServiceElement) {
        Element source = remotingServiceElement != null ? remotingServiceElement : parent;

        BeanDefinitionBuilder remotingProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(REMOTING_PROCESSOR_CLASS_NAME);

        if (remotingServiceElement != null) {
            ParsingUtils.mapAllAttributes(remotingServiceElement, remotingProcessorBuilder);
        }

        String brokerId = parent.getAttribute(ID_ATTRIBUTE);

        ParsingUtils.registerInfrastructureComponent(source, parserContext, remotingProcessorBuilder, brokerId + BeanIds.REMOTING_PROCESSOR_SUFFIX);
        configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.REMOTING_PROCESSOR_SUFFIX));

        registerFlexRemotingAnnotationPostProcessorIfNecessary(source, parserContext);
    }

    @SuppressWarnings("unchecked")
    private void configureSecurity(Element parent, ParserContext parserContext, ManagedSet configProcessors, ManagedList advisors,
        ManagedSet translators, ManagedSet interceptors, Element securedElement) {

        if (securedElement == null) {
            return;
        }

        boolean perClientAuthentication = Boolean.parseBoolean(securedElement.getAttribute(PER_CLIENT_AUTHENTICATION_ATTR));

        String authManager = securedElement.getAttribute(AUTH_MANAGER_ATTR);
        if (!StringUtils.hasText(authManager)) {
            authManager = org.springframework.security.config.BeanIds.AUTHENTICATION_MANAGER;
        }

        String accessManager = securedElement.getAttribute(ACCESS_MANAGER_ATTR);
        if (!StringUtils.hasText(accessManager)) {
            accessManager = org.springframework.security.config.BeanIds.ACCESS_MANAGER;
        }

        registerAuthenticationListenerIfNecessary(securedElement, parserContext);

        String brokerId = parent.getAttribute(ID_ATTRIBUTE);
        registerLoginCommand(brokerId, parserContext, configProcessors, securedElement, authManager, perClientAuthentication);

        translators.add(new SecurityExceptionTranslator());
        if (perClientAuthentication) {
            interceptors.add(new PerClientAuthenticationInterceptor());
        }
        interceptors.add(new LoginMessageInterceptor());

        registerEndpointInterceptorIfNecessary(securedElement, parserContext, interceptors, authManager, accessManager);
    }

    private Object parseConfigAttributeDefinition(String access) {
        if (StringUtils.hasText(access)) {
            return new ConfigAttributeDefinition(StringUtils.commaDelimitedListToStringArray(access));
        } else {
            return null;
        }
    }

    private void registerAuthenticationListenerIfNecessary(Element securedElement, ParserContext parserContext) {

        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.SESSION_FIXATION_PROTECTION_CONFIGURER)) {
            BeanDefinitionBuilder configurerBuilder = BeanDefinitionBuilder.genericBeanDefinition(SESSION_FIXATION_CONFIGURER_CLASS_NAME);
            ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, configurerBuilder,
                BeanIds.SESSION_FIXATION_PROTECTION_CONFIGURER);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerConfigMapEditorIfNecessary(Element source, ParserContext parserContext) {
        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.JSON_CONFIG_MAP_EDITOR_CONFIGURER)) {
            BeanDefinitionBuilder configurerBuilder = BeanDefinitionBuilder.genericBeanDefinition(CUSTOM_EDITOR_CONFIGURER_CLASS_NAME);
            ManagedMap editors = new ManagedMap();
            editors.put(CONFIG_MAP_CLASS_NAME, JSON_CONFIG_MAP_EDITOR_CLASS_NAME);
            configurerBuilder.addPropertyValue(CUSTOM_EDITORS_PROPERTY, editors);
            ParsingUtils.registerInfrastructureComponent(source, parserContext, configurerBuilder, BeanIds.JSON_CONFIG_MAP_EDITOR_CONFIGURER);
        }

    }

    @SuppressWarnings("unchecked")
    private void registerCustomConfigProcessors(ParserContext parserContext, Set configProcessors, List configProcessorElements) {
        if (!CollectionUtils.isEmpty(configProcessorElements)) {
            Iterator i = configProcessorElements.iterator();
            while (i.hasNext()) {
                Element configProcessorElement = (Element) i.next();
                configProcessors.add(new RuntimeBeanReference(configProcessorElement.getAttribute(REF_ATTR)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void registerEndpointInterceptorIfNecessary(Element securedElement, ParserContext parserContext, ManagedSet interceptors,
        String authManager, String accessManager) {
        if (securedElement.hasChildNodes()) {
            BeanDefinitionBuilder interceptorBuilder = BeanDefinitionBuilder.genericBeanDefinition(ENDPOINT_INTERCEPTOR_CLASS_NAME);
            interceptorBuilder.addPropertyReference(AUTH_MANAGER_PROPERTY, authManager);
            interceptorBuilder.addPropertyReference(ACCESS_MANAGER_PROPERTY, accessManager);

            BeanDefinitionBuilder endpointDefSourceBuilder = BeanDefinitionBuilder.genericBeanDefinition(ENDPOINT_DEFINITION_SOURCE_CLASS_NAME);

            HashMap endpointMap = new HashMap();
            List securedChannelElements = DomUtils.getChildElementsByTagName(securedElement, SECURED_CHANNEL_ELEMENT);
            if (!CollectionUtils.isEmpty(securedChannelElements)) {
                Iterator i = securedChannelElements.iterator();
                while (i.hasNext()) {
                    Element securedChannel = (Element) i.next();
                    String access = securedChannel.getAttribute(ACCESS_ATTR);
                    String channel = securedChannel.getAttribute(CHANNEL_ATTR);
                    Object attributeDefinition = parseConfigAttributeDefinition(access);
                    endpointMap.put(channel, attributeDefinition);
                }
            }

            LinkedHashMap requestMap = new LinkedHashMap();
            List securedEndpointPathElements = DomUtils.getChildElementsByTagName(securedElement, SECURED_ENDPOINT_PATH_ELEMENT);
            if (!CollectionUtils.isEmpty(securedEndpointPathElements)) {
                Iterator i = securedEndpointPathElements.iterator();
                while (i.hasNext()) {
                    Element securedPath = (Element) i.next();
                    String access = securedPath.getAttribute(ACCESS_ATTR);
                    RequestKey pattern = new RequestKey(securedPath.getAttribute(PATTERN_ATTR));
                    Object attributeDefinition = parseConfigAttributeDefinition(access);
                    requestMap.put(pattern, attributeDefinition);
                }
            }

            endpointDefSourceBuilder.addConstructorArgValue(new AntUrlPathMatcher());
            endpointDefSourceBuilder.addConstructorArgValue(requestMap);
            endpointDefSourceBuilder.addConstructorArgValue(endpointMap);

            String endpointDefSourceId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, endpointDefSourceBuilder);
            interceptorBuilder.addPropertyReference(OBJECT_DEF_SOURCE_PROPERTY, endpointDefSourceId);
            String interceptorId = ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, interceptorBuilder);
            interceptors.add(new RuntimeBeanReference(interceptorId));
        }
    }

    @SuppressWarnings("unchecked")
    private void registerEndpointProcessor(ParserContext parserContext, ManagedSet configProcessors, ManagedList advisors, Element securedElement,
        String brokerId) {
        BeanDefinitionBuilder endpointProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(ENDPOINT_PROCESSOR_CLASS_NAME);
        endpointProcessorBuilder.addConstructorArgValue(advisors);
        ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, endpointProcessorBuilder, brokerId
            + BeanIds.ENDPOINT_PROCESSOR_SUFFIX);
        configProcessors.add(new RuntimeBeanReference(brokerId + BeanIds.ENDPOINT_PROCESSOR_SUFFIX));
    }

    @SuppressWarnings("unchecked")
    private void registerExceptionTranslation(Element element, ParserContext parserContext, ManagedList advisors, ManagedSet translators,
        List exceptionTranslatorElements) {

        if (!CollectionUtils.isEmpty(exceptionTranslatorElements)) {
            Iterator i = exceptionTranslatorElements.iterator();
            while (i.hasNext()) {
                Element exceptionTranslatorElement = (Element) i.next();
                translators.add(new RuntimeBeanReference(exceptionTranslatorElement.getAttribute(REF_ATTR)));
            }
        }

        BeanDefinitionBuilder advisorBuilder = BeanDefinitionBuilder.genericBeanDefinition(SERVICE_MESSAGE_ADVISOR_CLASS_NAME);
        BeanDefinitionBuilder exceptionTranslationBuilder = BeanDefinitionBuilder.genericBeanDefinition(EXCEPTION_TRANSLATION_CLASS_NAME);
        exceptionTranslationBuilder.addPropertyValue(EXCEPTION_TRANSLATORS_PROPERTY, translators);
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

    private void registerHandlerAdapterIfNecessary(Element element, ParserContext parserContext) {
        // Make sure we only ever register one MessageBrokerHandlerAdapter
        if (!parserContext.getRegistry().containsBeanDefinition(BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER)) {
            BeanDefinitionBuilder handlerAdapterBuilder = BeanDefinitionBuilder.genericBeanDefinition(MESSAGE_BROKER_HANDLER_ADAPTER_CLASS_NAME);

            ParsingUtils.registerInfrastructureComponent(element, parserContext, handlerAdapterBuilder, BeanIds.MESSAGE_BROKER_HANDLER_ADAPTER);
        }
    }

    @SuppressWarnings("unchecked")
    private void registerHandlerMappings(Element parent, ParserContext parserContext, List mappingPatternElements) {
        BeanDefinitionBuilder handlerMappingBuilder = BeanDefinitionBuilder.genericBeanDefinition(DEFAULT_HANDLER_MAPPING_CLASS_NAME);

        if (StringUtils.hasText(parent.getAttribute(MAPPING_ORDER_ATTR))) {
            handlerMappingBuilder.addPropertyValue(ORDER_PROPERTY, Integer.parseInt(parent.getAttribute(MAPPING_ORDER_ATTR)));
        }

        Map mappings = new HashMap();
        if (CollectionUtils.isEmpty(mappingPatternElements)) {
            mappings.put(DEFAULT_MAPPING_PATH, parent.getAttribute(ID_ATTRIBUTE));
        } else {
            Iterator i = mappingPatternElements.iterator();
            while (i.hasNext()) {
                Element mappingElement = (Element) i.next();
                mappings.put(mappingElement.getAttribute(PATTERN_ATTR), parent.getAttribute(ID_ATTRIBUTE));
            }
        }

        handlerMappingBuilder.addPropertyValue(URL_MAP_PROPERTY, mappings);
        ParsingUtils.registerInfrastructureComponent(parent, parserContext, handlerMappingBuilder, parent.getAttribute(ID_ATTRIBUTE)
            + BeanIds.HANDLER_MAPPING_SUFFIX);
    }

    @SuppressWarnings("unchecked")
    private void registerLoginCommand(String brokerId, ParserContext parserContext, ManagedSet configProcessors, Element securedElement,
        String authManager, boolean perClientAuthentication) {

        String loginCommandId = brokerId + BeanIds.LOGIN_COMMAND_SUFFIX;

        BeanDefinitionBuilder loginCommandBuilder = BeanDefinitionBuilder.genericBeanDefinition(LOGIN_COMMAND_CLASS_NAME);
        loginCommandBuilder.addConstructorArgReference(authManager);
        loginCommandBuilder.addPropertyValue(PER_CLIENT_AUTHENTICATION_PROPERTY, perClientAuthentication);

        ParsingUtils.registerInfrastructureComponent(securedElement, parserContext, loginCommandBuilder, loginCommandId);
        configProcessors.add(new RuntimeBeanReference(loginCommandId));
    }

    @SuppressWarnings("unchecked")
    private void registerMessageInterception(Element element, ParserContext parserContext, ManagedList advisors, ManagedSet interceptors,
        List messageInterceptorElements) {

        if (!CollectionUtils.isEmpty(messageInterceptorElements)) {
            Iterator i = messageInterceptorElements.iterator();
            while (i.hasNext()) {
                Element messageProcessorElement = (Element) i.next();
                interceptors.add(new RuntimeBeanReference(messageProcessorElement.getAttribute(REF_ATTR)));
            }
        }

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
