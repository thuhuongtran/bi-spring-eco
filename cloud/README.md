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
#### Spring Cloud Task
Support for running tasks or jobs
```java
 <dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-task-dependencies</artifactId>
    <version>2.2.3.RELEASE</version>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-task</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-task-core</artifactId>
</dependency>
```
Add `@EnableTask` annotation: The annotation brings SimpleTaskConfiguration class in the picture which in turns registers the TaskRepository and its infrastructure.
```java
@EnableTask
public class TaskDemo {
    // ...
}
```
##### Configuring a DataSource for TaskRepository
The in-memory map to store the TaskRepository will vanish once the task ends and we'll lose data related to Task events. To store in a permanent storage, we're going to use MySQL as a data source with Spring Data JPA.
```yaml
spring:
  application:
    name: helloWorld
  datasource:
    url: jdbc:mysql://localhost:3306/springcloud?useSSL=false
    username: root
    password:
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL5Dialect
  batch:
    initialize-schema: always
```
To use the provided data source as an storage of `TaskRepository`, we create a class that extends `DefaultTaskConfigurer`.
```java
public class HelloWorldTaskConfigurer extends DefaultTaskConfigurer{
    public HelloWorldTaskConfigurer(DataSource dataSource){
        super(dataSource);
    }
}
```
```java
@Autowired
private DataSource dataSource;

@Bean
public HelloWorldTaskConfigurer getTaskConfigurer() {
    return new HelloWorldTaskConfigurer(dataSource);
}
```
##### Runner
```java
@Component
public static class HelloWorldApplicationRunner 
  implements ApplicationRunner {
 
    @Override
    public void run(ApplicationArguments arg0) throws Exception {
        System.out.println("Hello World from Spring Cloud Task!");
    }
}
```
##### Task life-cycle
During the task life-cycle, we can register listeners available from TaskExecutionListener interface. We need a class implementing the interface having three methods – onTaskEnd, onTaksFailed and onTaskStartup triggered in respective events of the Task.
```java
public class TaskListener implements TaskExecutionListener {
    @Override
    public void onTaskStartup(TaskExecution taskExecution) {

    }

    @Override
    public void onTaskEnd(TaskExecution taskExecution) {

    }

    @Override
    public void onTaskFailed(TaskExecution taskExecution, Throwable throwable) {

    }
}
```
```java
@Bean
public TaskListener taskListener() {
    return new TaskListener();
}
```
##### Run a batch of jobs
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-batch</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-task-batch</artifactId>
</dependency>
```
```java
@EnableBatchProcessing
public class TaskDemo {
}
```
If we run the application, the @EnableBatchProcessing annotation will trigger the Spring Batch Job execution and Spring Cloud Task will log the events of the executions of all batch jobs with the other Task executed in the springcloud database.

```java

@Configuration
public class JobConfiguration {
    @Autowired
    private JobBuilderFactory jobBuilderFactory;
    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Bean
    public Step step1() {
        return this.stepBuilderFactory.get("job1step1")
                .tasklet((contribution, chunkContext) -> RepeatStatus.FINISHED).build();
    }

    @Bean
    public Step step2() {
        return this.stepBuilderFactory..
    }

