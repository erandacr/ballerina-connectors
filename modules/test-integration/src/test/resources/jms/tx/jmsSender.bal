import ballerina.net.jms;

function main (string[] args) {
    _ = jmsSender();
}

function jmsSender () (boolean) {
    endpoint<jms:JmsClient> jmsEP {create jms:JmsClient(getConnectorConfig());}

    jms:JMSMessage queueMessage1 = jms:createTextMessage(getConnectorConfig());
    queueMessage1.setTextMessageContent("Hello from JMS Tx 1");
    jms:JMSMessage queueMessage2 = jms:createTextMessage(getConnectorConfig());
    queueMessage2.setTextMessageContent("Hello from JMS Tx 2");
    transaction {
        jmsEP.send("MyQueue1", queueMessage1);
        jmsEP.send("MyQueue2", queueMessage2);
    }
    return true;
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                                          providerUrl:"tcp://localhost:61618",
                                          connectionFactoryName:"QueueConnectionFactory",
                                          connectionFactoryType:"queue",
                                          acknowledgementMode:"SESSION_TRANSACTED"};
    return properties;
}


