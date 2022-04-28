package client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import services.model.Employee;

@Component
public class EmployeeWebClient {
    WebClient client = WebClient.create("http://localhost:8080");

    public void findEmployeeById() {
        Mono<Employee> employeeMono = client.get()
                .uri("/employees/{id}", "1")
                .retrieve()
                .bodyToMono(Employee.class);
        employeeMono.subscribe(System.out::println);
    }

    public void listEmployees() {
        Flux<Employee> employeeFlux = client.get()
                .uri("/employees")
                .retrieve()
                .bodyToFlux(Employee.class);

        employeeFlux.subscribe(System.out::println);
    }
}
