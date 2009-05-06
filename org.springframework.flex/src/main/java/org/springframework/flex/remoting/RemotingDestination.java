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

package org.springframework.flex.remoting;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.flex.config.BeanIds;

import flex.messaging.MessageBroker;

/**
 * Indicates that an annotated Spring-managed bean should be exported as a destination for AMF-based remoting via a
 * Spring-managed {@link MessageBroker}.
 * 
 * @author Jeremy Grelle
 * 
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface RemotingDestination {

    /**
     * A array of ids for the BlazeDS channels over which this remoting destination should be exposed.
     * 
     * <p>
     * Only needed when overriding the default channels for the RemotingService.
     */
    String[] channels() default {};

    /**
     * The id of the Spring-managed MessageBroker that will route messages to the exported bean.
     * 
     * <p>
     * Not required unless an explicit id has been specified for the MessageBroker.
     */
    String messageBroker() default BeanIds.MESSAGE_BROKER;

    /**
     * A reference to a custom Spring-managed ServiceAdapter (usually created via a ManageableComponentFactoryBean) to
     * be used when invoking methods on this destination.
     */
    String serviceAdapter() default "";

    /**
     * The id of the remoting destination. This corresponds to the "destination" property that will be set on the
     * RemoteObject in the Flex client.
     * 
     * <p>
     * By default, this will be set to the name of the bean being exported.
     */
    String value() default "";
}
