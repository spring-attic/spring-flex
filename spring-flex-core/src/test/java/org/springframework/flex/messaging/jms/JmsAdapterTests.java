package org.springframework.flex.messaging.jms;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQTopic;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.support.StaticApplicationContext;
import org.springframework.flex.core.AbstractMessageBrokerTests;

import flex.messaging.FlexContext;
import flex.messaging.MessageBroker;
import flex.messaging.MessageDestination;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.services.MessageService;


public class JmsAdapterTests extends AbstractMessageBrokerTests {

    private static final String DEST_ID = "testJmsAdapter";
    
    @Mock
    private ApplicationEventPublisher publisher;
    
    @Before
    public void setUp() throws Exception {
    	MockitoAnnotations.initMocks(this);
    }
    
    @After
    public void tearDown() throws Exception {
        getMessageService().removeDestination(DEST_ID);
    }

    @Test
    public void destinationSubscribeSendUnsubscribe() throws Exception {
        JmsAdapter adapter = createAdapter();
        
        FlexContext.setThreadLocalFlexClient(getMessageBroker().getFlexClientManager().getFlexClient("foo"));
        
        CommandMessage subscribeMessage = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage.setClientId("1234");
        subscribeMessage.setDestination(DEST_ID);
        adapter.manage(subscribeMessage);
        
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertTrue("MessageListener not running",adapter.getMessageListenerContainer().isRunning());
        
        AsyncMessage testMessage = new AsyncMessage();
        testMessage.setBody("test");
        adapter.invoke(testMessage);
        
        CommandMessage unsubscribeMessage = new CommandMessage(CommandMessage.UNSUBSCRIBE_OPERATION);
        unsubscribeMessage.setClientId("1234");
        unsubscribeMessage.setDestination(DEST_ID);
        adapter.manage(unsubscribeMessage);
        
        assertFalse("MessageListener not stopped",adapter.getMessageListenerContainer().isRunning());
        assertFalse("MessageListener should be shut down", adapter.getMessageListenerContainer().isActive());
    }

    @Test
    public void destinationSubscribeTwiceSendUnsubscribe() throws Exception {
        JmsAdapter adapter = createAdapter();
        
        CommandMessage subscribeMessage = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage.setClientId("1234");
        subscribeMessage.setDestination(DEST_ID);
        adapter.manage(subscribeMessage);
        
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertTrue("MessageListener not running",adapter.getMessageListenerContainer().isRunning());
        
        CommandMessage subscribeMessage2 = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage2.setClientId("5678");
        subscribeMessage2.setDestination(DEST_ID);
        adapter.manage(subscribeMessage2);
        
        AsyncMessage testMessage = new AsyncMessage();
        testMessage.setBody("test");
        adapter.invoke(testMessage);
        
        CommandMessage unsubscribeMessage = new CommandMessage(CommandMessage.UNSUBSCRIBE_OPERATION);
        unsubscribeMessage.setClientId("1234");
        unsubscribeMessage.setDestination(DEST_ID);
        adapter.manage(unsubscribeMessage);
        
        assertTrue("MessageListener stopped unexpectedly",adapter.getMessageListenerContainer().isRunning());
        assertTrue("MessageListener shut down unexpectedly", adapter.getMessageListenerContainer().isActive());
    }

    @Test
    public void subscribeUnsubscribeStop() throws Exception{
        
        JmsAdapter adapter = createAdapter();
        
        CommandMessage subscribeMessage = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage.setClientId("1234");
        subscribeMessage.setDestination(DEST_ID);
        adapter.manage(subscribeMessage);
        
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertTrue("MessageListener not running",adapter.getMessageListenerContainer().isRunning());
        
        CommandMessage unsubscribeMessage = new CommandMessage(CommandMessage.UNSUBSCRIBE_OPERATION);
        unsubscribeMessage.setClientId("1234");
        unsubscribeMessage.setDestination(DEST_ID);
        adapter.manage(unsubscribeMessage);
        
        assertFalse("MessageListener not stopped",adapter.getMessageListenerContainer().isRunning());
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
        
        adapter.stop();
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
    }

