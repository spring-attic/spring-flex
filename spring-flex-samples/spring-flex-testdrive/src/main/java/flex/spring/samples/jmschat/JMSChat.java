package flex.spring.samples.jmschat;

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
import javax.jms.Topic;
import javax.jms.Session;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMapMessage;

public class JMSChat implements MessageListener, ActionListener {

    private String url = "tcp://localhost:61616";
    private Connection connection;
    private Session session;    
    private Topic topic;
    private MessageProducer producer;

    private JTextField tfUser;
    private JTextField tfMessage;
    private JTextArea taChat;

    public static void main(String args[]) {
        new JMSChat();
    }

    public JMSChat() {

        try {
        	
            ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(url);
            connection = factory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            topic = session.createTopic("sampletopic.flex.jms.chat");

            MessageConsumer consumer = session.createConsumer(topic);
            consumer.setMessageListener(this);
            
            producer = session.createProducer(topic);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            connection.start();

        } catch (JMSException e) {
            e.printStackTrace();
        }

        // Build user interface
        JFrame frame = new JFrame("JMS Chat");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        taChat = new JTextArea();
        frame.getContentPane().add(taChat, BorderLayout.CENTER);

        Box north = new Box(BoxLayout.X_AXIS);
        north.add(new JLabel("User Name:"));
        tfUser = new JTextField();
        north.add(tfUser);
        frame.getContentPane().add(north, BorderLayout.NORTH);

        Box south = new Box(BoxLayout.X_AXIS);
        south.add(new JLabel("Message:"));
        tfMessage = new JTextField();
        south.add(tfMessage);
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
            String userId = (String) message.getString("userId");
            String msg = (String) message.getString("chatMessage");
            taChat.append(userId + ": " + msg + "\n");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent event) {
        try {
        	MapMessage message = session.createMapMessage();
            message.setString("userId", tfUser.getText());
            message.setString("chatMessage", tfMessage.getText());
            producer.send(message);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

}
