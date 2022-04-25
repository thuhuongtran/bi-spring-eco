package groovy

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ExampleGroovyController {
    @Autowired
    private MyService myService;

    @GetMapping("/")
    public String helloWorld() {
        return myService.sayWorld();
    }
}