    @Test
    public void subscribeTwiceUnsubscribeStop() throws Exception{
        
        JmsAdapter adapter = createAdapter();
        
        CommandMessage subscribeMessage = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage.setClientId("1234");
        subscribeMessage.setDestination(DEST_ID);
        adapter.manage(subscribeMessage);
        
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertTrue("MessageListener not running",adapter.getMessageListenerContainer().isRunning());
        
        CommandMessage subscribeMessage2 = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage2.setClientId("5678");
        subscribeMessage2.setDestination(DEST_ID);
        adapter.manage(subscribeMessage2);
        
        CommandMessage unsubscribeMessage = new CommandMessage(CommandMessage.UNSUBSCRIBE_OPERATION);
        unsubscribeMessage.setClientId("1234");
        unsubscribeMessage.setDestination(DEST_ID);
        adapter.manage(unsubscribeMessage);
        
        assertTrue("MessageListener stopped unexpectedly",adapter.getMessageListenerContainer().isRunning());
        assertTrue("MessageListener shut down unexpectedly", adapter.getMessageListenerContainer().isActive());
        
        adapter.stop();
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
    }

    @Test
    public void subscribeStop() throws Exception{
        
        JmsAdapter adapter = createAdapter();
        
        CommandMessage subscribeMessage = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage.setClientId("1234");
        subscribeMessage.setDestination(DEST_ID);
        adapter.manage(subscribeMessage);
        
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertTrue("MessageListener not running",adapter.getMessageListenerContainer().isRunning());
        
        adapter.stop();
        assertFalse("MessageListener not stopped",adapter.getMessageListenerContainer().isRunning());
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
    }

    @Test
    public void stop() throws Exception{
        
        JmsAdapter adapter = createAdapter();
        
        adapter.stop();
        assertFalse("MessageListener not stopped",adapter.getMessageListenerContainer().isRunning());
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
    }

    @Test
    public void stopRestart() throws Exception {
        JmsAdapter adapter = createAdapter();        
        
        adapter.stop();
        assertFalse("MessageListener not stopped",adapter.getMessageListenerContainer().isRunning());
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
        
        adapter.start();
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertFalse("MessageListener running unexpectedly",adapter.getMessageListenerContainer().isRunning());
    }

    @Test
    public void subscribeStopRestart() throws Exception{
        JmsAdapter adapter = createAdapter();        
        
        CommandMessage subscribeMessage = new CommandMessage(CommandMessage.SUBSCRIBE_OPERATION);
        subscribeMessage.setClientId("1234");
        subscribeMessage.setDestination(DEST_ID);
        adapter.manage(subscribeMessage);
        
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertTrue("MessageListener not running",adapter.getMessageListenerContainer().isRunning());
        
        adapter.stop();
        assertFalse("MessageListener not stopped",adapter.getMessageListenerContainer().isRunning());
        assertFalse("MessageListener not shut down", adapter.getMessageListenerContainer().isActive());
        
        adapter.start();
        assertTrue("MessageListener not initialized", adapter.getMessageListenerContainer().isActive());
        assertFalse("MessageListener running unexpectedly",adapter.getMessageListenerContainer().isRunning());
    }
    
    private JmsAdapter createAdapter() throws Exception {
        String adapterBeanName = "test-jms-adapter";
        MutablePropertyValues properties = new MutablePropertyValues();
        ConnectionFactory cf = new ActiveMQConnectionFactory("vm:(broker:(tcp://localhost:61616)?persistent=false)?marshal=false");
        Destination dest = new ActiveMQTopic("test.topic");
        properties.addPropertyValue("connectionFactory", cf);
        properties.addPropertyValue("jmsDestination", dest);
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerPrototype(adapterBeanName, JmsAdapter.class, properties);

        JmsAdapter adapter = (JmsAdapter) context.getBean(adapterBeanName);
        MessageDestination destination = new MessageDestination();
        destination.setId(DEST_ID);
        destination.setService(getMessageService());
        adapter.setDestination(destination);
        adapter.setApplicationEventPublisher(publisher);
        adapter.start();
        return adapter;
    }
    
    private MessageService getMessageService() throws Exception {
        MessageBroker broker = getMessageBroker();
        return (MessageService) broker.getServiceByType(MessageService.class.getName());
    }
    
}