    @Bean
    public Job job1() {
        return this.jobBuilderFactory.get("job1")
                .start(step1())
                .next(step2())
                .build();
    }
```
##### Task from Stream
```java
@EnableTaskLauncher
public class StreamTaskSinkApplication {
```
We can trigger Tasks from Spring Cloud Stream, the @EnableTaskLaucnher annotation, then a TaskSink will be available.

The TaskSink receives the message from a stream that contains a GenericMessage containing TaskLaunchRequest as a payload. Then it triggers a Task-based on co-ordinate provided in the Task launch request.

```java
@EnableTaskLauncher
public class StreamTaskSinkApplication {
```
```java
@Configuration
public class TaskSinkConfiguration {
    @Bean
    public TaskLauncher taskLauncher() {
        return mock(TaskLauncher.class);
    }
}
```
```java
public class TaskSinkTest {
    @Autowired
    private Sink sink;
}

TaskLaunchRequest request = new TaskLaunchRequest("maven://org.springframework.cloud.task.app:"
                + "timestamp-task:jar:1.0.1.RELEASE", null, prop, null, null);
GenericMessage<TaskLaunchRequest> message = new GenericMessage<>(request);
this.sink.input().send(message);
```
Now, we create an instance of TaskLaunchRequest and send that as a payload of GenericMessage<TaskLaunchRequest> object. Then we can invoke the input channel of the Sink keeping the GenericMessage object in the channel.
#### Spring Cloud CLI
Spring Boot Cloud CLI, the tool provides a set of command line enhancements to the Spring Boot CLI that helps in further abstracting and simplifying Spring Cloud deployments.
##### Installation
You can download the Spring CLI distribution from the Spring software repository:

- [spring-boot-cli-2.6.7-bin.zip](https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/2.6.7/spring-boot-cli-2.6.7-bin.zip)
- [spring-boot-cli-2.6.7-bin.tar.gz](https://repo.spring.io/release/org/springframework/boot/spring-boot-cli/2.6.7/spring-boot-cli-2.6.7-bin.tar.gz)

##### Command lines
```spring --version```

`spring help run`

`spring run hello.groovy`

`spring run hello.groovy -- --server.port=9000`

Packaging Your Application:
`spring jar my-app.jar *.groovy`

Initialize a New Project: `spring init --dependencies=web,data-jpa my-project`

`spring init --list`

`spring init --build=gradle --java-version=1.8 --dependencies=websocket --packaging=war sample-app.zip`

Using the Embedded Shell: `spring shell`

Adding Extensions to the CLI: `spring install com.example:spring-boot-cli-extension:1.0.0.RELEASE`

`spring uninstall --all`
##### Deploy core services
- To launch a Cloud Config server on http://localhost:8888: `spring cloud configserver`
- To start a Eureka server on http://localhost:8761: `spring cloud eureka`
- To initiate an H2 server on http://localhost:9095: `spring cloud h2`
- To launch a Kafka server on http://localhost:9091: `spring cloud kafka`
- To start a Zipkin server on http://localhost:9411: `spring cloud zipkin`
- To launch a Dataflow server on http://localhost:9393: `spring cloud dataflow`
- To start a Hystrix dashboard on http://localhost:7979: `spring cloud hystrixdashboard`
- List currently running cloud services: `spring cloud --list`
- The handy help command: `spring help cloud`
- Encrypt and Decrypt With Config Server:
```
spring encrypt my_value --key my_key
$ curl localhost:8888/encrypt -d mysecret
//682bc583f4641835fa2db009355293665d2647dade3375c0ee201de2a49f7bda
$ curl localhost:8888/decrypt -d 682bc583f4641835fa2db009355293665d2647dade3375c0ee201de2a49f7bda
//mysecret
```
##### Customizing Cloud Services
Add other apps into deployment
```yaml
spring:
  cloud:
    launcher:
      deployables:
        - name: configserver
          coordinates: maven://...:spring-cloud-launcher-configserver:1.3.2.RELEASE
          port: 8888
          waitUntilStarted: true
          order: -10
        - name: eureka
          coordinates: maven:/...:spring-cloud-launcher-eureka:1.3.2.RELEASE
          port: 8761
```
#### Spring Cloud Gateway
The tool provides out-of-the-box routing mechanisms often used in microservices applications as a way of hiding multiple services behind a single facade.
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>
```
##### Routing Handler
```java
@Bean
public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
    return builder.routes()
      .route("r1", r -> r.host("**.baeldung.com")
        .and()
        .path("/baeldung")
        .uri("http://baeldung.com"))
      .route(r -> r.host("**.baeldung.com")
        .and()
        .path("/myOtherRouting")
        .filters(f -> f.prefixPath("/myPrefix"))
        .uri("http://othersite.com")
        .id("myOtherID"))
    .build();
}
```
```yaml
spring:
  application:
    name: gateway-service  
  cloud:
    gateway:
      routes:
      - id: cloudgateway
        uri: gateway.com
      - id: myOtherRouting
        uri: localhost:9999
```
We see that the relative url: “/cloudgateway” is configured as a route, so hitting the url “http://localhost/cloudgateway” we'll be redirected to “http://gateway.com“, as was configured in our example.
##### Global Filters
Pre Global Filter: code before `chain.filter()`, run before the real URI is called
```java
@Component
public class LoggingGlobalPreFilter implements GlobalFilter {
    @Override
    public Mono<Void> filter(
      ServerWebExchange exchange,
      GatewayFilterChain chain) {
        // ..
        return chain.filter(exchange);
    }
}
```
Post Global Filter: code after `chain.filter(exchange).then()`, run after the real URI responds, before returning to the client
```java
@Configuration
public class LoggingGlobalFiltersConfigurations {
    @Bean
    public GlobalFilter postGlobalFilter() {
        return (exchange, chain) -> {
            return chain.filter(exchange)
              .then(Mono.fromRunnable(() -> {
                  // ..
              }));
        };
    }
}
```
Combine 'Pre' and 'Post'
```java
 @Override
    public Mono<Void> filter(ServerWebExchange exchange,
      GatewayFilterChain chain) {
        // ..
        return chain.filter(exchange)
          .then(Mono.fromRunnable(() -> {
                // ..
            }));
    }
```
Order: Due to the nature of the filter chain, a filter with lower precedence (a lower order in the chain) will execute its “pre” logic in an earlier stage, but it's “post” implementation will get invoked later
```java
@Override
    public int getOrder() {
        return -1;
    }
```
##### Modifying the Request
```java
@Component
public class ModifyRequestGatewayFilterFactory extends AbstractGatewayFilterFactory<ModifyRequestGatewayFilterFactory.Config> {
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            if (exchange.getRequest()
                    .getHeaders()
                    .getAcceptLanguage()
                    .isEmpty()) {

                String queryParamLocale = exchange.getRequest()
                        .getQueryParams()
                        .getFirst("locale");

                Locale requestLocale = Optional.ofNullable(queryParamLocale)
                        .map(Locale::forLanguageTag)
                        .orElse(config.getDefaultLocale());

                exchange.getRequest()
                        .mutate()
                        .headers(h -> h.setAcceptLanguageAsLocales(Collections.singletonList(requestLocale)));
            }

            String allOutgoingRequestLanguages = exchange.getRequest()
                    .getHeaders()
                    .getAcceptLanguage()
                    .stream()
                    .map(Locale.LanguageRange::getRange)
                    .collect(Collectors.joining(","));

            logger.info("Modify request output - Request contains Accept-Language header: {}", allOutgoingRequestLanguages);

            ServerWebExchange modifiedExchange = exchange.mutate()
                    .request(originalRequest -> originalRequest.uri(UriComponentsBuilder.fromUri(exchange.getRequest()
                                    .getURI())
                            .replaceQueryParams(new LinkedMultiValueMap<String, String>())
                            .build()
                            .toUri()))
                    .build();

            logger.info("Removed all query params: {}", modifiedExchange.getRequest()
                    .getURI());

            return chain.filter(modifiedExchange);
        };
    }
}
```
##### Modifying the Response
```java
    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> chain.filter(exchange)
                .then(Mono.fromRunnable(() -> {
                    ServerHttpResponse response = exchange.getResponse();
                    Optional.ofNullable(exchange.getRequest()
                                    .getQueryParams()
                                    .getFirst("locale"))
                            .ifPresent(qp -> {
                                String responseContentLanguage = Objects.requireNonNull(response.getHeaders()
                                                .getContentLanguage())
                                        .getLanguage();

                                response.getHeaders()
                                        .add("Bael-Custom-Language-Header", responseContentLanguage);
                                logger.info("Added custom header to Response");
                            });
                }));
    }
```
#### Spring Cloud Feign
Feign makes writing web service clients easier with pluggable annotation support, which includes Feign annotations and JAX-RS annotations.

```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-okhttp</artifactId>
</dependency>
<dependency>
    <groupId>io.github.openfeign</groupId>
    <artifactId>feign-httpclient</artifactId>
</dependency>
```
##### Basic implementation
Add `@EnableFeignClients` to our main class. Then declare a Feign client using the `@FeignClient` annotation.
````java
@SpringBootApplication
@EnableFeignClients
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
````
```java
@FeignClient(value = "jplaceholder", url = "https://jsonplaceholder.typicode.com/", fallback = JSONPlaceHolderFallback.class, configuration = MyClientConfiguration.class)
public interface JSONPlaceHolderClient {

    @RequestMapping(method = RequestMethod.GET, value = "/posts")
    List<User> getPosts();

    @RequestMapping(method = RequestMethod.GET, value = "/posts/{postId}", produces = "application/json")
    User getPostById(@PathVariable("postId") Long postId);
}

@Component
public class JSONPlaceHolderFallback implements JSONPlaceHolderClient {

    @Override
    public List<User> getPosts() {
        return Collections.emptyList();
    }

    @Override
    public User getPostById(Long postId) {
        return null;
    }
}
```
With the fallback pattern, when a remote service call fails, rather than generating an exception, the service consumer will execute an alternative code path to try to carry out the action through another means.
##### Interceptors
```java
@Configuration
public class ClientConfiguration {
    @Bean
    public BasicAuthRequestInterceptor basicAuthRequestInterceptor() {
        return new BasicAuthRequestInterceptor("username", "password");
    }
}
```
##### Exception
eign's default error handler, ErrorDecoder.default, always throws a FeignException.
To customize the Exception thrown, we can use a CustomErrorDecoder.
```java
public class CustomErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {

        switch (response.status()) {
            case 400:
                return new BadRequestException();
            case 404:
                return new NotFoundException();
            default:
                return new Exception("Generic error");
        }
    }
}
```
```java
@Bean
public ErrorDecoder errorDecoder() {
    return new CustomErrorDecoder();
}
```
##### Feign Basic Integration Test
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
<dependency>
    <groupId>com.github.tomakehurst</groupId>
    <artifactId>wiremock</artifactId>
    <scope>test</scope>
</dependency>
```
Config server:
```yaml
book:
  service:
    url: http://localhost:9561
```
Map url to client:
```java
@FeignClient(value="simple-books-client", url="${book.service.url}")
public interface BooksClient {

