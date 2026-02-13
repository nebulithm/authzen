package org.authzen.examples.webflux.domain;

import lombok.Getter;
import org.authzen.Policy;
import org.authzen.Principal;
import org.authzen.Role;

import java.util.List;

@Getter
public class ExamplePrincipal extends Principal {
    private final String department;

    public ExamplePrincipal(String id, String department, List<Role> roles, Policy policy) {
        super(id, roles, policy);
        this.department = department;
    }
}
