package handlingerrors;

import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Component
public class ErrorHandlingRouter {
    @Bean
    public RouterFunction<ServerResponse> routeRequest1(FunctionalErrorHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/endpoint1")
                .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), handler::handleRequest1);
    }

    @Bean
    public RouterFunction<ServerResponse> routeRequest2(FunctionalErrorHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/endpoint2")
                .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), handler::handleRequest2);
    }

    @Bean
    public RouterFunction<ServerResponse> routeRequest3(FunctionalErrorHandler handler) {
        return RouterFunctions.route(RequestPredicates.GET("/api/endpoint3")
                .and(RequestPredicates.accept(MediaType.TEXT_PLAIN)), handler::handleRequest3);
    }
}
