package org.springframework.flex.core;

import static org.mockito.Mockito.*;
import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;

import flex.messaging.MessageException;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;

public class MessageInterceptionAdviceTests extends TestCase {

	@Mock private AbstractEndpoint endpoint; 
	@Mock private Message inMessage;
	@Mock private Message outMessage;
	@Mock private Message mutatedInMessage;
	@Mock private Message mutatedOutMessage;
	private AbstractEndpoint advisedEndpoint;
	
	public void setUp() {
		MockitoAnnotations.initMocks(this);
	}
	
	public final void testPassthroughInterceptor() {
		
		PassthroughInterceptor interceptor = new PassthroughInterceptor();
		setupInterceptor(interceptor);
		when(advisedEndpoint.serviceMessage(inMessage)).thenReturn(outMessage);
		
		Message result = advisedEndpoint.serviceMessage(inMessage);
		
		assertSame(outMessage, result);
		assertTrue(interceptor.preInvoked);
		assertTrue(interceptor.postInvoked);
				
	}
	
	public final void testMessageMutatingInterceptor() {
		
		MessageMutatingInterceptor interceptor = new MessageMutatingInterceptor();
		setupInterceptor(interceptor);
		when(advisedEndpoint.serviceMessage(mutatedInMessage)).thenReturn(outMessage);
		
		Message result = advisedEndpoint.serviceMessage(inMessage);
		
		assertNotNull(result);
		assertSame(mutatedOutMessage, result);
		assertTrue(interceptor.preInvoked);
		assertTrue(interceptor.postInvoked);
				
	}
	
	public final void testExceptionPassthrough() {
		PassthroughInterceptor interceptor = new PassthroughInterceptor();
		setupInterceptor(interceptor);
		MessageException error = new MessageException();
		when(advisedEndpoint.serviceMessage(inMessage)).thenThrow(error);
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
		} catch (MessageException ex) {
			assertSame(error, ex);
			assertTrue(interceptor.preInvoked);
			assertFalse(interceptor.postInvoked);
		}
	}
	
	private void setupInterceptor(MessageInterceptor interceptor) {
		ProxyFactory factory = new ProxyFactory();
		factory.setProxyTargetClass(true);
		MessageInterceptionAdvice advice = new MessageInterceptionAdvice();
		advice.getMessageInterceptors().add(interceptor);
		factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(advice));
		factory.setTarget(endpoint);
		advisedEndpoint = (AbstractEndpoint) factory.getProxy();
	}
	
	public class PassthroughInterceptor implements MessageInterceptor {
		
		protected boolean preInvoked = false;
		protected boolean postInvoked = false;
		
		public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
			postInvoked = true;
			return outputMessage;
		}

		public Message preProcess(MessageProcessingContext context, Message inputMessage) {
			preInvoked = true;
			return inputMessage;
		}
	}
	
	public class MessageMutatingInterceptor implements MessageInterceptor {
		
		protected boolean preInvoked = false;
		protected boolean postInvoked = false;
		
		public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage) {
			postInvoked = true;
			return mutatedOutMessage;
		}

		public Message preProcess(MessageProcessingContext context, Message inputMessage) {
			preInvoked = true;
			return mutatedInMessage;
		}
	}

}
