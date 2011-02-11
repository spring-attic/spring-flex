package org.springframework.flex.samples.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.flex.core.ExceptionTranslator;

import flex.messaging.MessageException;


public class LoggingExceptionTranslator implements ExceptionTranslator {

    private static final Log log = LogFactory.getLog(LoggingExceptionTranslator.class);
    
    public boolean handles(Class<?> clazz) {
        return true;
    }

    public MessageException translate(Throwable t) {
        log.error("Uncaught Exception thrown: ", t);
        return null;
    }

}
