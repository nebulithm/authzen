package org.authzen.service.reactive;

import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

public interface ContextFactory<C> {
    Mono<C> create(ServerHttpRequest request);
}
