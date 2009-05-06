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

import flex.messaging.messages.Message;

/**
 * Strategy interface for applying custom processing logic to incoming and outgoing AMF {@link Message}s
 * 
 * @author Jeremy Grelle
 */
public interface MessageInterceptor {

    /**
     * Hook for post-processing the outgoing AMF {@link Message}
     * 
     * @param context context for the current request
     * @param inputMessage the incoming AMF message
     * @param outputMessage the outgoing AMF message
     * @return the AMF message to send in response
     */
    public Message postProcess(MessageProcessingContext context, Message inputMessage, Message outputMessage);

    /**
     * Hook for pre-processing the incoming AMF {@link Message}
     * 
     * @param context context for the current request
     * @param inputMessage the incoming AMF message
     * @return the AMF message to process in the current request
     */
    public Message preProcess(MessageProcessingContext context, Message inputMessage);
}
