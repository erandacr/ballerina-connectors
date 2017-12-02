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

package org.ballerinalang.test.service.jms.sample.consumer.tx;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.service.jms.sample.JMSServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.QueueConnection;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Testing the JMS consumer tx rollback
 */
public class JMSConsumerTxRollbackTestCase extends IntegrationTestCase {
    private static final Logger log = LoggerFactory.getLogger(JMSConsumerTxRollbackTestCase.class);
    ServerInstance ballerinaServer;
    private BrokerService broker = null;
    private String serverZipPath;

    private String queueName = "MyQueueTx";

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
        ballerinaServer = new JMSServerInstance(serverZipPath, 9091);
    }

    /**
     * Stops the started activemq broker and ballerina server.
     *
     * @throws Exception if stopping of any of the above fails
     */
    @AfterClass
    private void cleanup() throws Exception {
        ballerinaServer.stopServer();
        if (broker != null) {
            broker.stop();
        }
    }

    @Test(description = "Test JMS receiver local tx rollback operation")
    public void testJMSSendReceive() throws Exception {
        log.info("JMS test start..");

        publishMessagesToQueue(queueName);

        if (broker.checkQueueSize(queueName)) {
            Assert.fail("Unable to push message to the testing queue, failed in test preparation.");
        }

        //Adding temporary echo service so the server start can be monitored using that. (since this is a jms service
        //there won't be any http port openings, hence current logic cannot identify whether server is started or not)
        String relativePath = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms" + File.separator
                        + "tx" + File.separator + "jmsReceiverTxRollback.bal").getAbsolutePath();
        String[] receiverArgs = { relativePath };

        ballerinaServer.setArguments(receiverArgs);

        // Start receiver
        ballerinaServer.startServer();

        // wait until http backend invoked and rollbacked (few times until it moves to the dlc)
        Thread.sleep(2000);

        // check if the message is not there in the initial queue and its there in the dead letter queue
        Assert.assertTrue(broker.checkQueueSize(queueName) && !broker.checkQueueSize("ActiveMQ.DLQ"),
                "Queue is not empty message is not committed.");
    }

    /**
     * To publish the messages to a queue.
     *
     * @throws JMSException         JMS Exception.
     * @throws InterruptedException Interrupted exception while waiting in between messages.
     */
    public void publishMessagesToQueue(String queueName) throws JMSException {
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61618");
        QueueConnection queueConn = (QueueConnection) connectionFactory.createConnection();
        queueConn.start();
        QueueSession queueSession = queueConn.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination destination = queueSession.createQueue(queueName);
        MessageProducer queueSender = queueSession.createProducer(destination);
        queueSender.setDeliveryMode(DeliveryMode.PERSISTENT);
        String queueText = "Queue Message : " + "Bal Message";
        TextMessage queueMessage = queueSession.createTextMessage(queueText);
        queueSender.send(queueMessage);
        queueConn.close();
        queueSession.close();
        queueSender.close();
    }
}


