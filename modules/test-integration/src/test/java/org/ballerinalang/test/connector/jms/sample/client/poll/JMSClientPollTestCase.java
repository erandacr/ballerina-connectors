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

import org.apache.activemq.broker.BrokerService;
import org.ballerinalang.test.IntegrationTestCase;
import org.ballerinalang.test.connector.jms.sample.JMSServerInstance;
import org.ballerinalang.test.connector.jms.sample.JMSTestUtils;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Testing the JMS Client connector polling testcase
 */
public class JMSClientPollTestCase extends IntegrationTestCase {
    private static final Logger log = LoggerFactory.getLogger(JMSClientPollTestCase.class);
    private BrokerService broker;
    private String serverZipPath;
    private String queueName = "MyPollingQueue";

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

    @Test(description = "Test fot JMS Client Connector Poll")
    public void testJMSPoll() throws Exception {
        log.info("JMS client connector poll test start..");

        JMSTestUtils.publishMessagesToQueue(queueName);
        Assert.assertTrue(!broker.checkQueueSize(queueName), "JMS Client Connector polling test preparation failed");

        ServerInstance jmsSender = new JMSServerInstance(serverZipPath);

        // Start sender
        String[] senderArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms"
                        + File.separator + "jmsClientPoll.bal").getAbsolutePath()
        };

        jmsSender.runMain(senderArgs);

        Thread.sleep(10000);

        Assert.assertTrue(broker.checkQueueSize(queueName), "JMS Client Connector polling failed");
    }
}
