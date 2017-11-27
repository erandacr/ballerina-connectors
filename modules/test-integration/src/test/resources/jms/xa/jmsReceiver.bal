import ballerina.net.jms;
import ballerina.net.http;


@jms:configuration {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61618",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue3",
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
}
service<jms> jmsService1 {
    resource onMessage (jms:JMSMessage m) {
        //Process the message
        string stringPayload = m.getTextMessageContent();
        println(stringPayload);
    }
}

@jms:configuration {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61618",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue4",
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
}
service<jms> jmsService2 {
    resource onMessage (jms:JMSMessage m) {
        //Process the message
        string stringPayload = m.getTextMessageContent();
        println(stringPayload);
    }
}



@http:configuration {
    basePath:"/echo"
}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Request req, http:Response res) {
        res.setStringPayload("hello world");
        res.send();
    }
}
