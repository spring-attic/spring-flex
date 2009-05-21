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

package org.springframework.flex.messaging.integration;

/**
 * Constants for the header names that are mapped from a Flex
 * Message to a Spring Integration Message.
 * 
 * @author Mark Fisher
 */
public abstract class FlexHeaders {

    public static final String MESSAGE_ID = "flex_message_id";

    public static final String CLIENT_ID = "flex_client_id";

    public static final String DESTINATION_ID = "flex_destination_id";

    public static final String TIMESTAMP = "flex_timestamp";

    public static final String TIME_TO_LIVE = "flex_time_to_live";

}
