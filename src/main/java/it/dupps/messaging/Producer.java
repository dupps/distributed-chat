package it.dupps.messaging;

import com.google.gson.Gson;
import it.dupps.communication.ComType;
import it.dupps.communication.Communication;
import it.dupps.network.Client;
import it.dupps.persistance.MessageFacade;
import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

/**
 * Created by dupps on 14.04.15.
 */
public class Producer {

    private Connection connection;
    private Session session;
    private MessageProducer producer;
    private MessageFacade messageFacade = new MessageFacade();

    public Producer(String brokerURL, String topicName) throws JMSException {
        ConnectionFactory factory = new ActiveMQConnectionFactory(brokerURL);
        connection = factory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(topicName);
        producer = session.createProducer(topic);
    }

    public void send(Client client, Communication com) throws JMSException {
        String username = client.getUsername();
        String text = com.getPayload();

        Communication comObj = new Communication(ComType.MESSAGE);
        comObj.setUsername(username);
        comObj.setPayload(text);
        String json = new Gson().toJson(comObj);
        Message message = session.createTextMessage(json);
        producer.send(message);

        messageFacade.persistMessage(text, username);
    }
}