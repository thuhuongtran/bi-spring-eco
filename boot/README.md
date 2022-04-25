### Spring Boot RestApi Test
```java
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```
##### RestTemplateBuilder
Spring Boot brings both the auto-configured RestTemplateBuilder to simplify creating RestTemplates, and the matching @RestClientTest annotation to test the clients built with RestTemplateBuilder. 

RestTemplateBuilder provides convenience methods for registering message converters, error handlers, URI template handlers, basic authorization and also use any additional customizers that you need.

````java
@Service
public class DetailsServiceClient {

    private final RestTemplate restTemplate;

    public DetailsServiceClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public Details getUserDetails(String name) {
        return restTemplate.getForObject("/{name}/details",
          Details.class, name);
    }
}
````
##### @RestClientTest
For testing such a REST client built with RestTemplateBuilder, you may use a SpringRunner-executed test class annotated with @RestClientTest.

@RestClientTest ensures that Jackson and GSON support is auto-configured, and also adds pre-configured RestTemplateBuilder and MockRestServiceServer instances to the context. 

````java
@RunWith(SpringRunner.class)
@RestClientTest(DetailsServiceClient.class)
public class DetailsServiceClientTest {

    @Autowired
    private DetailsServiceClient client;

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        String detailsString = 
          objectMapper.writeValueAsString(new Details("John Smith", "john"));
        
        this.server.expect(requestTo("/john/details"))
          .andRespond(withSuccess(detailsString, MediaType.APPLICATION_JSON));
    }
````
### Spring Boot Actuator
In essence, Actuator brings production-ready features to our application.

Monitoring our app, gathering metrics, understanding traffic, or the state of our database become trivial with this dependency.

````java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
````
#####  Health Indicators
ReactiveHealthIndicator, has been added to implement reactive health checks.

```java
@Component
public class DownstreamServiceHealthIndicator implements ReactiveHealthIndicator {

    @Override
    public Mono<Health> health() {
        return checkDownstreamServiceHealth().onErrorResume(
          ex -> Mono.just(new Health.Builder().down(ex).build())
        );
    }

    private Mono<Health> checkDownstreamServiceHealth() {
        // we could use WebClient to check health reactively
        return Mono.just(new Health.Builder().up().build());
    }
}
```
#####  Creating a Custom Endpoint
Let's create an Actuator endpoint to query, enable, and disable feature flags in our application:
```java
@Component
@Endpoint(id = "features")
public class FeaturesEndpoint {

    private Map<String, Feature> features = new ConcurrentHashMap<>();

    @ReadOperation
    public Map<String, Feature> features() {
        return features;
    }

    @ReadOperation
    public Feature feature(@Selector String name) {
        return features.get(name);
    }

    @WriteOperation
    public void configureFeature(@Selector String name, Feature feature) {
        features.put(name, feature);
    }

    @DeleteOperation
    public void deleteFeature(@Selector String name) {
        features.remove(name);
    }

    public static class Feature {
        private Boolean enabled;

        // [...] getters and setters 
    }

}
```
As of Spring Boot 2.2, we can organize health indicators into groups and apply the same configuration to all the group members.

For example, we can create a health group named custom by adding this to our application.properties:

```java
management.endpoint.health.group.custom.include=diskSpace,ping
```

We can configure the group to show more details via application.properties:

```java
management.endpoint.health.group.custom.show-components=always
management.endpoint.health.group.custom.show-details=always
```
Now if we send the same request to /actuator/health/custom, we'll see more details:

```json
{
  "status": "UP",
  "components": {
    "diskSpace": {
      "status": "UP",
      "details": {
        "total": 499963170816,
        "free": 91300069376,
        "threshold": 10485760
      }
    },
    "ping": {
      "status": "UP"
    }
  }
}
```
### Handle exceptions
```java
@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({BookNotFoundException.class})
    protected ResponseEntity<Object> handleNotFound(
            Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Book not found",
                new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException() {
    }

    public BookNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
```
### Spring Boot Starter Mail
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.subethamail</groupId>
    <artifactId>subethasmtp</artifactId>
    <version>3.1.7</version>
</dependency>
```
```java
public class MailSender {
    @Autowired
    private JavaMailSender javaMailSender;
    private Wiser wiser;
    private String userTo = "user2@localhost";
    private String userFrom = "user1@localhost";
    private String subject = "Test subject";
    private String textMail = "Text subject mail";

    public void sendMail() {
        final int TEST_PORT = 25;
        wiser = new Wiser(TEST_PORT);
        wiser.start();
        SimpleMailMessage message = composeEmailMessage();
        javaMailSender.send(message);
        List<WiserMessage> messages = wiser.getMessages();
    }

