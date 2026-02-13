package org.authzen.kafka.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.service.reactive.PrincipalPolicyRecord;
import org.authzen.service.reactive.PrincipalPolicyRepository;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public abstract class PrincipalPolicyEventConsumer<M> {

    private final PrincipalPolicyRepository principalPolicyRepository;

    protected abstract EntityEvent<PrincipalPolicyRecord> mapMessage(M message);

    public Mono<Void> consume(M message) {
        EntityEvent<PrincipalPolicyRecord> event = mapMessage(message);
        return switch (event.getEventType()) {
            case CREATE, UPDATE -> principalPolicyRepository.save(event.getPayload()).then();
            case DELETE -> principalPolicyRepository.deleteByPrincipalId(event.getPayload().getPrincipalId());
        };
    }
}
