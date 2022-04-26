package aop;

import org.springframework.stereotype.Component;

@Component
public class AopService {
    @LogExecutionTime
    public void serve() throws InterruptedException {
        Thread.sleep(2000);
    }
}
