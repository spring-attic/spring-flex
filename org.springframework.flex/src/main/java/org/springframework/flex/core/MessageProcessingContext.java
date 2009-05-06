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

import java.util.HashMap;
import java.util.Map;

/**
 * Context holder for information about the current AMF message processing request.
 * 
 * @author Jeremy Grelle
 */
public class MessageProcessingContext {

    private final Object messageTarget;

    private final Map<String, Object> attributes = new HashMap<String, Object>();

    /**
     * Creates a new MessageProcessingContext with the current message target endpoint
     * 
     * @param messageTarget the intercepted target endpoint of the AMF message
     */
    public MessageProcessingContext(Object messageTarget) {
        this.messageTarget = messageTarget;
    }

    /**
     * Returns any stored attributes for the current request
     * 
     * @return the map of attributes
     */
    public Map<String, Object> getAttributes() {
        return this.attributes;
    }

    /**
     * Returns the intercepted target endpoint of the current message processing request
     * 
     * @return the message target endpoint
     */
    public Object getMessageTarget() {
        return this.messageTarget;
    }

}
