/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.connector.jms.sample.client.poll;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.connector.jms.sample.JMSServerInstance;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Testing the JMS Client connector polling with selector testcase
 */
public class JMSClientSelectorPollTestCase extends IntegrationTestCase {
    private static final Logger log = LoggerFactory.getLogger(JMSClientSelectorPollTestCase.class);
    private BrokerService broker;
    private String serverZipPath;
    private String queueName = "MyPollingQueue";
    private String queueName2 = "MyPollingQueue2";

    /**
     * Setup an embedded activemq broker and prepare the ballerina distribution to run jms samples.
     *
     * @throws Exception if setting up fails
     */
    @BeforeClass
    private void setup() throws Exception {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.addConnector("tcp://localhost:61618");

        broker.start();

        serverZipPath = System.getProperty(Constant.SYSTEM_PROP_SERVER_ZIP);
    }

    /**
     * Stops the started activemq broker and ballerina server.
     *
     * @throws Exception if stopping of any of the above fails
     */
    @AfterClass
    private void cleanup() throws Exception {
        if (broker != null) {
            broker.stop();
        }
    }

    @Test(description = "Test for JMS Client Connector Poll with Selector matching correlation Id")
    public void testJMSPoll() throws Exception {
        log.info("JMS client connector poll test start..");

        String correlationId =  "abc12345";

        publishMessagesToQueue(queueName, correlationId);
        Assert.assertTrue(!broker.checkQueueSize(queueName),
                "JMS Client Connector polling with selector test preparation failed");

        ServerInstance jmsSender = new JMSServerInstance(serverZipPath);

        // Start sender
        String[] senderArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms"
                        + File.separator + "jmsClientSelectorPoll.bal").getAbsolutePath()
        };

        jmsSender.runMain(senderArgs);

        Thread.sleep(10000);

        Assert.assertTrue(broker.checkQueueSize(queueName), "JMS Client Connector polling with selector failed");
    }

   @Test(description = "Test for JMS Client Connector Poll with Selector non-matching correlation Id")
    public void testJMSPollNotMatchingId() throws Exception {
        log.info("JMS client connector poll test start..");

        String correlationId =  "abc12345xyz";

        publishMessagesToQueue(queueName2, correlationId);
        Assert.assertTrue(!broker.checkQueueSize(queueName2),
                "JMS Client Connector polling with selector test preparation failed");

        ServerInstance jmsSender = new JMSServerInstance(serverZipPath);

        // Start sender
        String[] senderArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms"
                        + File.separator + "jmsClientSelectorPoll.bal").getAbsolutePath()
        };

        jmsSender.runMain(senderArgs);

        Thread.sleep(10000);

        Assert.assertTrue(!broker.checkQueueSize(queueName2), "JMS Client Connector polling with selector failed");
    }



    /**
     * To publish the messages to a queue with the given correlationID.
     *
     * @throws JMSException         JMS Exception.
     * @throws InterruptedException Interrupted exception while waiting in between messages.
     */
    public static void publishMessagesToQueue(String queueName, String correlationID) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61618");
        Connection connection = null;
        Session session = null;
        MessageProducer producer = null;
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            Destination destination = session.createQueue(queueName);
            producer = session.createProducer(destination);

            producer.setDeliveryMode(DeliveryMode.PERSISTENT);
            String queueText = "Queue Message : " + "Bal Message";
            TextMessage queueMessage = session.createTextMessage(queueText);
            queueMessage.setJMSCorrelationID(correlationID);
            producer.send(queueMessage);
        } catch (JMSException e) {
            throw e;
        } finally {
            if (producer != null) {
                producer.close();
            }
            if (session != null) {
                session.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
