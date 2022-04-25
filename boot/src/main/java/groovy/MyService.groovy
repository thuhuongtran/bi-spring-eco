package groovy

import org.springframework.stereotype.Service

@Service
class MyService {
    String sayWorld() {
        return "World!";
    }
}
