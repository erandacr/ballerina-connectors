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

package org.ballerinalang.test.connector.jms.sample.client.send.xa;

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

/**
 * Testing the JMS XA publisher rollback
 */
public class JMSPublisherXaRollbackTestCase extends IntegrationTestCase {
    private static final Logger log = LoggerFactory.getLogger(JMSPublisherXaRollbackTestCase.class);
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

    @Test(description = "Test fot JMS XA Transacted failing publisher")
    public void testJMSSend() throws Exception {
        log.info("JMS xa negative test start..");

        ServerInstance jmsSender = new JMSServerInstance(serverZipPath);
        // Start sender
        String[] senderArgs = {
                new File("src" + File.separator + "test" + File.separator + "resources" + File.separator + "jms"
                        + File.separator + "xa" + File.separator + "jmsFailSender.bal").getAbsolutePath()
        };

        jmsSender.runMain(senderArgs);

        Thread.sleep(2000);

        Assert.assertTrue(broker.checkQueueSize("xaQueue1") && broker.checkQueueSize("xaQueue2"),
                "XA transaction rollback is failed");
    }
}
