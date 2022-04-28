### Spring (Boot) WebFlux
#### Basic REST application
Spring WebFlux internally uses Project Reactor and its publisher implementations, Flux and Mono.

Let's start with the spring-boot-starter-webflux dependency, which pulls in all other required dependencies:

- spring-boot and spring-boot-starter for basic Spring Boot application setup
- spring-webflux framework
- reactor-core that we need for reactive streams and also reactor-netty

```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
    <version>2.6.7</version>
</dependency>
```

##### @Controller
On the server, we create an annotated controller that publishes a reactive stream of the Employee resource.
```java
@GetMapping("/{id}")
private Mono<Employee> getEmployeeById(@PathVariable String id) {
    return employeeRepository.findEmployeeById(id);
}

@GetMapping
private Flux<Employee> getAllEmployees() {
        return employeeRepository.findAllEmployees();
}
```
Mono because we return at most one employee.

Flux of type Employee since that's the publisher for 0..n elements.

##### Web Client
We can use WebClient to create a client to retrieve data from the endpoints provided by the EmployeeController.
```java
public class EmployeeWebClient {

    WebClient client = WebClient.create("http://localhost:8080");
}
```
```java
Mono<Employee> employeeMono = client.get()
  .uri("/employees/{id}", "1")
  .retrieve()
  .bodyToMono(Employee.class);

employeeMono.subscribe(System.out::println);
```
```java
Flux<Employee> employeeFlux = client.get()
  .uri("/employees")
  .retrieve()
  .bodyToFlux(Employee.class);
        
employeeFlux.subscribe(System.out::println);
```
##### WebClient Instance
```java
WebClient client = WebClient.builder()
  .baseUrl("http://localhost:8080")
  .defaultCookie("cookieKey", "cookieValue")
  .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) 
  .defaultUriVariables(Collections.singletonMap("url", "http://localhost:8080"))
  .build();
```
```java
HttpClient httpClient = HttpClient.create()
  .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
  .responseTimeout(Duration.ofMillis(5000))
  .doOnConnected(conn -> 
    conn.addHandlerLast(new ReadTimeoutHandler(5000, TimeUnit.MILLISECONDS))
      .addHandlerLast(new WriteTimeoutHandler(5000, TimeUnit.MILLISECONDS)));

WebClient client = WebClient.builder()
  .clientConnector(new ReactorClientHttpConnector(httpClient))
  .build();
```
##### Request
```java
UriSpec<RequestBodySpec> uriSpec = client.post();
RequestBodySpec bodySpec = uriSpec.uri("/resource");
```
```java
RequestHeadersSpec<?> headersSpec = bodySpec.bodyValue("data");

RequestHeadersSpec<?> headersSpec = bodySpec.body(
  Mono.just(new Foo("name")), Foo.class);

RequestHeadersSpec<?> headersSpec = bodySpec.body(
BodyInserters.fromValue("data"));
```
```java
RequestHeadersSpec headersSpec = bodySpec.body(
  BodyInserters.fromPublisher(Mono.just("data")),
  String.class);

LinkedMultiValueMap map = new LinkedMultiValueMap();
map.add("key1", "value1");
map.add("key2", "value2");
RequestHeadersSpec<?> headersSpec = bodySpec.body(
BodyInserters.fromMultipartData(map));
```
##### Response
```java
ResponseSpec responseSpec = headersSpec.header(
    HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
  .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
  .acceptCharset(StandardCharsets.UTF_8)
  .ifNoneMatch("*")
  .ifModifiedSince(ZonedDateTime.now())
  .retrieve();

Mono<String> response = headersSpec.retrieve()
.bodyToMono(String.class);
```
##### WebTestClient
```java
WebTestClient testClient = WebTestClient
  .bindToServer()
  .baseUrl("http://localhost:8080")
  .build();
```
```java
RouterFunction function = RouterFunctions.route(
  RequestPredicates.GET("/resource"),
  request -> ServerResponse.ok().build()
);

WebTestClient
  .bindToRouterFunction(function)
  .build().get().uri("/resource")
  .exchange()
  .expectStatus().isOk()
  .expectBody().isEmpty();
```
```java
WebHandler handler = exchange -> Mono.empty();
WebTestClient.bindToWebHandler(handler).build();
```
```java
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EmployeeRouterTest {
        @Autowired
        private WebTestClient webTestClient;
    
        @Test
        public void testHello() {
            webTestClient
                    .get().uri("/hello")
                    .accept(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(Employee.class).value(greeting -> {
                        assertThat(greeting.getName()).isEqualTo("Hello, Spring!");
                    });
        }
```
#### Logging
```log()``` method to enable logging
```java
Flux<Integer> reactiveStream = Flux.range(1, 5).log();
reactiveStream.subscribe();
```
Output: Five values were emitted and then stream closed with an onComplete() event.
```java
2018-11-11 22:37:04 INFO | onSubscribe([Synchronous Fuseable] FluxRange.RangeSubscription)
2018-11-11 22:37:04 INFO | request(unbounded)
2018-11-11 22:37:04 INFO | onNext(1)
2018-11-11 22:37:04 INFO | onNext(2)
2018-11-11 22:37:04 INFO | onNext(3)
2018-11-11 22:37:04 INFO | onNext(4)
2018-11-11 22:37:04 INFO | onNext(5)
2018-11-11 22:37:04 INFO | onComplete()
```
```take()``` caused the stream to cancel after emitting three events.
```java
Flux<Integer> reactiveStream = Flux.range(1, 5).log().take(3);
```
Output: 
```java
2018-11-11 22:45:35 INFO | onSubscribe([Synchronous Fuseable] FluxRange.RangeSubscription)
2018-11-11 22:45:35 INFO | request(unbounded)
2018-11-11 22:45:35 INFO | onNext(1)
2018-11-11 22:45:35 INFO | onNext(2)
2018-11-11 22:45:35 INFO | onNext(3)
2018-11-11 22:45:35 INFO | cancel()
```
``` log() & take()```: the stream produced three events, but instead of cancel(), we see onComplete().
```java
Flux<Integer> reactiveStream = Flux.range(1, 5).take(3).log();
```
```java
2018-11-11 22:49:23 INFO | onSubscribe([Fuseable] FluxTake.TakeFuseableSubscriber)
2018-11-11 22:49:23 INFO | request(unbounded)
2018-11-11 22:49:23 INFO | onNext(1)
2018-11-11 22:49:23 INFO | onNext(2)
2018-11-11 22:49:23 INFO | onNext(3)
2018-11-11 22:49:23 INFO | onComplete()
```
#### Handling Errors
##### Functional level
We can use ```onErrorReturn()``` to return a static default value whenever an error occurs:
```java
public Mono<ServerResponse> handleRequest(ServerRequest request) {
    return sayHello(request)
      .onErrorReturn("Hello Stranger")
      .flatMap(s -> ServerResponse.ok()
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue(s));
}
```
```onErrorResume```
There are three ways that we can use onErrorResume to handle errors:
- Compute a dynamic fallback value
- Execute an alternative path with a fallback method
- Catch, wrap and re-throw an error, e.g., as a custom business exception
```java
public Mono<ServerResponse> handleRequest(ServerRequest request) {
    return sayHello(request)
      .flatMap(s -> ServerResponse.ok()
      .contentType(MediaType.TEXT_PLAIN)
          .bodyValue(s))
        .onErrorResume(e -> Mono.just("Error " + e.getMessage())
          .flatMap(s -> ServerResponse.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .bodyValue(s)));
}
```
Next, let's call a fallback method when an error occurs:
```java
public Mono<ServerResponse> handleRequest(ServerRequest request) {
    return sayHello(request)
      .flatMap(s -> ServerResponse.ok()
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue(s))
      .onErrorResume(e -> sayHelloFallback()
      .flatMap(s -> ServerResponse.ok()
      .contentType(MediaType.TEXT_PLAIN)
      .bodyValue(s)));
}
```
Catch, wrap and re-throw an error
```java
public Mono<ServerResponse> handleRequest(ServerRequest request) {
    return ServerResponse.ok()
      .body(sayHello(request)
      .onErrorResume(e -> Mono.error(new NameRequiredException(
        HttpStatus.BAD_REQUEST, 
        "username is required", e))), String.class);
}
```
##### Global level
However, we can opt to handle our WebFlux errors at a global level.
- Customize the Global Error Response Attributes
- Implement the Global Error Handler
```java
public class GlobalErrorAttributes extends DefaultErrorAttributes{
    
    @Override
    public Map<String, Object> getErrorAttributes(ServerRequest request, 
      ErrorAttributeOptions options) {
        Map<String, Object> map = super.getErrorAttributes(
          request, options);
        map.put("status", HttpStatus.BAD_REQUEST);
        map.put("message", "username is required");
        return map;
    }

}
```
Implement the Global Error Handler. Spring provides a convenient AbstractErrorWebExceptionHandler class for us to extend and implement in handling global errors. 

