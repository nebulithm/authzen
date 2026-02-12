package org.authzen;

import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Resource {
    @EqualsAndHashCode.Include
    private final String id;
    private final String type;
    private final Policy policy;

    public Resource(String id, String type) {
        this.id = id;
        this.type = type;
        this.policy = null;
    }

    public Resource(String id, String type, Policy policy) {
        this.id = id;
        this.type = type;
        this.policy = policy;
    }
}
