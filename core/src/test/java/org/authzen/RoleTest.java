package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RoleTest {

    @Test
    void roleCreationWithPolicy() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("role-admin"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Role role = new Role("admin", "Administrator", policy, Map.of("level", "high"));

        assertEquals("admin", role.getId());
        assertEquals("Administrator", role.getName());
        assertNotNull(role.getPolicy());
        assertEquals(1, role.getPolicy().getStatements().size());
        assertEquals("high", role.getAttributes().get("level"));
    }

    @Test
    void roleCreationWithoutPolicy() {
        Role role = new Role("viewer", "Viewer");

        assertEquals("viewer", role.getId());
        assertEquals("Viewer", role.getName());
        assertNull(role.getPolicy());
        assertNotNull(role.getAttributes());
        assertTrue(role.getAttributes().isEmpty());
    }

    @Test
    void equalityBasedOnId() {
        Role role1 = new Role("admin", "Administrator", null, null);
        Role role2 = new Role("admin", "Admin", null, null);
        Role role3 = new Role("viewer", "Viewer", null, null);

        assertEquals(role1, role2);
        assertNotEquals(role1, role3);
        assertEquals(role1.hashCode(), role2.hashCode());
    }
}
