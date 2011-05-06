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

package org.springframework.flex.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.HttpFlexSessionProvider;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.endpoints.Endpoint;

/**
 * {@link HandlerAdapter} for routing HTTP messages to a Spring-managed {@link MessageBroker}.
 * 
 * <p>
 * This class is automatically registered with the application context when using the <code>message-broker</code> tag in
 * the xml configuration namespace.
 * 
 * @see MessageBroker
 * @see HandlerMapping
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerHandlerAdapter implements HandlerAdapter, ServletConfigAware {

    private static final Log logger = LogFactory.getLog(MessageBrokerHandlerAdapter.class);

    private ServletConfig servletConfig;

    /**
     * 
     * {@inheritDoc}
     */
    public long getLastModified(HttpServletRequest request, Object handler) {
        return -1;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public ModelAndView handle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        MessageBroker broker = (MessageBroker) handler;

        try {
            // Update thread locals
            broker.initThreadLocals();
            
            // Set this first so it is in place for the session creation event.
            FlexContext.setThreadLocalObjects(null, null, broker, req, res, this.servletConfig);
            
            Object providerToCheck = broker.getFlexSessionManager().getFlexSessionProvider(HttpFlexSession.class);
            Assert.isInstanceOf(HttpFlexSessionProvider.class, providerToCheck, "MessageBrokerHandlerAdapter requires an instance of "+HttpFlexSessionProvider.class.getName()+ " to have been registered with the MessageBroker.");
            HttpFlexSessionProvider provider = (HttpFlexSessionProvider) providerToCheck;
            provider.getOrCreateSession(req);

            String contextPath = req.getContextPath();
            String pathInfo = req.getPathInfo();
            String endpointPath = req.getServletPath();
            if (pathInfo != null) {
                endpointPath = endpointPath + pathInfo;
            }
            Endpoint endpoint = null;
            try {
                endpoint = broker.getEndpoint(endpointPath, contextPath);
            } catch (MessageException me) {
                if (logger.isErrorEnabled()) {
                    logger.error("Received invalid request for endpoint path '" + endpointPath + "'.");
                }

                if (!res.isCommitted()) {
                    res.sendError(HttpServletResponse.SC_NOT_FOUND);
                }

                return null;
            }

            try {
                if (logger.isInfoEnabled()) {
                    logger.info("Channel endpoint " + endpoint.getId() + " received request.");
                }
                endpoint.service(req, res);
            } catch (UnsupportedOperationException ue) {
                if (logger.isErrorEnabled()) {
                    logger.error("Channel endpoint " + endpoint.getId() + " received request for an unsupported operation.", ue);
                }

                if (!res.isCommitted()) {
                    res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
                }
            }
        } finally {
            FlexContext.clearThreadLocalObjects();
        }

        return null;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public void setServletConfig(ServletConfig servletConfig) {
        this.servletConfig = servletConfig;
    }

    /**
     * 
     * {@inheritDoc}
     */
    public boolean supports(Object handler) {
        return handler instanceof MessageBroker;
    }

}
