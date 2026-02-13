package org.authzen.service.reactive;

import lombok.Value;
import org.authzen.Policy;

import java.util.Map;

@Value
public class RolePolicyRecord {
    String roleId;
    String name;
    Policy policy;
    Map<String, Object> attributes;
}
