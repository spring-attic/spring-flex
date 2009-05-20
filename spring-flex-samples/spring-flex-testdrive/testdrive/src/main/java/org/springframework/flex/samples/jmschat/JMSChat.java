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

package org.springframework.flex.samples.jmschat;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.Topic;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;

/**
 * 
 * @author Christophe Coenraets
 * @author Jeremy Grelle
 */
public class JMSChat implements MessageListener, ActionListener {

    private final String url = "tcp://localhost:61616";

    private Connection connection;

    private Session session;

    private Topic topic;

    private MessageProducer producer;

    private final JTextField tfUser;

    private final JTextField tfMessage;

    private final JTextArea taChat;

    public static void main(String args[]) {
        new JMSChat();
    }

    public JMSChat() {

        try {

            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(this.url);
            this.connection = factory.createConnection();
            this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.topic = this.session.createTopic("sampletopic.flex.jms.chat");

            MessageConsumer consumer = this.session.createConsumer(this.topic);
            consumer.setMessageListener(this);

            this.producer = this.session.createProducer(this.topic);
            this.producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            this.connection.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }

        // Build user interface
        JFrame frame = new JFrame("JMS Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.taChat = new JTextArea();
        frame.getContentPane().add(this.taChat, BorderLayout.CENTER);

        Box north = new Box(BoxLayout.X_AXIS);
        north.add(new JLabel("User Name:"));
        this.tfUser = new JTextField();
        north.add(this.tfUser);
        frame.getContentPane().add(north, BorderLayout.NORTH);

        Box south = new Box(BoxLayout.X_AXIS);
        south.add(new JLabel("Message:"));
        this.tfMessage = new JTextField();
        south.add(this.tfMessage);
        JButton btSend = new JButton("Send");
        btSend.addActionListener(this);
        south.add(btSend);
        frame.getContentPane().add(south, BorderLayout.SOUTH);

        int width = 300;
        int height = 300;
        frame.setSize(width, height);
        frame.setVisible(true);

    }

    public void onMessage(Message object) {
        try {
            ActiveMQMapMessage message = (ActiveMQMapMessage) object;
            String userId = message.getString("userId");
            String msg = message.getString("chatMessage");
            this.taChat.append(userId + ": " + msg + "\n");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent event) {
        try {
            MapMessage message = this.session.createMapMessage();
            message.setString("userId", this.tfUser.getText());
            message.setString("chatMessage", this.tfMessage.getText());
            this.producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
