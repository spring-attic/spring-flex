package org.springframework.flex.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * {@link ApplicationListener} implementation that listens for
 * {@link InteractiveAuthenticationSuccessEvent}s and cleans up any existing
 * FlexSession, forcing it to be re-created.
 * 
 * <p>
 * This listener is provided for compatability with Spring Security's session
 * fixation protection feature, and should be included in the ApplicationContext
 * if session fixation protection is enabled.
 * 
 * <p>
 * When using the config namespace, this listener will be configured
 * automatically if necessary when Spring Security integration is enabled.
 * 
 * @author Jeremy Grelle
 * 
 */
public class FlexSessionInvalidatingAuthenticationListener implements
		ApplicationListener {

	private static final String FLEX_SESSION_KEY = "__flexSession";

	private static final Log log = LogFactory
			.getLog(FlexSessionInvalidatingAuthenticationListener.class);

	public void onApplicationEvent(ApplicationEvent event) {
		if (event instanceof InteractiveAuthenticationSuccessEvent) {
			if (RequestContextHolder.getRequestAttributes() == null) {
				log
						.error("Unable to find a thread-bound RequestAttributes object.  You must install either a RequestContextListener or RequestContextFilter in order " +
								"for this listener to have access to the current HttpSession to be able to clean up the FlexSession correctly.");
				return;
			}
			RequestContextHolder.getRequestAttributes().removeAttribute(FLEX_SESSION_KEY, RequestAttributes.SCOPE_SESSION);
		}
	}

}
