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

package org.springframework.flex.hibernate3.core;


import org.springframework.flex.config.MessageBrokerConfigProcessor;

import flex.messaging.MessageBroker;
import flex.messaging.services.RemotingService;

public class MessageBrokerFactoryBeanTests extends AbstractMessageBrokerTests {

    private boolean testErrorCase = false;
    
    public void testBrokerInitialization() throws Exception {

        MessageBroker broker = getMessageBroker();

        assertNotNull("MessageBroker was not created.", broker);
        RemotingService remotingService = (RemotingService) broker.getService("remoting-service");
        assertNotNull("remoting-service not found", remotingService);
        assertTrue("The remoting service was not started", remotingService.isStarted());
    }
    
    public void testBrokerInitialization_InvalidConfigFileFailure() throws Exception {
        
        setDirty();
        testErrorCase = true;
        
        MessageBroker broker;
        
        try {
            broker = getMessageBroker();
        } catch (Exception ex) {
            //Expected - continue
        }
        
        testErrorCase = false;
        
        broker = createMessageBroker();
        
        assertNotNull("MessageBroker was not created.", broker);        
    }
    
    public void testBrokerInitialization_ConfigProcessorErrorBeforeStartup() throws Exception {
        
        setDirty();
        
        addStartupProcessor(new BeforeStartupErrorThrowingProcessor());
        
        MessageBroker broker;
        
        try {
            broker = getMessageBroker();
        } catch (Exception ex) {
            ex.printStackTrace();
            //Expected - continue
        }
       
        clearProcessors();
        
        broker = createMessageBroker();
        
        assertNotNull("MessageBroker was not created.", broker);       
    }
    
    public void testBrokerInitialization_ConfigProcessorErrorAfterStartup() throws Exception {
        
        setDirty();
        
        addStartupProcessor(new AfterStartupErrorThrowingProcessor());
        
        MessageBroker broker;
        
        try {
            broker = getMessageBroker();
        } catch (Exception ex) {
            ex.printStackTrace();
            //Expected - continue
        }
       
        clearProcessors();
        
        broker = createMessageBroker();
        
        assertNotNull("MessageBroker was not created.", broker);       
    }

    @Override
    protected String getServicesConfigPath() {
        if (testErrorCase) {
            return "fubar";
        } else {
            return super.getServicesConfigPath();
        }
    }
    
    public static final class BeforeStartupErrorThrowingProcessor implements MessageBrokerConfigProcessor {

        public MessageBroker processAfterStartup(MessageBroker broker) {
            return broker;
        }

        public MessageBroker processBeforeStartup(MessageBroker broker) {
            throw new RuntimeException("failure before startup");
        }        
    }
    
    public static final class AfterStartupErrorThrowingProcessor implements MessageBrokerConfigProcessor {

        public MessageBroker processAfterStartup(MessageBroker broker) {
            throw new RuntimeException("failure before startup");
        }

        public MessageBroker processBeforeStartup(MessageBroker broker) {
            return broker;
        }        
    }

}
