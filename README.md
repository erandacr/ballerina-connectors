This is a Integration test project to develop integration tests for Ballerina based connectors.

* How to update Ballerina runtime version

i. Updated parent version in the root pom

ii. Update **ballerina.runtime.version** property in the root pom
    
* How to add a new Ballerina connector to the Ballerina Connector Integration Test module
   
 i. Include connector dependency inside dependencyManagement tag in the root pom.xml as follows,
    
    <dependency>
        <groupId>org.ballerinalang</groupId>
        <artifactId>ballerina-jms</artifactId>
        <version>${jms.connector.version}</version>
        <type>zip</type>
    </dependency>
    
   ii. Provide the connector version as a property in the root pom as follows,
    
    <jms.connector.version>0.95.0</jms.connector.version>
    
   iii. Include connector dependency inside dependencies tag in the modules/distribution/pom.xml as follows,
    
    <dependency>
        <groupId>org.ballerinalang</groupId>
        <artifactId>ballerina-jms</artifactId>
        <type>zip</type>
    </dependency>
    
   iv. Add you new connectors test cases into **modules/test-integration** module