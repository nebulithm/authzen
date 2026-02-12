package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PrincipalTest {

    @Test
    void principalCreationWithPolicy() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Principal principal = new Principal("user-1", List.of(), policy);

        assertEquals("user-1", principal.getId());
        assertNotNull(principal.getPolicy());
        assertEquals(1, principal.getPolicy().getStatements().size());
        assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    void principalCreationWithoutPolicy() {
        Principal principal = new Principal("user-2");

        assertEquals("user-2", principal.getId());
        assertNull(principal.getPolicy());
        assertNotNull(principal.getRoles());
        assertTrue(principal.getRoles().isEmpty());
    }

    @Test
    void principalWithRoles() {
        Role role = new Role("admin", "Administrator");

        Principal principal = new Principal("user-3", List.of(role), null);

        assertEquals("user-3", principal.getId());
        assertEquals(1, principal.getRoles().size());
        assertEquals("admin", principal.getRoles().get(0).getId());
        assertNull(principal.getPolicy());
    }

    @Test
    void equalityBasedOnId() {
        Principal principal1 = new Principal("user-1", List.of(), null);
        Principal principal2 = new Principal("user-1", List.of(), null);
        Principal principal3 = new Principal("user-2", List.of(), null);

        assertEquals(principal1, principal2);
        assertNotEquals(principal1, principal3);
        assertEquals(principal1.hashCode(), principal2.hashCode());
    }
}
