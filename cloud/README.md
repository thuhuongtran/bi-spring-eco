### Spring Cloud
#### Spring Cloud Stream with RabbitMQ
Spring Cloud Stream is a framework built on top of Spring Boot and Spring Integration that helps in creating event-driven or message-driven microservices.  
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
    <version>3.1.3</version>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-test-support</artifactId>
    <version>3.1.3</version>
    <scope>test</scope>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-rabbit</artifactId>
    <version>3.2.2</version>
</dependency>
```
Let's look at a simple service in Spring Cloud Stream that listens to input binding and sends a response to the output binding:
```java
@SpringBootApplication
@EnableBinding(Processor.class)
public class MyLoggerServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyLoggerServiceApplication.class, args);
    }

    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public LogMessage enrichLogMessage(LogMessage log) {
        return new LogMessage(String.format("[1]: %s", log.getMessage()));
    }
}
```
`StreamListeners` — message-handling methods in beans that will be automatically invoked on a message from the channel after the MessageConverter does the serialization/deserialization between middleware-specific events and domain object types / POJOs

Messages designated to destinations are delivered by the Publish-Subscribe messaging pattern. Publishers categorize messages into topics, each identified by a name. Subscribers express interest in one or more topics. The middleware filters the messages, delivering those of the interesting topics to the subscribers.

Now, the subscribers could be grouped. A consumer group is a set of subscribers or consumers, identified by a group id, within which messages from a topic or topic's partition are delivered in a load-balanced manner.

##### Test
```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MyLoggerServiceApplication.class)
@DirtiesContext
public class MyLoggerApplicationTests {

    @Autowired
    private Processor pipe;

    @Autowired
    private MessageCollector messageCollector;

    @Test
    public void whenSendMessage_thenResponseShouldUpdateText() {
        pipe.input()
          .send(MessageBuilder.withPayload(new LogMessage("This is my message"))
          .build());

        Object payload = messageCollector.forChannel(pipe.output())
          .poll()
          .getPayload();

        assertEquals("[1]: This is my message", payload.toString());
    }
}
```
##### Custom channel
```java
public interface MyProcessor {
    String INPUT = "myInput";

    @Input
    SubscribableChannel myInput();

    @Output("myOutput")
    MessageChannel anOutput();

    @Output
    MessageChannel anotherOutput();
}
```
Otherwise, Spring will use the method names as the channel names. Therefore, we've got three channels called myInput, myOutput, and anotherOutput.
```java
@Autowired
private MyProcessor processor;

@StreamListener(MyProcessor.INPUT)
public void routeValues(Integer val) {
    if (val < 10) {
        processor.anOutput().send(message(val));
    } else {
        processor.anotherOutput().send(message(val));
    }
}

private static final <T> Message<T> message(T val) {
    return MessageBuilder.withPayload(val).build();
}

@StreamListener(
        target = MyProcessor.INPUT,
        condition = "payload < 10")
public void routeValuesToAnOutput(Integer val) {
    processor.anOutput().send(message(val));
}

@StreamListener(
        target = MyProcessor.INPUT,
        condition = "payload >= 10")
public void routeValuesToAnotherOutput(Integer val) {
    processor.anotherOutput().send(message(val));
}
```
##### RabbitMQ Configuration
```yaml
spring:
  cloud:
    stream:
      bindings:
        input:
          destination: queue.log.messages
          binder: local_rabbit
        output:
          destination: queue.pretty.log.messages
          binder: local_rabbit
      binders:
        local_rabbit:
          type: rabbit
          environment:
            spring:
              rabbitmq:
                host: <host>
                port: 5672
                username: <username>
                password: <password>
                virtual-host: /
```
`spring.cloud.stream.instanceCount` — number of running applications
`spring.cloud.stream.instanceIndex` — index of the current application