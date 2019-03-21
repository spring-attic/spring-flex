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

package org.springframework.flex.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import flex.messaging.MessageException;

/**
 * Default implementation of {@link ExceptionLogger} that logs all {@link MessageException}s at WARN level with Commons Logging.
 *
 * @author Jeremy Grelle
 */
public class DefaultExceptionLogger implements ExceptionLogger {

	private static final Log log = LogFactory.getLog(DefaultExceptionLogger.class);
	
	public void log(Throwable throwable) {
		if (log.isWarnEnabled()) {
			log.warn("The following exception occurred during request processing by the BlazeDS MessageBroker and will be serialized back to the client: ", throwable);
		}
	}

}
