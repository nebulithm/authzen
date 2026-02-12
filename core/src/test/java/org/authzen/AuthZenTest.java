package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthZenTest {

    private final AuthZen authZen = new AuthZen();

    @Test
    void basicAuthorizationWithPrincipal() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-1", List.of(), principalPolicy);
        Resource resource = new Resource("doc-123", "document", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "read");

        assertTrue(decision.isAllowed());
        assertEquals("Both identity and resource policies allow", decision.getReason());
    }

    @Test
    void authorizationWithContext() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .condition("context.time == 'business_hours'")
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("*"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-1", List.of(), principalPolicy);
        Resource resource = new Resource("doc-123", "document", resourcePolicy);

        Decision allowedDecision = authZen.authorize(principal, resource, "read", 
                Map.of("time", "business_hours"));
        assertTrue(allowedDecision.isAllowed());

        Decision deniedDecision = authZen.authorize(principal, resource, "read", 
                Map.of("time", "after_hours"));
        assertFalse(deniedDecision.isAllowed());
    }

    @Test
    void delegationToAuthorizationEngine() {
        Principal principal = new Principal("user-1");
        Resource resource = new Resource("doc-123", "document");

        Decision decision = authZen.authorize(principal, resource, "read");

        assertNotNull(decision);
        assertFalse(decision.isAllowed());
        assertEquals("No matching allow policies from both identity and resource", decision.getReason());
    }
}