    @RequestMapping("/books")
    List<Book> getBooks();

}
```
ServerMock config:
```java
@TestConfiguration
public class WireMockConfig {
    @Autowired
    private WireMockServer wireMockServer;
    
    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockBooksService() {
        return new WireMockServer(9561);
    }
}
```
Config response for the api /books
```java
public class BookMocks {

    public static void setupMockBooksResponse(WireMockServer mockService) throws IOException {
        mockService.stubFor(WireMock.get(WireMock.urlEqualTo("/books"))
          .willReturn(WireMock.aResponse()
            .withStatus(HttpStatus.OK.value())
            .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .withBody(
              copyToString(
                BookMocks.class.getClassLoader().getResourceAsStream("payload/get-books-response.json"),
                defaultCharset()))));
    }

}
```
Integration Test class:
```java
@SpringBootTest
@ActiveProfiles("test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { WireMockConfig.class })
class BooksClientIntegrationTest {

    @Autowired
    private WireMockServer mockBooksService;
    @Autowired
    private BooksClient booksClient;

    @BeforeEach
    void setUp() throws IOException {
        BookMocks.setupMockBooksResponse(mockBooksService);
    }
    @Test
    public void whenGetBooks_thenBooksShouldBeReturned() {
        assertFalse(booksClient.getBooks().isEmpty());
    }
}
```
##### Feign Integration Test with Ribbon
Improve our client by adding the load-balancing capabilities provided by Ribbon.
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-ribbon</artifactId>
</dependency>
```
Configuration:
```java
@FeignClient("books-service")
public interface BooksClient {
...
```
```yaml
books-service:
  ribbon:
    listOfServers: http://localhost:9561
```
This configuration sets up two WireMock servers, each running on a different port dynamically assigned at runtime. Moreover, it also configures the Ribbon server list with the two mock servers.
```java
@TestConfiguration
@ActiveProfiles("ribbon-test")
public class RibbonTestConfig {

    @Autowired
    private WireMockServer mockBooksService;

    @Autowired
    private WireMockServer secondMockBooksService;

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WireMockServer mockBooksService() {
        return new WireMockServer(options().dynamicPort());
    }

    @Bean(name="secondMockBooksService", initMethod = "start", destroyMethod = "stop")
    public WireMockServer secondBooksMockService() {
        return new WireMockServer(options().dynamicPort());
    }

    @Bean
    public ServerList ribbonServerList() {
        return new StaticServerList<>(
          new Server("localhost", mockBooksService.port()),
          new Server("localhost", secondMockBooksService.port()));
    }
}
```
```java
@SpringBootTest
@ActiveProfiles("ribbon-test")
@EnableConfigurationProperties
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { RibbonTestConfig.class })
class LoadBalancerBooksClientIntegrationTest {

    @Autowired
    private WireMockServer mockBooksService;

    @Autowired
    private WireMockServer secondMockBooksService;

    @Autowired
    private BooksClient booksClient;

    @BeforeEach
    void setUp() throws IOException {
        setupMockBooksResponse(mockBooksService);
        setupMockBooksResponse(secondMockBooksService);
    }
    //..
```
#### Spring Cloud Config
Spring Cloud Config provides server-side and client-side support for externalized configuration in a distributed system. With the Config Server, you have a central place to manage external properties for applications across all environments.
Spring Cloud Config can be used with any application running in any language.

The default implementation of the server storage backend uses git.

The default strategy for locating property sources is to clone a git repository `(at spring.cloud.config.server.git.uri)` and use it to initialize a mini `SpringApplication`.

The HTTP service has resources in the following form:

```yaml
/{application}/{profile}[/{label}]
/{application}-{profile}.yml
/{label}/{application}-{profile}.yml
/{application}-{profile}.properties
/{label}/{application}-{profile}.properties
```
`profile` is an active profile (or comma-separated list of properties), and label is an optional git label (defaults to `master`.)

Spring Cloud Config Server pulls configuration for remote clients from various sources. The following example gets configuration from a git repository (which must be provided), as shown in the following example:
```yaml
spring:
  cloud:
    config:
      server:
        git:
          uri: https://github.com/spring-cloud-samples/config-repo
          username: trolley
          password: strongpassword
```
```yaml
spring:
  profiles:
    active: git
  cloud:
    config:
      server:
        git:
          uri: https://github.com/spring-cloud-samples/config-repo
          proxy:
            https:
              host: my-proxy.host.io
              password: myproxypassword
              port: '3128'
              username: myproxyusername
              nonProxyHosts: example.com
```
```yaml
spring:
  profiles:
    active: redis
  redis:
    host: redis
    port: 16379
```
```yaml
spring:
  profiles:
    active: awss3
  cloud:
    config:
      server:
        awss3:
          region: us-east-1
          bucket: bucket1
```
```yaml
spring:
  profiles:
    active: credhub
  cloud:
    config:
      server:
        credhub:
          url: https://credhub:8844
          oauth2:
            registration-id: credhub-client
  security:
    oauth2:
      client:
        registration:
          credhub-client:
            provider: uaa
            client-id: credhub_config_server
            client-secret: asecret
            authorization-grant-type: client_credentials
        provider:
          uaa:
            token-uri: https://uaa:8443/oauth/token
```
Other sources are any JDBC compatible database, Subversion, Hashicorp Vault, Credhub and local filesystems.

```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```
##### Spring Cloud Config Server
Spring Cloud Config Server provides an HTTP resource-based API for external configuration (name-value pairs or equivalent YAML content). The server is embeddable in a Spring Boot application, by using the `@EnableConfigServer` annotation.
```java
@SpringBootApplication
@EnableConfigServer
public class ConfigServer {
    public static void main(String[] args) {
        SpringApplication.run(ConfigServer.class, args);
    }
}
```
application.properties: Config to use port 8888 instead of the default port 8080
```java
server.port: 8888
spring.cloud.config.server.git.uri: file://${user.home}/config-repo
```
#### Spring Cloud Kubernetes
Spring Cloud Kubernetes provides implementations of well known Spring Cloud interfaces allowing developers to build and run Spring Cloud applications on Kubernetes.

In a microservices environment, there are usually multiple pods running the same service. Kubernetes exposes the service as a collection of endpoints that can be fetched and reached from within a Spring Boot Application running in a pod in the same Kubernetes cluster.

For instance, in our example, we have multiple replicas of the travel agent service, which is accessed from our client service as http://travel-agency-service:8080. However, this internally would translate into accessing different pods such as travel-agency-service-7c9cfff655-4hxnp.
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes</artifactId>
</dependency>
```
```java
@SpringBootApplication
@EnableDiscoveryClient
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}

@RestController
public class ClientController {
    @Autowired
    private DiscoveryClient discoveryClient;
}
```
Environment Setup

`minikube start --vm-driver=virtualbox`

`kubectl config use-context minikube`

`minikube dashboard`

##### ConfigMaps
Typically, microservices require some kind of configuration management. For instance, in Spring Cloud applications, we would use a Spring Cloud Config Server.

However, we can achieve this by using ConfigMaps provided by Kubernetes – provided that we intend to use it for non-sensitive, unencrypted information only. 

```yaml
apiVersion: v1 by d
kind: ConfigMap
metadata:
  name: client-service
data:
  application.properties: |-
    bean.message=Testing reload! Message from backend is: %s <br/> Services : %s
```
Or
```yaml
spring:
  application:
    name: cloud-k8s-app
  cloud:
    kubernetes:
      config:
        name: default-name
        namespace: default-namespace
        sources:
          # Spring Cloud Kubernetes looks up a ConfigMap named c1 in namespace default-namespace
          - name: c1
          # Spring Cloud Kubernetes looks up a ConfigMap named default-name in whatever namespace n2
          - namespace: n2
          # Spring Cloud Kubernetes looks up a ConfigMap named c3 in namespace n3
          - namespace: n3
            name: c3
```
It's important that the name of the ConfigMap matches the name of the application as specified in our “application.properties” file. In this case, it's client-service. 
Next:
`kubectl create -f client-config.yaml`

```java
@Configuration
@ConfigurationProperties(prefix = "bean")
public class ClientConfig {

