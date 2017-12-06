import ballerina.net.jms;

function main (string[] args) {
    jmsClientConsume();
}

function jmsClientConsume() {
    endpoint<jms:JmsClient> jmsEP {
        create jms:JmsClient (getConnectorConfig());
    }

    // Poll message from message broker
    jms:JMSMessage message = jmsEP.poll("MyPollingQueue", 1000);
    if (message != null) {
        println(message.getTextMessageContent());
    }
}

function getConnectorConfig () (jms:ClientProperties) {
    jms:ClientProperties properties = {initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
                                          providerUrl:"tcp://localhost:61618",
                                          connectionFactoryName:"QueueConnectionFactory",
                                          connectionFactoryType:"queue"};
    return properties;
}


