package org.authzen.examples.webflux.domain;

import org.authzen.Policy;
import org.authzen.Role;
import org.authzen.service.reactive.PrincipalFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamplePrincipalFactory implements PrincipalFactory<ExamplePrincipal, ExampleAttributes> {

    @Override
    public ExamplePrincipal create(String id, ExampleAttributes attributes, List<Role> roles, Policy policy) {
        return new ExamplePrincipal(id, attributes.getDepartment(), roles, policy);
    }
}