    private SimpleMailMessage composeEmailMessage() {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(userTo);
        mailMessage.setReplyTo(userFrom);
        mailMessage.setFrom(userFrom);
        mailMessage.setSubject(subject);
        mailMessage.setText(textMail);
        return mailMessage;
    }
```
### Spring Boot Logging
```java
Logger logger = LoggerFactory.getLogger(LoggingController.class);
logger.trace("A TRACE Message");
logger.debug("A DEBUG Message");
logger.info("An INFO Message");
logger.warn("A WARN Message");
logger.error("An ERROR Message");
```
### Spring Boot OAuth2 Auto-Configuration
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.12.0</version>
</dependency>
```
As we saw, the Spring Security OAuth stack offered the possibility of setting up an Authorization Server as a Spring Application. But the project has been deprecated, and Spring does not support its own authorization server as of now. Instead, it's recommended to use existing well-established providers such as Okta, Keycloak and ForgeRock.

In the context of OAuth 2.0, a resource server is an application that protects resources via OAuth tokens. These tokens are issued by an authorization server, typically to a client application. The job of the resource server is to validate the token before serving a resource to the client.

For that, we'll be using Keycloak embedded in a Spring Boot Application. Keycloak is an open-source identity and access management solution. 

Resource Server – Using JWTs

```java
public class Foo {
    private long id;
    private String name;
    
    // constructor, getters and setters
}
```
```java
@RestController
@RequestMapping(value = "/foos")
public class FooController {

    @GetMapping(value = "/{id}")
    public Foo findOne(@PathVariable Long id) {
        return new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4));
    }

    @GetMapping
    public List findAll() {
        List fooList = new ArrayList();
        fooList.add(new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4)));
        fooList.add(new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4)));
        fooList.add(new Foo(Long.parseLong(randomNumeric(2)), randomAlphabetic(4)));
        return fooList;
    }
```
```java
@Configuration
public class JWTSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
          .authorizeRequests(authz -> authz
            .antMatchers(HttpMethod.GET, "/foos/**").hasAuthority("SCOPE_read")
            .antMatchers(HttpMethod.POST, "/foos").hasAuthority("SCOPE_write")
            .anyRequest().authenticated())
          .oauth2ResourceServer(oauth2 -> oauth2.jwt());
	}
}
```
##### application.yml
```java
server: 
  port: 8081
  servlet: 
    context-path: /resource-server-jwt

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: http://localhost:8083/auth/realms/springoauth2
```
```java
@Test
public void givenUserWithReadScope_whenGetFooResource_thenSuccess() {
    String accessToken = obtainAccessToken("read");

    Response response = RestAssured.given()
      .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
      .get("http://localhost:8081/resource-server-jwt/foos");
    assertThat(response.as(List.class)).hasSizeGreaterThan(0);
}
```
### Spring Boot Devtools
To enhance the development experience further, Spring released the spring-boot-devtools tool
- Property defaults
- Automatic Restart
  
Using spring-boot-devtools, this process is also automated. Whenever files change in the classpath, applications using spring-boot-devtools will cause the application to restart.
- Live Reload

spring-boot-devtools module includes an embedded LiveReload server that is used to trigger a browser refresh when a resource is changed.

- Global settings
- Remote applications
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
</dependency>
```
### Spring Boot Cli
Spring Boot CLI is a command-line abstraction that allows us to easily run Spring micro-services expressed as Groovy scripts. 

One of the most important commands is telling Spring Boot CLI to run a Groovy script:

```run [SCRIPT_NAME].groovy```

Spring Boot CLI will either automatically infer the dependencies or will do so given the correctly supplied annotations. After this, it will launch an embedded web container and app.

Groovy and Spring come together with Spring Boot CLI to allow powerful, performant micro-services to be quickly scripted in single-file Groovy deployments.

##### @Grab
The @Grab annotation and Groovy's Java-esque import clauses allow for easy dependency management and injection.

```groovy
package org.test

@Grab("spring-boot-starter-actuator")

@RestController
class ExampleRestController{
  //...
}
```
As we can see, spring-boot-starter-actuator comes pre-configured allowing for succinct script deployment without requiring a customized application or environmental properties, XML, or other programmatic configuration, though each of those things can be specified when necessary.

The full list of @Grab arguments — each specifying a library to download and import — is available [here](https://docs.spring.io/spring-boot/docs/current/reference/html/dependency-versions.html)

##### @Controller, @RestController, and @EnableWebMvc

To further expedite deployment, we can alternatively utilize Spring Boot CLI's provided “grab hints” to automatically infer correct dependencies to import.

For example, we can use the familiar @Controller and @Service annotations to quickly scaffold a standard MVC controller and service:

``.groovy`` class
```groovy
@RestController
class Example {
 
    @Autowired
    private MyService myService;

    @GetMapping("/")
    public String helloWorld() {
        return myService.sayWorld();
    }
}

@Service
class MyService {
    public String sayWorld() {
        return "World!";
    }
}
```

