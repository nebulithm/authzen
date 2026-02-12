package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceTest {

    @Test
    void resourceCreationWithPolicy() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-123"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Resource resource = new Resource("doc-123", "document", policy);

        assertEquals("doc-123", resource.getId());
        assertEquals("document", resource.getType());
        assertNotNull(resource.getPolicy());
        assertEquals(1, resource.getPolicy().getStatements().size());
    }

    @Test
    void resourceCreationWithoutPolicy() {
        Resource resource = new Resource("doc-456", "document");

        assertEquals("doc-456", resource.getId());
        assertEquals("document", resource.getType());
        assertNull(resource.getPolicy());
    }

    @Test
    void equalityBasedOnId() {
        Resource resource1 = new Resource("doc-123", "document", null);
        Resource resource2 = new Resource("doc-123", "file", null);
        Resource resource3 = new Resource("doc-456", "document", null);

        assertEquals(resource1, resource2);
        assertNotEquals(resource1, resource3);
        assertEquals(resource1.hashCode(), resource2.hashCode());
    }
}
