import ballerina.net.jms;
import ballerina.net.http;


@jms:configuration {
    initialContextFactory:"org.apache.activemq.jndi.ActiveMQInitialContextFactory",
    providerUrl:"tcp://localhost:61618?jms.redeliveryPolicy.maximumRedeliveries=0",
    connectionFactoryType:"queue",
    connectionFactoryName:"QueueConnectionFactory",
    destination:"MyQueueTx",
    acknowledgementMode:"SESSION_TRANSACTED"
}
service<jms> jmsService {
    resource onMessage (jms:JMSMessage m) {
        endpoint<http:HttpClient> httpConnector {
             create http:HttpClient ("http://non.existing.ep",{});
        }

        //Process the message
        string msgType = m.getType();
        string stringPayload = m.getTextMessageContent();
        println(stringPayload);

        http:Request req = {};

        // Retrieve the string payload using native function and set as a json payload.
        req.setStringPayload(m.getTextMessageContent());

        var resp, e = httpConnector.get("/echo", req);
        println("POST response: " + resp.getStringPayload());

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
        _=res.send();
    }
}