In this example, we set the order of our global error handler to -2. This is to give it a higher priority than the DefaultErrorWebExceptionHandler, which is registered at ```@Order(-1)```
```java
@Component
@Order(-2)
public class GlobalErrorWebExceptionHandler extends 
    AbstractErrorWebExceptionHandler {

    // constructors

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(
      ErrorAttributes errorAttributes) {

        return RouterFunctions.route(
          RequestPredicates.all(), this::renderErrorResponse);
    }

    private Mono<ServerResponse> renderErrorResponse(
       ServerRequest request) {

       Map<String, Object> errorPropertiesMap = getErrorAttributes(request, 
         ErrorAttributeOptions.defaults());

       return ServerResponse.status(HttpStatus.BAD_REQUEST)
         .contentType(MediaType.APPLICATION_JSON)
         .body(BodyInserters.fromValue(errorPropertiesMap));
    }
}
```
#### WebClient Oauth2
##### WebClient Config
```java
spring.security.oauth2.client.registration.bael.client-name=bael
spring.security.oauth2.client.registration.bael.client-id=bael-client-id
spring.security.oauth2.client.registration.bael.client-secret=bael-secret
spring.security.oauth2.client.registration.bael
  .authorization-grant-type=authorization_code
spring.security.oauth2.client.registration.bael
  .redirect-uri=http://localhost:8080/login/oauth2/code/bael

spring.security.oauth2.client.provider.bael.token-uri=http://localhost:8085/oauth/token
spring.security.oauth2.client.provider.bael
  .authorization-uri=http://localhost:8085/oauth/authorize
spring.security.oauth2.client.provider.bael.user-info-uri=http://localhost:8084/user
spring.security.oauth2.client.provider.bael.user-name-attribute=name
```
````java
@Bean
WebClient webClient(
  ReactiveClientRegistrationRepository clientRegistrations,
  ServerOAuth2AuthorizedClientRepository authorizedClients) {
    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth =
      new ServerOAuth2AuthorizedClientExchangeFilterFunction(
        clientRegistrations,
        authorizedClients);
    oauth.setDefaultOAuth2AuthorizedClient(true);
    return WebClient.builder()
      .filter(oauth)
      .build();
}
````
##### HTTP Security Configuration
The most common scenario is using Spring Security's OAuth2 Login capabilities to authenticate users and give them access to our endpoints and resources.
```java
@Bean
public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    http.authorizeExchange()
      .anyExchange()
      .authenticated()
      .and()
      .oauth2Login();
    return http.build();
}
```
##### Using the WebClient
```java
@GetMapping("/auth-code")
    Mono<String> useOauthWithAuthCode() {
        Mono<String> retrievedResource = webClient.get()
          .uri("http://localhost:8084/retrieve-resource")
          .retrieve()
          .bodyToMono(String.class);
        return retrievedResource.map(string ->
          "We retrieved the following resource using Oauth: " + string);
    }
```
Earlier, we saw that using the ```setDefaultOAuth2AuthorizedClient``` implies that the application will include the access token in any call we realize with the client.

Since we associated the Principal with authorized clients, we can obtain the OAuth2AuthorizedClient instance using the ```@RegisteredOAuth2AuthorizedClient``` annotation:

```java
@GetMapping("/auth-code-annotated")
Mono<String> useOauthWithAuthCodeAndAnnotation(
  @RegisteredOAuth2AuthorizedClient("bael") OAuth2AuthorizedClient authorizedClient) {
    Mono<String> retrievedResource = webClient.get()
      .uri("http://localhost:8084/retrieve-resource")
      .attributes(
        ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient(authorizedClient))
      .retrieve()
      .bodyToMono(String.class);
    return retrievedResource.map(string -> 
      "Resource: " + string 
        + " - Principal associated: " + authorizedClient.getPrincipalName() 
        + " - Token will expire at: " + authorizedClient.getAccessToken()
          .getExpiresAt());
}
```
_Source: Baeldung, Spring docs_