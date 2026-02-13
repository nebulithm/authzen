package org.authzen.service.reactive;

import org.authzen.Policy;
import org.authzen.Principal;
import org.authzen.Role;
import java.util.List;

public interface PrincipalFactory<P extends Principal, A> {
    P create(String id, A attributes, List<Role> roles, Policy policy);
}
