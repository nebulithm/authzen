package org.authzen;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Role {
    @EqualsAndHashCode.Include
    private final String id;
    private final String name;
    private final Policy policy;
    private final Map<String, Object> attributes;

    public Role(String id, String name) {
        this.id = id;
        this.name = name;
        this.policy = null;
        this.attributes = new HashMap<>();
    }

    public Role(String id, String name, Policy policy, Map<String, Object> attributes) {
        this.id = id;
        this.name = name;
        this.policy = policy;
        this.attributes = attributes != null ? new HashMap<>(attributes) : new HashMap<>();
    }
}
