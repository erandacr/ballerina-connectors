import ballerina.net.jms;
import ballerina.net.http;


@jms:configuration {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61618",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueue",
    acknowledgementMode:"AUTO_ACKNOWLEDGE"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {
        //Process the message
        string msgType = m.getType();
        string stringPayload = m.getTextMessageContent();
        println("message type : " + msgType);
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
        println("=================================================== http =====================================");
        res.setStringPayload("hello world");
        res.send();
    }
}
