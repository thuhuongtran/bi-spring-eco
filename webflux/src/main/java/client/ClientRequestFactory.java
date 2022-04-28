package client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import services.model.Employee;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;

@Component
public class ClientRequestFactory {
    @Autowired
    private WebClientFactory clientFactory;

    public void createRequest() {
        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = clientFactory.createWebClientWithTimeout().post();
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(
                uriBuilder -> uriBuilder.pathSegment("/resource").build());
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.body(
                Mono.just(new Employee()), Employee.class);
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .ifNoneMatch("*")
                .ifModifiedSince(ZonedDateTime.now())
                .retrieve();

        // getting the response
        Mono<String> response = headersSpec.retrieve()
                .bodyToMono(String.class);
    }

    public WebClient.RequestHeadersSpec<?> buildHeaderWithMapParams() {
        WebClient.UriSpec<WebClient.RequestBodySpec> uriSpec = clientFactory.createWebClientWithTimeout().post();
        WebClient.RequestBodySpec bodySpec = uriSpec.uri(
                uriBuilder -> uriBuilder.pathSegment("/resource").build());
        LinkedMultiValueMap map = new LinkedMultiValueMap();
        map.add("key1", "value1");
        map.add("key2", "value2");
        WebClient.RequestHeadersSpec<?> headersSpec = bodySpec.body(
                BodyInserters.fromMultipartData(map));
        return headersSpec;
    }

    public WebClient.ResponseSpec buildResponse(WebClient.RequestHeadersSpec<?> headersSpec) {
        WebClient.ResponseSpec responseSpec = headersSpec.header(
                        HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
                .acceptCharset(StandardCharsets.UTF_8)
                .ifNoneMatch("*")
                .ifModifiedSince(ZonedDateTime.now())
                .retrieve();
        return responseSpec;
    }
}
