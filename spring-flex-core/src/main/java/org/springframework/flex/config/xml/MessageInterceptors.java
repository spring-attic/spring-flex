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

package org.springframework.flex.config.xml;

import org.springframework.flex.core.MessageInterceptor;

/**
 * Defines the default order of the framework-supplied {@link MessageInterceptor} implementations.
 *
 * @author Jeremy Grelle
 */
enum MessageInterceptors {

	FIRST,
    PER_CLIENT_AUTH_INTERCEPTOR,
    LOGIN_MESSAGE_INTERCEPTOR,
    ENDPOINT_INTERCEPTOR,
    LAST;
	
	private static final int INTERVAL = 100;
    private final int order;

    private MessageInterceptors() {
        order = ordinal() * INTERVAL;
    }

    public int getOrder() {
       return order;
    }
}
