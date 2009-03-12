package org.springframework.flex.messaging;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.ThrowsAdvice;
import org.springframework.util.ClassUtils;

import flex.messaging.MessageException;

/**
 * Catches Throwable objects and looks for a registered {@link ExceptionTranslator} that known how to translate them 
 * to more specific BlazeDS MessageExceptions to be rethrown so that a proper AMF error will be sent back to the client.
 * 
 * <p>
 * If the caught object is an instance of {@link MessageException} with the generic "Server.Processing" fault code,
 * a translator will be looked up for the root cause exception type rather than the generic wrapper MessageException.
 * 
 * @author Jeremy Grelle
 */
public class ExceptionTranslationAdvice implements ThrowsAdvice {
	
	private static final String SERVER_PROCESSING_CODE = "Server.Processing";
	
	private Set<ExceptionTranslator> exceptionTranslators = new HashSet<ExceptionTranslator>();

	public void afterThrowing(Throwable t) throws Throwable {
		
		Class<?> candidate = t.getClass();
		
		if (ClassUtils.isAssignable(MessageException.class, candidate)) {
			MessageException me = (MessageException) t;
			if (SERVER_PROCESSING_CODE.equals(me.getCode()) && me.getRootCause() != null) {
				candidate = me.getRootCause().getClass();
				t = me.getRootCause();
			}
		}
		
		for (ExceptionTranslator translator : exceptionTranslators) {
			if (translator.handles(candidate)) {
				MessageException result = translator.translate(t);
				if (result != null) {
					throw result;
				}
			}
		}
		throw t;
	}
	
	public Set<ExceptionTranslator> getExceptionTranslators() {
		return exceptionTranslators;
	}

	public void setExceptionTranslators(Set<ExceptionTranslator> translators) {
		this.exceptionTranslators = translators;
	}
	
}
