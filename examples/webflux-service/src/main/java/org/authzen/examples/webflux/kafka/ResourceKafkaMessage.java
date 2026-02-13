package org.authzen.examples.webflux.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResourceKafkaMessage {
    private String eventType;
    private String resourceId;
    private String resourceType;
    private Map<String, Object> policy;
}
