package datajpa.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.persistence.EntityManagerFactory;

@Configuration
@EnableJpaRepositories
public class ConfigJpa {
    @Bean
    public EntityManagerFactory entityManagerFactory() {
        return null;
    }
}
