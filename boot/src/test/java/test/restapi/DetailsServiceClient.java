package test.restapi;

import model.Book;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DetailsServiceClient {

    private final RestTemplate restTemplate;

    public DetailsServiceClient(RestTemplateBuilder restTemplateBuilder) {
        restTemplate = restTemplateBuilder.build();
    }

    public Book getUserDetails(String name) {
        return restTemplate.getForObject("/{name}/details",
                Book.class, name);
    }
}
