package handlingerrors;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
public class FunctionalErrorHandler {
    public Mono<ServerResponse> handleRequest1(ServerRequest request) {
        return sayHello(request).onErrorReturn("Hello, Stranger")
                .flatMap(s -> ServerResponse.ok()
                        .contentType(MediaType.TEXT_PLAIN)
                        .bodyValue(s));
    }

    public Mono<ServerResponse> handleRequest2(ServerRequest request) {
        return
                sayHello(request)
                        .flatMap(s -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(s))
                        .onErrorResume(e -> sayHelloFallback()
                                .flatMap(s -> ServerResponse.ok()
                                        .contentType(MediaType.TEXT_PLAIN)
                                        .bodyValue(s)));
    }

    public Mono<ServerResponse> handleRequest3(ServerRequest request) {
        return
                sayHello(request)
                        .flatMap(s -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(s))
                        .onErrorResume(e -> (Mono.just("Hi, I looked around for your name but found: " +
                                e.getMessage())).flatMap(s -> ServerResponse.ok()
                                .contentType(MediaType.TEXT_PLAIN)
                                .bodyValue(s)));
    }
    private Mono<String> sayHello(ServerRequest request) {
        try {
            return Mono.just("Hello, " + request.queryParam("name")
                    .get());
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    private Mono<String> sayHelloFallback() {
        return Mono.just("Hello, Stranger");
    }
}
