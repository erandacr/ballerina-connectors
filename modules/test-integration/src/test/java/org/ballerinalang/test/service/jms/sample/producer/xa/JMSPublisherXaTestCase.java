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

package org.ballerinalang.test.service.jms.sample.producer.xa;

import org.apache.activemq.broker.BrokerService;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.LogLeecher;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.service.jms.sample.JMSServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Testing the JMS XA publisher
 */
public class JMSPublisherXaTestCase extends IntegrationTestCase {
    private static final Logger log = LoggerFactory.getLogger(JMSPublisherXaTestCase.class);
    ServerInstance ballerinaServer =  null;
    private BrokerService broker;
    private String serverZipPath;

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

    @Test(description = "Test for JMS XA publisher",
          enabled = true)
    public void testJMSSendReceive() throws Exception {
        log.info("JMS XA test start..");

        //Adding temporary echo service so the server start can be monitored using that. (since this is a jms service
        //there won't be any http port openings, hence current logic cannot identify whether server is started or not)
        String relativePath = new File(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms" + File.separator
                        + "xa" + File.separator + "jmsReceiver.bal").getAbsolutePath();
        String[] receiverArgs = { relativePath };

        ballerinaServer.setArguments(receiverArgs);

        // Start receiver
        ballerinaServer.startServer();

        // leecher 1
        String messageText1 = "Hello from JMS XA 1";

        LogLeecher leecher1 = new LogLeecher(messageText1);

        ballerinaServer.addLogLeecher(leecher1);

        // leecher 2
        String messageText2 = "Hello from JMS XA 2";

        LogLeecher leecher2 = new LogLeecher(messageText2);

        ballerinaServer.addLogLeecher(leecher2);

        ServerInstance jmsSender = new JMSServerInstance(serverZipPath);
        // Start sender
        String[] senderArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms"
                        + File.separator + "xa" + File.separator + "jmsSender.bal").getAbsolutePath()
        };

        jmsSender.runMain(senderArgs);

        // Wait for expected text
        leecher1.waitForText(5000);
        leecher2.waitForText(5000);
    }
}
