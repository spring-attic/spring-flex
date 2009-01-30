package org.springframework.flex.messaging.security;

import java.io.IOException;
import java.lang.reflect.Method;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.context.SecurityContext;
import org.springframework.security.context.SecurityContextImpl;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;

import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageBroker;

public class FlexSessionContextIntegrationFilter extends SpringSecurityFilter
		implements InitializingBean {

	 //~ Static fields/initializers =====================================================================================

    static final String FILTER_APPLIED = "__spring_security_flex_session_integration_filter_applied";

    public static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    private MessageBroker messageBroker;

    private Class contextClass = SecurityContextImpl.class;

    private Object contextObject;
    
    public FlexSessionContextIntegrationFilter() throws ServletException {
        this.contextObject = generateNewContext();
    }
    
    public FlexSessionContextIntegrationFilter(MessageBroker messageBroker) throws ServletException {
    	this.messageBroker = messageBroker;
        this.contextObject = generateNewContext();
    }

	@Override
	protected void doFilterHttp(HttpServletRequest request,
			HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		if (request.getAttribute(FILTER_APPLIED) != null) {
            // ensure that filter is only applied once per request
            chain.doFilter(request, response);

            return;
        }

        HttpSession httpSession = (HttpSession) HttpFlexSession.getFlexSession(request);
        SecurityContext contextBeforeChainExecution = readSecurityContextFromFlexContext();
	}

	public void afterPropertiesSet() throws Exception {
        if ((this.contextClass == null) || (!SecurityContext.class.isAssignableFrom(this.contextClass))) {
            throw new IllegalArgumentException("context must be defined and implement SecurityContext "
                    + "(typically use org.springframework.security.context.SecurityContextImpl; existing class is "
                    + this.contextClass + ")");
        }
        
        contextObject = generateNewContext();
    }

	public int getOrder() {
		return FilterChainOrder.HTTP_SESSION_CONTEXT_FILTER;
	}
	
	public SecurityContext generateNewContext() throws ServletException {
        try {
            return (SecurityContext) this.contextClass.newInstance();
        }
        catch (InstantiationException ie) {
            throw new ServletException(ie);
        }
        catch (IllegalAccessException iae) {
            throw new ServletException(iae);
        }
    }
	
	/**
     * Gets the security context from the Flex context and returns it.
     * <p/>
     * If <tt>cloneFromHttpSession</tt> is set to true, it will attempt to clone the context object
     * and return the cloned instance.
     *
     * @param httpSession the session obtained from the request.
     */
    private SecurityContext readSecurityContextFromFlexContext() {

    	Object context;
    	if (messageBroker.getLoginManager() == null || messageBroker.getLoginManager().isPerClientAuthentication()) {
    		context = FlexContext.getFlexClient().getAttribute(SPRING_SECURITY_CONTEXT_KEY);
    	} else {
    		context = FlexContext.getFlexSession().getAttribute(SPRING_SECURITY_CONTEXT_KEY);
    	}

        if (context == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("FlexContext returned null object for SPRING_SECURITY_CONTEXT");
            }

            return null;
        }

        // We now have the security context object from the session.

        if (!(context instanceof SecurityContext)) {
            if (logger.isWarnEnabled()) {
                logger.warn("SPRING_SECURITY_CONTEXT did not contain a SecurityContext but contained: '"
                        + context
                        + "'; are you improperly modifying the FlexContext directly "
                        + "(you should always use SecurityContextHolder) or using the FlexContext attribute "
                        + "reserved for this class?");
            }

            return null;
        }

        // Everything OK. The only non-null return from this method.

        return (SecurityContext) context;
    }

}
