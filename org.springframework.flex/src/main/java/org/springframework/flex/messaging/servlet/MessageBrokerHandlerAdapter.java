package org.springframework.flex.messaging.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.context.ServletConfigAware;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import flex.messaging.FlexContext;
import flex.messaging.HttpFlexSession;
import flex.messaging.MessageBroker;
import flex.messaging.MessageException;
import flex.messaging.endpoints.Endpoint;

/**
 * {@link HandlerAdapter} for routing HTTP messages to a Spring-managed
 * {@link MessageBroker}.
 * 
 * <p>
 * This class is automatically registered with the application context when using the <code>message-broker</code> tag
 * in the xml configuration namespace.
 * 
 * @see MessageBroker
 * @see HandlerMapping
 * 
 * @author Jeremy Grelle
 */
public class MessageBrokerHandlerAdapter implements HandlerAdapter,
		ServletConfigAware {

	private static final Log logger = LogFactory
			.getLog(MessageBrokerHandlerAdapter.class);

	private ServletConfig servletConfig;

	public long getLastModified(HttpServletRequest request, Object handler) {
		return -1;
	}

	public ModelAndView handle(HttpServletRequest req, HttpServletResponse res,
			Object handler) throws Exception {
		MessageBroker broker = (MessageBroker) handler;

		try {
			// Update thread locals
			broker.initThreadLocals();
			// Set this first so it is in place for the session creation event.
			FlexContext.setThreadLocalObjects(null, null, broker, req, res,
					servletConfig);
			HttpFlexSession.getFlexSession(req);

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
				if (logger.isInfoEnabled()) {
					logger.info("Received invalid request for endpoint path '"
							+ endpointPath + "'.");
				}

				if (!res.isCommitted()) {
					res.sendError(HttpServletResponse.SC_NOT_FOUND);
				}

				return null;
			}

			try {
				if (logger.isInfoEnabled()) {
					logger.info(
							"Channel endpoint "+endpoint.getId()+" received request.");
				}
				endpoint.service(req, res);
			} catch (UnsupportedOperationException ue) {
				if (logger.isInfoEnabled()) {
					logger.info("Channel endpoint "+endpoint.getId()+" received request for an unsupported operation.", ue);
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

	public boolean supports(Object handler) {
		return handler instanceof MessageBroker;
	}

	public void setServletConfig(ServletConfig servletConfig) {
		this.servletConfig = servletConfig;
	}

}
