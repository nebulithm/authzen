package org.authzen.examples.webflux.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaListeners {

    private final ExampleResourceEventConsumer resourceEventConsumer;
    private final ExamplePrincipalPolicyEventConsumer principalPolicyEventConsumer;
    private final ExampleRolePolicyEventConsumer rolePolicyEventConsumer;

    @KafkaListener(topics = "resource-events", containerFactory = "resourceKafkaListenerContainerFactory")
    public void onResourceEvent(ResourceKafkaMessage message) {
        resourceEventConsumer.consume(message).block();
    }

    @KafkaListener(topics = "principal-policy-events", containerFactory = "principalPolicyKafkaListenerContainerFactory")
    public void onPrincipalPolicyEvent(PrincipalPolicyKafkaMessage message) {
        principalPolicyEventConsumer.consume(message).block();
    }

    @KafkaListener(topics = "role-policy-events", containerFactory = "rolePolicyKafkaListenerContainerFactory")
    public void onRolePolicyEvent(RolePolicyKafkaMessage message) {
        rolePolicyEventConsumer.consume(message).block();
    }
}
