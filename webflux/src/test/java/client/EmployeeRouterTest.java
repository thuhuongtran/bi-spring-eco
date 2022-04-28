package client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import services.model.Employee;

import static org.assertj.core.api.Assertions.assertThat;

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

    @Test
    public void test() {
        WebTestClient
                .bindToServer()
                .baseUrl("http://localhost:8080")
                .build()
                .post()
                .uri("/resource")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().valueEquals("Content-Type", "application/json")
                .expectBody().jsonPath("field").isEqualTo("value");
    }
}
