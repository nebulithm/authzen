package org.authzen;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Principal {
    @EqualsAndHashCode.Include
    private final String id;
    private final List<Role> roles;
    private final Policy policy;

    public Principal(String id) {
        this.id = id;
        this.roles = new ArrayList<>();
        this.policy = null;
    }

    public Principal(String id, List<Role> roles, Policy policy) {
        this.id = id;
        this.roles = roles != null ? new ArrayList<>(roles) : new ArrayList<>();
        this.policy = policy;
    }
}
