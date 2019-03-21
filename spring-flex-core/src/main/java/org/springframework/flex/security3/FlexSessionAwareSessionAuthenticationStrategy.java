/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.security3;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

import flex.messaging.FlexSession;
import flex.messaging.HttpFlexSession;
import flex.messaging.HttpFlexSessionProvider;
import flex.messaging.MessageBroker;

/**
 * Delegating implementation of {@link SessionAuthenticationStrategy} that ensures proper handling of the {@link FlexSession} 
 * when session fixation protection is enabled.
 *
 * @author Jeremy Grelle
 */
public class FlexSessionAwareSessionAuthenticationStrategy implements SessionAuthenticationStrategy {

	private final SessionAuthenticationStrategy delegate;
	
	public FlexSessionAwareSessionAuthenticationStrategy(SessionAuthenticationStrategy delegate) {
		this.delegate = delegate;
	}

	public void onAuthentication(Authentication authentication, HttpServletRequest request, HttpServletResponse response) throws SessionAuthenticationException {
		HttpSession currentSession = request.getSession(false);
		String sessionId = currentSession != null ? currentSession.getId() : null;
		delegate.onAuthentication(authentication, request, response);
		currentSession = request.getSession(false);
		if (currentSession != null && !currentSession.getId().equals(sessionId)) {
			if (currentSession.getAttribute("__flexSession") != null) {
				currentSession.removeAttribute("__flexSession");
			}
			HttpFlexSessionProvider provider = (HttpFlexSessionProvider) MessageBroker.getMessageBroker("_messageBroker").
				getFlexSessionManager().getFlexSessionProvider(HttpFlexSession.class);
			provider.getOrCreateSession(request);
		}
	}
}
