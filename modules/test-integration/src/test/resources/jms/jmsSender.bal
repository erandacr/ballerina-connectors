import ballerina.net.jms;

function main (string[] args) {
    _ = jmsSender();
}

function jmsSender () (boolean) {

    endpoint<jms:JmsClient> jmsEP {create jms:JmsClient (getConnectorConfig());}

    jms:JMSMessage queueMessage = jms:createTextMessage(getConnectorConfig());
    queueMessage.setTextMessageContent("Hello from JMS");
    jmsEP.send("MyQueue", queueMessage);
    return true;
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                                          providerUrl:"tcp://localhost:61618",
                                          connectionFactoryName:"QueueConnectionFactory",
                                          connectionFactoryType:"queue"};
    return properties;
}
