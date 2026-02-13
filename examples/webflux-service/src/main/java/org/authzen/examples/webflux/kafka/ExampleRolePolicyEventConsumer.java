package org.authzen.examples.webflux.kafka;

import org.authzen.kafka.reactive.EntityEvent;
import org.authzen.kafka.reactive.EventType;
import org.authzen.kafka.reactive.RolePolicyEventConsumer;
import org.authzen.service.reactive.RolePolicyRecord;
import org.authzen.service.reactive.RolePolicyRepository;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ExampleRolePolicyEventConsumer extends RolePolicyEventConsumer<RolePolicyKafkaMessage> {

    public ExampleRolePolicyEventConsumer(RolePolicyRepository rolePolicyRepository) {
        super(rolePolicyRepository);
    }

    @Override
    protected EntityEvent<RolePolicyRecord> mapMessage(RolePolicyKafkaMessage message) {
        EventType eventType = EventType.valueOf(message.getEventType());
        RolePolicyRecord record = new RolePolicyRecord(
                message.getRoleId(), message.getName(), null,
                message.getAttributes() != null ? message.getAttributes() : Map.of());
        return new EntityEvent<>(eventType, record);
    }
}
