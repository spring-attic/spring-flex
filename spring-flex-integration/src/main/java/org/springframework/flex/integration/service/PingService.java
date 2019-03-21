/*
 * Copyright 2002-2009 the original author or authors.
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

package org.springframework.flex.integration.service;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.flex.messaging.MessageTemplate;

public class PingService implements InitializingBean, Ping {

    private MessageTemplate template;

    private TaskExecutor taskExecutor;

    public void afterPropertiesSet() throws Exception {
        System.out.println("_____________________INITIALIZING PING SERVICE______________________");
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.flex.integration.service.Ping#fireEvent()
     */
    public void fireEvent() {
        this.taskExecutor.execute(new EventPublisher());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.flex.integration.service.Ping#foo()
     */
    public String foo() {
        return "bar";
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.flex.integration.service.Ping#ping()
     */
    public String ping() {
        return "pong";
    }

    public void setTaskExecutor(TaskExecutor taskExecutor) {
        this.taskExecutor = taskExecutor;
    }

    public void setTemplate(MessageTemplate template) {
        this.template = template;
    }

    private final class EventPublisher implements Runnable {

        public void run() {
            PingService.this.template.send("event-bus", "fired");
        }
    }

}
