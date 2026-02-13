package org.authzen.kafka.reactive;

import lombok.Value;

@Value
public class EntityEvent<T> {
    EventType eventType;
    T payload;
}
