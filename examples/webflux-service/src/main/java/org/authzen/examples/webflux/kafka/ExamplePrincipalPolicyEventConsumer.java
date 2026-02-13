package org.authzen.examples.webflux.kafka;

import org.authzen.kafka.reactive.EntityEvent;
import org.authzen.kafka.reactive.EventType;
import org.authzen.kafka.reactive.PrincipalPolicyEventConsumer;
import org.authzen.service.reactive.PrincipalPolicyRecord;
import org.authzen.service.reactive.PrincipalPolicyRepository;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamplePrincipalPolicyEventConsumer extends PrincipalPolicyEventConsumer<PrincipalPolicyKafkaMessage> {

    public ExamplePrincipalPolicyEventConsumer(PrincipalPolicyRepository principalPolicyRepository) {
        super(principalPolicyRepository);
    }

    @Override
    protected EntityEvent<PrincipalPolicyRecord> mapMessage(PrincipalPolicyKafkaMessage message) {
        EventType eventType = EventType.valueOf(message.getEventType());
        List<String> roleIds = message.getRoleIds() != null ? message.getRoleIds() : List.of();
        PrincipalPolicyRecord record = new PrincipalPolicyRecord(message.getPrincipalId(), null, roleIds);
        return new EntityEvent<>(eventType, record);
    }
}
