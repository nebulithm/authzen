package org.authzen.examples.webflux.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolePolicyKafkaMessage {
    private String eventType;
    private String roleId;
    private String name;
    private Map<String, Object> policy;
    private Map<String, Object> attributes;
}
