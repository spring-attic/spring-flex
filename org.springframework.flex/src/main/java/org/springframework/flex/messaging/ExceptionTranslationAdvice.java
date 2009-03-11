package org.springframework.flex.messaging;

import java.util.LinkedHashMap;

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
	
	private LinkedHashMap<Class<?>, ExceptionTranslator> exceptionTranslators = new LinkedHashMap<Class<?>, ExceptionTranslator>();

	public void afterThrowing(Throwable t) throws Throwable {
		
		Class<?> key = t.getClass();
		
		if (ClassUtils.isAssignable(MessageException.class, key)) {
			MessageException me = (MessageException) t;
			if (SERVER_PROCESSING_CODE.equals(me.getCode()) && me.getRootCause() != null) {
				key = me.getRootCause().getClass();
				t = me.getRootCause();
			}
		}
		
		for (Class<?> candidate : exceptionTranslators.keySet()) {
			if (ClassUtils.isAssignable(candidate, key)) {
				MessageException result = exceptionTranslators.get(candidate).translate(t);
				if (result != null) {
					throw result;
				}
			}
		}
		throw t;
	}
	
	public LinkedHashMap<Class<?>, ExceptionTranslator> getExceptionTranslators() {
		return exceptionTranslators;
	}

	public void setExceptionTranslators(LinkedHashMap<Class<?>, ExceptionTranslator> translators) {
		this.exceptionTranslators = translators;
	}
	
}
