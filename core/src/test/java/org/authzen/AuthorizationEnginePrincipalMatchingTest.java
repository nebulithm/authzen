package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationEnginePrincipalMatchingTest {

    private final AuthorizationEngine engine = new AuthorizationEngine();

    @Test
    void principalIdExactMatch() {
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
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("user-1"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision decision = engine.evaluate(principal, resource, "read", null);
        assertTrue(decision.isAllowed());
    }

    @Test
    void principalIdWildcardMatch() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Principal principal = new Principal("user-123", List.of(), policy);
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("user-*"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision decision = engine.evaluate(principal, resource, "read", null);
        assertTrue(decision.isAllowed());
    }

    @Test
    void roleIdMatching() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("role-admin"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy rolePolicy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Role adminRole = new Role("role-admin", "Admin", rolePolicy, null);
        Principal principal = new Principal("user-1", List.of(adminRole), null);
        
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("role-admin"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision decision = engine.evaluate(principal, resource, "read", null);
        assertTrue(decision.isAllowed());
    }

    @Test
    void notPrincipalsExclusionLogic() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .notPrincipals(List.of("user-blocked"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Principal allowedPrincipal = new Principal("user-1", List.of(), policy);
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("*"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision allowedDecision = engine.evaluate(allowedPrincipal, resource, "read", null);
        assertTrue(allowedDecision.isAllowed());

        Principal blockedPrincipal = new Principal("user-blocked", List.of(), policy);
        Decision blockedDecision = engine.evaluate(blockedPrincipal, resource, "read", null);
        assertFalse(blockedDecision.isAllowed());
    }

    @Test
    void identityPolicyPrincipalValidation() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Principal matchingPrincipal = new Principal("user-1", List.of(), policy);
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("*"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision decision = engine.evaluate(matchingPrincipal, resource, "read", null);
        assertTrue(decision.isAllowed());

        Principal nonMatchingPrincipal = new Principal("user-2", List.of(), policy);
        Decision deniedDecision = engine.evaluate(nonMatchingPrincipal, resource, "read", null);
        assertFalse(deniedDecision.isAllowed());
    }

    @Test
    void resourcePolicyPrincipalValidation() {
        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Resource resource = new Resource("doc-123", "document", resourcePolicy);

        Principal allowedPrincipal = new Principal("user-1", List.of(), Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("user-1"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision allowedDecision = engine.evaluate(allowedPrincipal, resource, "read", null);
        assertTrue(allowedDecision.isAllowed());

        Principal deniedPrincipal = new Principal("user-2", List.of(), Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("user-2"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision deniedDecision = engine.evaluate(deniedPrincipal, resource, "read", null);
        assertFalse(deniedDecision.isAllowed());
    }
}
