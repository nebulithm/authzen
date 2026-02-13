package org.authzen.kafka.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.Resource;
import org.authzen.service.reactive.ResourceRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class ResourceEventConsumer<M, R extends Resource> {

    private final ResourceRepository<R> resourceRepository;

    protected abstract EntityEvent<R> mapMessage(M message);

    public Mono<Void> consume(M message) {
        EntityEvent<R> event = mapMessage(message);
        return switch (event.getEventType()) {
            case CREATE, UPDATE -> resourceRepository.save(event.getPayload()).then();
            case DELETE -> resourceRepository.deleteById(event.getPayload().getId());
        };
    }
}
