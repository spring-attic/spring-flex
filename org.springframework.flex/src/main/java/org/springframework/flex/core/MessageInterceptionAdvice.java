package org.springframework.flex.core;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import flex.messaging.messages.Message;

public class MessageInterceptionAdvice implements MethodInterceptor {
	
	private Set<MessageInterceptor> messageInterceptors = new HashSet<MessageInterceptor>();
	
	public Object invoke(MethodInvocation mi) throws Throwable {
		MessageProcessingContext context = new MessageProcessingContext(mi.getThis());
		Message inputMessage = (Message) mi.getArguments()[0];
		for (MessageInterceptor interceptor : messageInterceptors) {
			inputMessage = interceptor.preProcess(context, inputMessage);
		}
		mi.getArguments()[0] = inputMessage;
		Message outputMessage = null;
		try {
			outputMessage = (Message) mi.proceed();
		} finally {
			if (outputMessage != null) {
				Stack<MessageInterceptor> postProcessStack = new Stack<MessageInterceptor>();
				postProcessStack.addAll(messageInterceptors);
				while (!postProcessStack.empty()) {
					MessageInterceptor interceptor = postProcessStack.pop();
					outputMessage = interceptor.postProcess(context, inputMessage, outputMessage);
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
