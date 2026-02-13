package org.authzen.kafka.reactive;

import org.authzen.Policy;
import org.authzen.service.reactive.PrincipalPolicyRecord;
import org.authzen.service.reactive.PrincipalPolicyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PrincipalPolicyEventConsumerTest {

    private PrincipalPolicyRepository repository;
    private PrincipalPolicyEventConsumer<String> consumer;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(PrincipalPolicyRepository.class);
    }

    @Test
    void createEventCallsSave() {
        PrincipalPolicyRecord record = new PrincipalPolicyRecord("p-1", null, List.of());
        consumer = new PrincipalPolicyEventConsumer<>(repository) {
            @Override
            protected EntityEvent<PrincipalPolicyRecord> mapMessage(String message) {
                return new EntityEvent<>(EventType.CREATE, record);
            }
        };
        when(repository.save(any())).thenReturn(Mono.just(record));

        StepVerifier.create(consumer.consume("msg")).verifyComplete();
        verify(repository).save(record);
    }

    @Test
    void deleteEventCallsDeleteByPrincipalId() {
        PrincipalPolicyRecord record = new PrincipalPolicyRecord("p-1", null, List.of());
        consumer = new PrincipalPolicyEventConsumer<>(repository) {
            @Override
            protected EntityEvent<PrincipalPolicyRecord> mapMessage(String message) {
                return new EntityEvent<>(EventType.DELETE, record);
            }
        };
        when(repository.deleteByPrincipalId("p-1")).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume("msg")).verifyComplete();
        verify(repository).deleteByPrincipalId("p-1");
    }
}
