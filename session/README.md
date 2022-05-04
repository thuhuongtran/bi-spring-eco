#### Spring Session
Spring Session provides an API and implementations for managing a user’s session information while also making it trivial to support clustered sessions without being tied to an application container-specific solution. It also provides transparent integration with:
- `HttpSession`: Allows replacing the `HttpSession` in an application container-neutral way, with support for providing session IDs in headers to work with RESTful APIs.
- `WebSocket`: Provides the ability to keep the `HttpSession` alive when receiving WebSocket messages
- `WebSession`: Allows replacing the Spring WebFlux’s `WebSession` in an application container-neutral way.

While Spring Session can persist data using JDBC, Gemfire, or MongoDB, we will use Redis.
```java
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.session</groupId>
    <artifactId>spring-session-data-redis</artifactId>
</dependency>
    <dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <type>jar</type>
</dependency>
<dependency>
    <groupId>com.github.kstyrc</groupId>
    <artifactId>embedded-redis</artifactId>
    <version>0.6</version>
</dependency>
```
```java
@Configuration
@EnableRedisHttpSession 
public class Config extends AbstractHttpSessionApplicationInitializer {
    @Bean
    public JedisConnectionFactory connectionFactory() {
        return new JedisConnectionFactory();
    }
}
```
```java
public class SessionControllerTest {

    private Jedis jedis;
    private TestRestTemplate testRestTemplate;
    private TestRestTemplate testRestTemplateWithAuth;
    private String testUrl = "http://localhost:8080/";

    @Before
    public void clearRedisData() {
        testRestTemplate = new TestRestTemplate();
        testRestTemplateWithAuth = new TestRestTemplate("admin", "password", null);

        jedis = new Jedis("localhost", 6379);
        jedis.flushAll();
    }
}
```
_Source: Spring Session Docs, Baeldung_