    private String message = "Message from backend is: %s <br/> Services : %s";

    // getters and setters
}
```
Create a MongoDB service
```yaml
apiVersion: extensions/v1beta1
kind: Deployment
metadata:
  name: mongo
spec:
  replicas: 1
  template:
    metadata:
      labels:
        service: mongo
      name: mongodb-service
    spec:
      containers:
      - args:
        - mongod
        - --smallfiles
        image: mongo:latest
        name: mongo
        env:
          - name: MONGO_INITDB_ROOT_USERNAME
            valueFrom:
              secretKeyRef:
                name: db-secret
                key: username
          - name: MONGO_INITDB_ROOT_PASSWORD
            valueFrom:
              secretKeyRef:
                name: db-secret
                key: password
```
```properties
spring.cloud.kubernetes.reload.enabled=true
spring.cloud.kubernetes.secrets.name=db-secret
spring.data.mongodb.host=mongodb-service
spring.data.mongodb.port=27017
spring.data.mongodb.database=admin
spring.data.mongodb.username=${MONGO_USERNAME}
spring.data.mongodb.password=${MONGO_PASSWORD}
```
##### Ribbon
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-kubernetes-ribbon</artifactId>
</dependency>
```
```java
@RibbonClient(name = "travel-agency-service")
```
```properties
ribbon.http.client.enabled=true
```
#### Spring Cloud Data Flow
Spring Cloud Data Flow is a toolkit for building data integration and real-time data-processing pipelines.

Spring Cloud Data Flow simplifies the development and deployment of applications that are focused on data-processing use cases.

- Source: is the application that consumes events
- Processor: consumes data from the Source, does some processing on it, and emits the processed data to the next application in the pipeline
- Sink: either consumes from a Source or Processor and writes the data to the desired persistence layer

```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-dataflow-server-local</artifactId>
</dependency>
```
```java
@EnableDataFlowServer
@SpringBootApplication
public class SpringDataFlowServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(
          SpringDataFlowServerApplication.class, args);
    }
}
```
`mvn spring-boot:run` The application will boot up on port 9393.
##### Spring Data Flow Shell
`unix:>java -jar spring-cloud-dataflow-shell-2.9.4.jar --help`

`dataflow:>help stream create`
Other commands please refer to Spring Cloud Shell docs

Or
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-dataflow-shell</artifactId>
</dependency>
```
```java
@EnableDataFlowShell
@SpringBootApplication
public class SpringDataFlowShellApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(SpringDataFlowShellApplication.class, args);
    }
}
```
`mvn spring-boot:run` After the shell is running, we can type the help command in the prompt to see a complete list of command that we can perform.

##### Cloud stream
Create a new stream definition: `stream create --name time-to-log --definition 'time-source | time-processor | logging-sink'`

To deploy the stream: `stream deploy --name time-to-log`
```java
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```
```java
@EnableBinding(Source.class)
@SpringBootApplication
public class SpringDataFlowTimeSourceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(
          SpringDataFlowTimeSourceApplication.class, args);
    }
}
```
```java
@Bean
@InboundChannelAdapter(
  value = Source.OUTPUT, 
  poller = @Poller(fixedDelay = "10000", maxMessagesPerPoll = "1")
)
public MessageSource<Long> timeMessageSource() {
    return () -> MessageBuilder.withPayload(new Date().getTime()).build();
}
```
```java
@EnableBinding(Processor.class)
@SpringBootApplication
public class SpringDataFlowTimeProcessorApplication {

    public static void main(String[] args) {
        SpringApplication.run(
          SpringDataFlowTimeProcessorApplication.class, args);
    }
}
```
```java
@Transformer(inputChannel = Processor.INPUT,
        outputChannel = Processor.OUTPUT)
public Object transform(Long timestamp) {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:yy");
    String date = dateFormat.format(timestamp);
    return date;
}
```
```java
@EnableBinding(Sink.class)
@SpringBootApplication
public class SpringDataFlowLoggingSinkApplication {

    public static void main(String[] args) {
	SpringApplication.run(
          SpringDataFlowLoggingSinkApplication.class, args);
    }
}
```
```java
@StreamListener(Sink.INPUT)
public void loggerSink(String date) {
    logger.info("Received: " + date);
}
```
_Source: Baeldung, Spring Cloud docs_