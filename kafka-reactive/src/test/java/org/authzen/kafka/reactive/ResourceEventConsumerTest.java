package org.authzen.kafka.reactive;

import org.authzen.Policy;
import org.authzen.Resource;
import org.authzen.service.reactive.ResourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResourceEventConsumerTest {

    private ResourceRepository<Resource> repository;
    private ResourceEventConsumer<String, Resource> consumer;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        repository = Mockito.mock(ResourceRepository.class);
    }

    @Test
    void createEventCallsSave() {
        Resource resource = new Resource("r-1", "document");
        consumer = new ResourceEventConsumer<>(repository) {
            @Override
            protected EntityEvent<Resource> mapMessage(String message) {
                return new EntityEvent<>(EventType.CREATE, resource);
            }
        };
        when(repository.save(any())).thenReturn(Mono.just(resource));

        StepVerifier.create(consumer.consume("msg")).verifyComplete();
        verify(repository).save(resource);
    }

    @Test
    void updateEventCallsSave() {
        Resource resource = new Resource("r-1", "document");
        consumer = new ResourceEventConsumer<>(repository) {
            @Override
            protected EntityEvent<Resource> mapMessage(String message) {
                return new EntityEvent<>(EventType.UPDATE, resource);
            }
        };
        when(repository.save(any())).thenReturn(Mono.just(resource));

        StepVerifier.create(consumer.consume("msg")).verifyComplete();
        verify(repository).save(resource);
    }

    @Test
    void deleteEventCallsDeleteById() {
        Resource resource = new Resource("r-1", "document");
        consumer = new ResourceEventConsumer<>(repository) {
            @Override
            protected EntityEvent<Resource> mapMessage(String message) {
                return new EntityEvent<>(EventType.DELETE, resource);
            }
        };
        when(repository.deleteById("r-1")).thenReturn(Mono.empty());

        StepVerifier.create(consumer.consume("msg")).verifyComplete();
        verify(repository).deleteById("r-1");
    }
}
