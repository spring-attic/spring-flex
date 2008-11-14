package org.springframework.flex.messaging;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.ModelAndView;

import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.endpoints.Endpoint;
import flex.messaging.log.Log;
import flex.messaging.log.LogCategories;
import flex.messaging.services.AuthenticationService;

public class MessageBrokerHandlerAdapter implements HandlerAdapter, ServletConfigAware {

	private ServletConfig servletConfig;

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	public ModelAndView handle(HttpServletRequest req, HttpServletResponse res, Object handler)
			throws Exception {
		MessageBroker broker = (MessageBroker) handler;
		
		try
	        {
	            // Update thread locals
	            broker.initThreadLocals();
	            // Set this first so it is in place for the session creation event.  The
	            // current session is set by the FlexSession stuff right when it is available.
	            // The threadlocal FlexClient is set up during message deserialization in the
	            // MessageBrokerFilter.
	            FlexContext.setThreadLocalObjects(null, null, broker, req, res, servletConfig);
	            HttpFlexSession fs = HttpFlexSession.getFlexSession(req);
	            Principal principal = null;
	            if(FlexContext.isPerClientAuthentication())
	            {
	                principal = FlexContext.getUserPrincipal();
	            }
	            else
	            {
	                principal = fs.getUserPrincipal();
	            }

	            if (principal == null && req.getHeader("Authorization") != null)
	            {
	                String encoded = req.getHeader("Authorization");
	                if (encoded.indexOf("Basic") > -1)
	                {
	                    encoded = encoded.substring(6); //Basic.length()+1
	                    try
	                    {
	                        AuthenticationService.decodeAndLogin(encoded, broker.getLoginManager());
	                    }
	                    catch (Exception e)
	                    {
	                        if (Log.isDebug())
	                            Log.getLogger(LogCategories.SECURITY).info("Authentication service could not decode and login: " + e.getMessage());
	                    }
	                }
	            }

	            String contextPath = req.getContextPath();
	            String pathInfo = req.getPathInfo();
	            String endpointPath = req.getServletPath();
	            if (pathInfo != null)
	                endpointPath = endpointPath + pathInfo;

	            Endpoint endpoint = null;
	            try
	            {
	                endpoint = broker.getEndpoint(endpointPath, contextPath);
	            }
	            catch (MessageException me)
	            {
	                if (Log.isInfo())
	                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Received invalid request for endpoint path '{0}'.", new Object[] {endpointPath});
	                
	                if (!res.isCommitted())
	                {
	                    try
	                    {                    
	                        res.sendError(HttpServletResponse.SC_NOT_FOUND);
	                    }
	                    catch (IOException ignore)
	                    {}
	                }
	                
	                return null;
	            }
	            
	            try
	            {
	                if (Log.isInfo())
	                {
	                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Channel endpoint {0} received request.", 
	                                                                       new Object[] {endpoint.getId()});
	                }
	                endpoint.service(req, res);
	            }
	            catch (UnsupportedOperationException ue)
	            {
	                if (Log.isInfo())
	                {
	                    Log.getLogger(LogCategories.ENDPOINT_GENERAL).info("Channel endpoint {0} received request for an unsupported operation.", 
	                                                                       new Object[] {endpoint.getId()}, 
	                                                                       ue);
	                }
	                
	                if (!res.isCommitted())
	                {
	                    try
	                    {                        
	                        res.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	                    }
	                    catch (IOException ignore)
	                    {}
	                }
	            }
	        }
	        finally
	        {
	            FlexContext.clearThreadLocalObjects();
	        }
		
		return null;
	}

	public boolean supports(Object handler) {
		return handler instanceof MessageBroker;
	}

	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

}
