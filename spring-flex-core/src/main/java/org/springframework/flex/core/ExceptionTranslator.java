/*
 * Copyright 2002-2009 the original author or authors.
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

import flex.messaging.MessageException;

/**
 * Strategy interface that allows for translation of known exception types into BlazeDS MessageExceptions that will
 * propagate proper AMF error message back to the client.
 * 
 * <p>
 * Implementations are encouraged to set the code of the created MessageException to something more useful than the
 * generic "Server.Processing" so that the client may reason on the code for custom fault handling logic.
 * 
 * @author Jeremy Grelle
 * 
 */
public interface ExceptionTranslator {

    /**
     * Checks if the translator can handle the specified exception class
     * 
     * @param clazz the class of the exception
     * @return true if the exception type can be handled
     */
    boolean handles(Class<?> clazz);

    /**
     * Translate the specified exception into an appropriate {@link MessageException}
     * 
     * @param t the original exception
     * @return the translated exception
     */
    MessageException translate(Throwable t);
}
