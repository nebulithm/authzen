package org.authzen.kafka.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.service.reactive.RolePolicyRecord;
import org.authzen.service.reactive.RolePolicyRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class RolePolicyEventConsumer<M> {

    private final RolePolicyRepository rolePolicyRepository;

    protected abstract EntityEvent<RolePolicyRecord> mapMessage(M message);

    public Mono<Void> consume(M message) {
        EntityEvent<RolePolicyRecord> event = mapMessage(message);
        return switch (event.getEventType()) {
            case CREATE, UPDATE -> rolePolicyRepository.save(event.getPayload()).then();
            case DELETE -> rolePolicyRepository.deleteById(event.getPayload().getRoleId());
        };
    }
}
