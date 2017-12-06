package org.ballerinalang.test.connector.jms.sample;
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

import org.ballerinalang.test.context.BallerinaTestException;
import org.ballerinalang.test.context.Constant;
import org.ballerinalang.test.context.ServerInstance;
import org.ballerinalang.test.context.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * The Ballerina server instance which is specifically configured to run jms tests by copying activemq client libraries.
 */
public class JMSServerInstance extends ServerInstance {

    public JMSServerInstance(String serverDistributionPath) throws BallerinaTestException {
        super(serverDistributionPath);
    }

    public JMSServerInstance(String serverDistributionPath, int serverHttpPort) throws BallerinaTestException {
        super(serverDistributionPath, serverHttpPort);
    }

    /**
     * Copies activemq client libraries to ballerina libraries to run jms client functions.
     *
     * @throws BallerinaTestException if preparing the server fails
     */
    @Override
    protected void configServer() throws BallerinaTestException {
        super.configServer();

        // Copy JMS libraries to the ballerina lib for testing

        // Source jar
        Path source = Paths
                .get(System.getProperty(Constant.PROJECT_BUILD_DIR), System.getProperty(Constant.ACTIVEMQ_ALL_JAR));

        // Target lib folder

        Path target = Paths.get(getServerHome(), "bre/lib", System.getProperty(Constant.ACTIVEMQ_ALL_JAR));

        Utils.copyFile(source, target);
    }
}
