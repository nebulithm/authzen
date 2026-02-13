package org.authzen.service.reactive;

import org.authzen.Resource;
import reactor.core.publisher.Mono;

public interface ResourceRepository<R extends Resource> {
    Mono<R> findById(String id);
    Mono<R> save(R resource);
    Mono<Void> deleteById(String id);
}
