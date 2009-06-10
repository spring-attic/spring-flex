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

package org.springframework.flex.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * {@link ApplicationListener} implementation that listens for {@link InteractiveAuthenticationSuccessEvent}s and cleans
 * up any existing FlexSession, forcing it to be re-created.
 * 
 * <p>
 * This listener is provided for compatability with Spring Security's session fixation protection feature, and should be
 * included in the ApplicationContext if session fixation protection is enabled.
 * 
 * <p>
 * When using the config namespace, this listener will be configured automatically if necessary when Spring Security
 * integration is enabled.
 * 
 * @author Jeremy Grelle
 * 
 */
public class FlexSessionInvalidatingAuthenticationListener implements ApplicationListener {

    private static final String FLEX_SESSION_KEY = "__flexSession";

    private static final Log log = LogFactory.getLog(FlexSessionInvalidatingAuthenticationListener.class);

    /**
     * 
     * {@inheritDoc}
     */
    public void onApplicationEvent(ApplicationEvent event) {
        if (event instanceof InteractiveAuthenticationSuccessEvent) {
            if (RequestContextHolder.getRequestAttributes() == null) {
                log.error("Unable to find a thread-bound RequestAttributes object.  You must install either a RequestContextListener or RequestContextFilter in order "
                    + "for this listener to have access to the current HttpSession to be able to clean up the FlexSession correctly.");
                return;
            }
            RequestContextHolder.getRequestAttributes().removeAttribute(FLEX_SESSION_KEY, RequestAttributes.SCOPE_SESSION);
        }
    }

}
