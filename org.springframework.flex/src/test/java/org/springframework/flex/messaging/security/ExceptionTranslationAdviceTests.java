package org.springframework.flex.messaging.security;

import static org.mockito.Mockito.when;
import junit.framework.TestCase;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.flex.messaging.EndpointServiceMessagePointcutAdvisor;
import org.springframework.flex.messaging.ExceptionTranslationAdvice;
import org.springframework.flex.messaging.ExceptionTranslator;

import flex.messaging.MessageException;
import flex.messaging.endpoints.AbstractEndpoint;
import flex.messaging.messages.Message;

public class ExceptionTranslationAdviceTests extends TestCase{
	
	@Mock private AbstractEndpoint endpoint; 
	@Mock private Message inMessage;
	@Mock private Message outMessage;
	private AbstractEndpoint advisedEndpoint;
	
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		
		ProxyFactory factory = new ProxyFactory();
		factory.setProxyTargetClass(true);
		ExceptionTranslationAdvice advice = new ExceptionTranslationAdvice();
		advice.getExceptionTranslators().put(TestException.class, new TestExceptionTranslator());
		factory.addAdvisor(new EndpointServiceMessagePointcutAdvisor(advice));
		factory.setTarget(endpoint);
		advisedEndpoint = (AbstractEndpoint) factory.getProxy();
	}
	
	public void testKnownException() {
		
		when(endpoint.serviceMessage(inMessage)).thenThrow(new TestException());
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail();
		} catch (MessageException ex) {
			assertEquals(TestExceptionTranslator.PROCESSED_CODE, ex.getCode());
			assertTrue(ex.getRootCause() instanceof TestException);
		}
	}
	
	public void testKnownNestedException() {
		
		MessageException wrapper = new MessageException();
		wrapper.setCode("Server.Processing");
		wrapper.setRootCause(new TestException());
		when(endpoint.serviceMessage(inMessage)).thenThrow(wrapper);
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail();
		} catch (MessageException ex) {
			assertEquals(TestExceptionTranslator.PROCESSED_CODE, ex.getCode());
			assertTrue(ex.getRootCause() instanceof TestException);
		}
	}
	
	public void testUnknownExceptionPassthrough() {
		
		MessageException expected = new MessageException();
		when(endpoint.serviceMessage(inMessage)).thenThrow(expected);
		
		try {
			advisedEndpoint.serviceMessage(inMessage);
			fail();
		} catch (MessageException ex) {
			assertSame(expected, ex);
		}
	}
	
	public void testNormalReturnPassthrough() {
		
		when(endpoint.serviceMessage(inMessage)).thenReturn(outMessage);
		
		Message result = advisedEndpoint.serviceMessage(inMessage);
		
		assertSame(outMessage, result);
	}
	
	@SuppressWarnings("serial")
	public class TestException extends RuntimeException {
		
	}
	
	public class TestExceptionTranslator implements ExceptionTranslator {

		public static final String PROCESSED_CODE = "Custom.Processed";
		
		public MessageException translate(Throwable t) {
			if (t instanceof TestException) {
				MessageException result = new MessageException();
				result.setRootCause(t);
				result.setCode(PROCESSED_CODE);
				return result;
			}
			return null;
		}
		
	}

}
