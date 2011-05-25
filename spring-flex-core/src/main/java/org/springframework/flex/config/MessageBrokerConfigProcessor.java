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

package org.springframework.flex.config;

import flex.messaging.MessageBroker;

/**
 * Factory hook that allows for custom configuration of a {@link MessageBroker} when created by a
 * {@link MessageBrokerFactoryBean}.
 * 
 * @author Jeremy Grelle
 * @see org.springframework.flex.core.MessageBrokerFactoryBean
 */
public interface MessageBrokerConfigProcessor {

    /**
     * Apply this config processor to the newly created {@link MessageBroker} after it and all of its services have been
     * started but before it is available for use.
     * 
     * @param broker the started {@link MessageBroker} instance
     * @return the modified {@link MessageBroker}
     */
    public MessageBroker processAfterStartup(MessageBroker broker);

    /**
     * Apply this config processor to the newly created {@link MessageBroker} after its intial configuration settings
     * have been parsed from the BlazeDS XML configuration, but before it has actually been started.
     * 
     * @param broker the new {@link MessageBroker} instance
     * @return the modified {@link MessageBroker}
     */
    public MessageBroker processBeforeStartup(MessageBroker broker);
}
