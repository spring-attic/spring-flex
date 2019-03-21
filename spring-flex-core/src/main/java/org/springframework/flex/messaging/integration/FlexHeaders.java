/*
 * Copyright 2002-2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   https://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.flex.messaging.integration;

import java.util.ArrayList;
import java.util.List;

/**
 * Constants for the header names that are mapped from a Flex
 * Message to a Spring Integration Message.
 * 
 * @author Mark Fisher
 */
public abstract class FlexHeaders {

    public static final String MESSAGE_CLIENT_ID = "flex_message_client_id";

    public static final String DESTINATION_ID = "flex_destination_id";
    
    public static final String MESSAGE_ID = "flex_id";
    
    public static final String TIMESTAMP = "flex_timestamp";
    
    public static final String FLEX_CLIENT_ID = "flex_client_id";
    
    
    
    private static final List<String> ignoredHeaders;
    
    static {
    	ignoredHeaders = new ArrayList<String>();
    	ignoredHeaders.add(MESSAGE_ID);
    	ignoredHeaders.add(TIMESTAMP);
    	ignoredHeaders.add(MESSAGE_CLIENT_ID);
    	ignoredHeaders.add(DESTINATION_ID);
    }
    
    /**
     * Returns the list of headers that are set explicitly to properties of AsyncMessage and shouldn't be added to it's header map.
     * @return a list of headers to ignore when setting AsyncMessage headers
     */
    public static List<String> ignored() {
    	return ignoredHeaders;
    }
}
