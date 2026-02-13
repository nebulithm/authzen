package org.authzen.examples.webflux.kafka;

import org.authzen.Resource;
import org.authzen.kafka.reactive.EntityEvent;
import org.authzen.kafka.reactive.EventType;
import org.authzen.kafka.reactive.ResourceEventConsumer;
import org.authzen.service.reactive.ResourceRepository;
import org.springframework.stereotype.Component;

@Component
public class ExampleResourceEventConsumer extends ResourceEventConsumer<ResourceKafkaMessage, Resource> {

    public ExampleResourceEventConsumer(ResourceRepository<Resource> resourceRepository) {
        super(resourceRepository);
    }

    @Override
    protected EntityEvent<Resource> mapMessage(ResourceKafkaMessage message) {
        EventType eventType = EventType.valueOf(message.getEventType());
        Resource resource = new Resource(message.getResourceId(), message.getResourceType());
        return new EntityEvent<>(eventType, resource);
    }
}
