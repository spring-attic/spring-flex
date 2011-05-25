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

package org.springframework.flex.security3;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.flex.core.ExceptionTranslator;
import org.springframework.flex.http.AmfHttpMessageConverter;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.ExceptionTranslationFilter;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.util.CollectionUtils;

import flex.messaging.MessageException;
import flex.messaging.io.MessageIOConstants;
import flex.messaging.io.amf.ActionMessage;
import flex.messaging.io.amf.MessageBody;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;

/**
 * An {@link AuthenticationEntryPoint} implementation to be used in conjunction with an authentication 
 * process that is completely driven by a Flex client, i.e. by presenting a Flex-based login UI and using 
 * the client-side ChannelSet API to commence authentication.  
 * 
 * <p>Mostly this class exists to satisfy the requirements of Spring Security, where it requires an 
 * <code>AuthenticationEntryPoint</code> to be provided to the {@link ExceptionTranslationFilter}.  Only in 
 * relatively exceptional cases (such as using the <code>intercept-url</code> tag to secure BlazeDS URLs, which is 
 * not recommended in preference for using Spring BlazeDS's <code>secured-endpoint-path</code> and 
 * <code>secured-channel</code> tags when using Remoting and Messaging destinations) should this implementation's 
 * {@link #commence(HttpServletRequest, HttpServletResponse, AuthenticationException)} method ever actually 
 * be invoked, as in the majority case security exceptions will never propagate out to the 
 * <code>ExceptionTranslationFilter</code>, instead being converted to a {@link MessageException} by the 
 * provided {@link ExceptionTranslator}s.  One such exceptional case might be when using RESTful Spring MVC 
 * endpoints to read and write AMF instead of the traditional RPC approach.
 * 
 * <p>When this class is used in conjunction with the XML config namespace for Flex, it will be automatically 
 * detected and its {@link #exceptionTranslators} will be configured automatically if they have not already been 
 * set explicitly as part of bean configuration.
 *
 * @author Jeremy Grelle
 */
public class FlexAuthenticationEntryPoint extends Http403ForbiddenEntryPoint {

	private static final Log log = LogFactory.getLog(FlexAuthenticationEntryPoint.class);
	
	private static final ExceptionTranslator DEFAULT_TRANSLATOR = new SecurityExceptionTranslator(); 
	
	private final AmfHttpMessageConverter converter = new AmfHttpMessageConverter();
	
	private final MediaType amfMediaType = new MediaType("application", "x-amf");
	
	private Set<ExceptionTranslator> exceptionTranslators;
	
	/**
	 * If the incoming message is an {@link ActionMessage}, indicating a standard Flex Remoting or Messaging 
	 * request, invokes Spring BlazeDS's {@link ExceptionTranslator}s with the {@link AuthenticationException} and 
	 * sends the resulting {@link MessageException} as an AMF response to the client.
	 * 
	 * <p>If the request is unabled to be deserialized to AMF, if the resulting deserialized object is not an 
	 * <code>ActionMessage</code>, or if no appropriate <code>ExceptionTranslator</code> is found, will simply 
	 * delegate to the parent class to return a 403 response.
	 */
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
			throws IOException, ServletException {
		
		if (CollectionUtils.isEmpty(this.exceptionTranslators)) {
			exceptionTranslators = Collections.singleton(DEFAULT_TRANSLATOR);
		}
		
		HttpInputMessage inputMessage = new ServletServerHttpRequest(request);
		HttpOutputMessage outputMessage = new ServletServerHttpResponse(response);
		
		if (!converter.canRead(Object.class, inputMessage.getHeaders().getContentType())) {
			super.commence(request, response, authException);
		}
		
		ActionMessage deserializedInput = null;
		try {
			deserializedInput = (ActionMessage) this.converter.read(ActionMessage.class, inputMessage); 
		} catch (HttpMessageNotReadableException ex) {
			log.info("Authentication failure detected, but request could not be read as AMF.", ex);
			super.commence(request, response, authException);
			return;
		}
		
		if (deserializedInput instanceof ActionMessage) {
			for (ExceptionTranslator translator : this.exceptionTranslators) {
	            if (translator.handles(authException.getClass())) {
	                MessageException result = translator.translate(authException);
	                ErrorMessage err = result.createErrorMessage();
                	MessageBody body = (MessageBody) ((ActionMessage) deserializedInput).getBody(0);
                	Message amfInputMessage = body.getDataAsMessage(); 
                	err.setCorrelationId(amfInputMessage.getMessageId());
                	err.setDestination(amfInputMessage.getDestination());
                	err.setClientId(amfInputMessage.getClientId());
                	ActionMessage responseMessage = new ActionMessage();
                    responseMessage.setVersion(((ActionMessage)deserializedInput).getVersion());
                    MessageBody responseBody = new MessageBody();
                    responseMessage.addBody(responseBody);
                    responseBody.setData(err);
                    responseBody.setTargetURI(body.getResponseURI());
                    responseBody.setReplyMethod(MessageIOConstants.STATUS_METHOD);
                    converter.write(responseMessage, amfMediaType, outputMessage);
                    response.flushBuffer();
                    return;
	            }
	        }
		}
		super.commence(request, response, authException);
	}

	public Set<ExceptionTranslator> getExceptionTranslators() {
		return exceptionTranslators;
	}

	public void setExceptionTranslators(
			Set<ExceptionTranslator> exceptionTranslators) {
		this.exceptionTranslators = exceptionTranslators;
	}
}
