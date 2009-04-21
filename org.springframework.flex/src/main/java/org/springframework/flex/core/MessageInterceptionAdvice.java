package org.springframework.flex.core;

import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import flex.messaging.messages.Message;

public class MessageInterceptionAdvice implements MethodInterceptor {
	
	private Set<MessageInterceptor> messageInterceptors = new HashSet<MessageInterceptor>();
	
	public Object invoke(MethodInvocation mi) throws Throwable {
		
		Message inputMessage = (Message) mi.getArguments()[0];
		for (MessageInterceptor interceptor : messageInterceptors) {
			inputMessage = interceptor.preProcess(inputMessage);
		}
		mi.getArguments()[0] = inputMessage;
		Message outputMessage = null;
		try {
			outputMessage = (Message) mi.proceed();
		} finally {
			if (outputMessage != null) {
				for (MessageInterceptor interceptor : messageInterceptors) {
					outputMessage = interceptor.postProcess(inputMessage, outputMessage);
				} 
			}
		}
		return outputMessage;
	}

	public Set<MessageInterceptor> getMessageInterceptors() {
		return messageInterceptors;
	}

	public void setMessageInterceptors(Set<MessageInterceptor> messageInterceptors) {
		this.messageInterceptors = messageInterceptors;
	}
	
}
