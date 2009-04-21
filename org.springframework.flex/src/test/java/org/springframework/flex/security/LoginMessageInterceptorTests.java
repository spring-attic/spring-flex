package org.springframework.flex.security;

import java.util.Map;

import junit.framework.TestCase;

import org.mockito.MockitoAnnotations;
import org.springframework.security.Authentication;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.security.providers.UsernamePasswordAuthenticationToken;

import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.Message;

public class LoginMessageInterceptorTests extends TestCase {

	private CommandMessage inputMessage;
	private AcknowledgeMessage outputMessage;
	
	private LoginMessageInterceptor interceptor;
	
	protected void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		interceptor = new LoginMessageInterceptor();
	}
	
	public final void testPreProcessPassThrough() {
		
		inputMessage = new CommandMessage(CommandMessage.LOGIN_OPERATION);
		
		assertSame(inputMessage, interceptor.preProcess(inputMessage));
	}
	
	public final void testPostProcessPassThrough() {
		
		inputMessage = new CommandMessage(CommandMessage.CLIENT_PING_OPERATION);
		outputMessage = new AcknowledgeMessage();
		assertSame(outputMessage, interceptor.postProcess(inputMessage, outputMessage));
	}
	
	@SuppressWarnings("unchecked")
	public final void testPostProcessSuccessfulLogin() {
		
		Authentication auth = new UsernamePasswordAuthenticationToken("foo",
				"bar", new GrantedAuthority[] {new GrantedAuthorityImpl("ROLE_USER")});
		SecurityContextHolder.getContext().setAuthentication(auth);
		
		inputMessage = new CommandMessage(CommandMessage.LOGIN_OPERATION);
		outputMessage = new AcknowledgeMessage();
		outputMessage.setBody("success");
		
		Message result = interceptor.postProcess(inputMessage, outputMessage);
		
		assertTrue(result.getBody() instanceof Map);	
		Map authResult = (Map) result.getBody();
		assertEquals("foo",authResult.get("name"));
		assertEquals("ROLE_USER", ((String[])authResult.get("authorities"))[0]);
	}

}
