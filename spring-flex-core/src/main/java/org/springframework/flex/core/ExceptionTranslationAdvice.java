/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.flex.core;

import java.util.HashSet;
import java.util.Set;

import org.springframework.aop.ThrowsAdvice;
import org.springframework.util.ClassUtils;

import flex.messaging.MessageException;

/**
 * Catches Throwable objects and looks for a registered {@link ExceptionTranslator} that knows how to translate them to
 * more specific BlazeDS MessageExceptions to be re-thrown so that a proper AMF error will be sent back to the client.
 * 
 * <p>
 * If the caught object is an instance of {@link MessageException} with the generic "Server.Processing" fault code, a
 * translator will be looked up for the root cause exception type rather than the generic wrapper MessageException.
 * 
 * @author Jeremy Grelle
 */
public class ExceptionTranslationAdvice implements ThrowsAdvice {

    private static final String SERVER_PROCESSING_CODE = "Server.Processing";

    private Set<ExceptionTranslator> exceptionTranslators = new HashSet<ExceptionTranslator>();
    
    private ExceptionLogger exceptionLogger = new DefaultExceptionLogger();

    /**
     * Apply translation to the thrown exception.
     * 
     * @param original the thrown exception
     * @throws Throwable the translated exception
     */
    public void afterThrowing(Throwable original) throws Throwable {

        Class<?> candidateType = original.getClass();
        Throwable candidate = original;

        if (ClassUtils.isAssignable(MessageException.class, candidateType)) {
            MessageException me = (MessageException) candidate;
            if (SERVER_PROCESSING_CODE.equals(me.getCode()) && me.getRootCause() != null) {
                candidateType = me.getRootCause().getClass();
                candidate = me.getRootCause();
            }
        }

        for (ExceptionTranslator translator : this.exceptionTranslators) {
            if (translator.handles(candidateType)) {
                MessageException result = translator.translate(candidate);
                if (result != null) {
                	exceptionLogger.log(result);
                    throw result;
                }
            }
        }
        exceptionLogger.log(original);
        throw original;
    }

    public ExceptionLogger getExceptionLogger() {
		return exceptionLogger;
	}

	public void setExceptionLogger(ExceptionLogger exceptionLogger) {
		this.exceptionLogger = exceptionLogger;
	}

	/**
     * Returns the set of provided exception translators
     * 
     * @return the exception translators
     */
    public Set<ExceptionTranslator> getExceptionTranslators() {
        return this.exceptionTranslators;
    }

    /**
     * Sets the provided exception translators
     * 
     * @param translators the exception translators to set
     */
    public void setExceptionTranslators(Set<ExceptionTranslator> translators) {
        this.exceptionTranslators = translators;
    }

}
