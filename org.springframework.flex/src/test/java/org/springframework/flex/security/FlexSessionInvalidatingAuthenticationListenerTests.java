package org.springframework.flex.security;

import static org.mockito.Mockito.*;

import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEvent;
import org.springframework.flex.security.FlexSessionInvalidatingAuthenticationListener;
import org.springframework.security.Authentication;
import org.springframework.security.event.authentication.InteractiveAuthenticationSuccessEvent;
import org.springframework.security.ui.webapp.AuthenticationProcessingFilter;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class FlexSessionInvalidatingAuthenticationListenerTests extends
		TestCase {

	private @Mock Authentication authentication;
	private @Mock RequestAttributes attributes;
	
	private FlexSessionInvalidatingAuthenticationListener listener = new FlexSessionInvalidatingAuthenticationListener();
	
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
	}
	
	public void testAuthenticationEvent_RemoveFlexSession() {
		
		RequestContextHolder.setRequestAttributes(attributes);
		
		ApplicationEvent event = new InteractiveAuthenticationSuccessEvent(authentication, AuthenticationProcessingFilter.class);
		
		listener.onApplicationEvent(event);
		
		verify(attributes).removeAttribute("__flexSession", RequestAttributes.SCOPE_SESSION);
	}
	
	public void testAuthenticationEvent_InvalidConfiguration() {
		
		RequestContextHolder.resetRequestAttributes();
		
		ApplicationEvent event = new InteractiveAuthenticationSuccessEvent(authentication, AuthenticationProcessingFilter.class);
		
		listener.onApplicationEvent(event);
		
		verify(attributes, never()).removeAttribute("__flexSession", RequestAttributes.SCOPE_SESSION);
	}
	
	@SuppressWarnings("serial")
	public void testOtherEvent() {
		
		RequestContextHolder.resetRequestAttributes();
				
		listener.onApplicationEvent(new ApplicationEvent(this) {});
		
		verify(attributes, never()).removeAttribute("__flexSession", RequestAttributes.SCOPE_SESSION);
	}

}
