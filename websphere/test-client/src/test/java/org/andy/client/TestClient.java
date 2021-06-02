package org.andy.client;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.InitialContext;

import org.junit.Test;

public class TestClient {

	private static final Logger log = Logger.getLogger(com.angelogalvao.samples.TestClient.class.getName());
	
	public final static int NUMBER_OF_MESSAGES = 100;

	@Test
	public void sendMessages() throws Exception {
		Connection connection         = null;
		InitialContext initialContext = null;
		
		try {
			initialContext = new InitialContext();
			
			Queue inQueue = (Queue) initialContext.lookup("queue/inQueue");

			Queue outQueue = (Queue) initialContext.lookup("queue/outQueue");
			
			ConnectionFactory cf = (ConnectionFactory) initialContext.lookup("ConnectionFactory");
			
			connection = cf.createConnection();
			
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

			CountDownLatch latch = new CountDownLatch(NUMBER_OF_MESSAGES);

			MessageListener messageListener = new MessageListener() {
				@Override
				public void onMessage(Message message) {
					TextMessage textMessage = (TextMessage) message;
					try {
						log.info("TestSendMessageToAMQ.onMessage received " + textMessage.getText());
					} catch (JMSException e) {
						e.printStackTrace();
					}
					latch.countDown();
				}
			};

			MessageConsumer consumer = session.createConsumer(outQueue);

			consumer.setMessageListener(messageListener);

			MessageProducer producer = session.createProducer(inQueue);

			connection.start();

			for (int i = 0; i < NUMBER_OF_MESSAGES; i++) {
				
				String text = "Message from AMQ broker #"+(i+1);

				TextMessage message = session.createTextMessage(text);

				log.info("Sent message: " + text);

				producer.send(message);
			}
			latch.await(10, TimeUnit.SECONDS);
		} finally {
			// Step 12. Be sure to close our JMS resources!
			if (initialContext != null) {
				initialContext.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
	}
}
