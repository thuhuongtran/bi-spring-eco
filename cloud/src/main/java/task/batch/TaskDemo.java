package task.batch;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;

@SpringBootApplication
@EnableTask
@EnableBatchProcessing
public class TaskDemo {
    @Autowired
    private DataSource dataSource;

    @Bean
    public HelloWorldTaskConfigurer getTaskConfigurer() {
        return new HelloWorldTaskConfigurer(dataSource);
    }

    @Bean
    public TaskListener taskListener() {
        return new TaskListener();
    }

    public static void main(String[] args) {
        SpringApplication.run(TaskDemo.class, args);
    }
}
