package org.authzen.examples.webflux.domain;

import org.authzen.service.reactive.ContextFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class ExampleContextFactory implements ContextFactory<Map<String, Object>> {

    @Override
    public Mono<Map<String, Object>> create(ServerHttpRequest request) {
        String clientIp = request.getRemoteAddress() != null
                ? request.getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
        return Mono.just(Map.of("clientIp", clientIp));
    }
}
