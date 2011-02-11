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

package org.springframework.flex.samples.integration;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Mark Fisher
 * @author Jeremy Grelle
 */
public class Counter {

    private volatile boolean running;

    private final Log log = LogFactory.getLog(Counter.class);

    private final AtomicInteger count = new AtomicInteger();

    public Integer next() {
        return this.running ? this.count.getAndIncrement() : null;
    }

    public void handle(String message) {
        if ("start".equalsIgnoreCase(message)) {
            this.running = true;
        } else if ("stop".equalsIgnoreCase(message)) {
            this.running = false;
        } else {
            try {
                this.count.set(Integer.parseInt(message));
            } catch (NumberFormatException e) {
                this.log.info("UNSUPPORTED FLEX MESSAGE RECEIVED: " + message);
            }
        }
    }

}
