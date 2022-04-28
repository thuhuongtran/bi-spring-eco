package services.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import services.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Mono<Employee> findEmployeeById(String id);

    Flux<Employee> findAllEmployees();
}
