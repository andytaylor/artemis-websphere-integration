package org.andy.mdb;


import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;

import org.jboss.ejb3.annotation.ResourceAdapter;

@ResourceAdapter("ArtemisRA")
@MessageDriven(name = "TestMDB", activationConfig = {
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "java:eis/inQueue")
})
@TransactionManagement(TransactionManagementType.CONTAINER)
public class TestMDB implements MessageListener {
	
	private static final Logger log = Logger.getLogger(TestMDB.class.getName());

	@Resource(mappedName="java:/eis/ArtemisConnectionFactory")
	private ConnectionFactory connectionFactory;

	@Resource(mappedName="java:eis/outQueue")
	private Queue queue;

	@Override
	public void onMessage(Message message) {
		log.info("received from inQueue " + message);
		try (
				Connection connection = connectionFactory.createConnection();
				Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
				MessageProducer producer = session.createProducer(queue);
		) {
			log.info("Sending text message to outQueue");
			producer.send(session.createTextMessage("This is a reply"));
		} catch (JMSException e) {
			log.info("error:" + e.getMessage());
			e.printStackTrace();
		}
	}

}
