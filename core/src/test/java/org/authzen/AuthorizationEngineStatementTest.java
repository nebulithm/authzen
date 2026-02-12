package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AuthorizationEngineStatementTest {

    private final AuthorizationEngine engine = new AuthorizationEngine();

    @Test
    void statementExtractionFromPrincipalPolicy() {
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
                        .actions(List.of("document:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision decision = engine.evaluate(principal, resource, "read", null);

        assertTrue(decision.isAllowed());
        assertEquals(2, decision.getMatchedStatements().size());
    }

    @Test
    void statementExtractionFromRolePolicies() {
        Statement roleStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("role-admin"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy rolePolicy = Policy.builder()
                .statements(List.of(roleStatement))
                .build();

        Role adminRole = new Role("role-admin", "Admin", rolePolicy, null);
        Principal principal = new Principal("user-1", List.of(adminRole), null);
        
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("*"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision decision = engine.evaluate(principal, resource, "read", null);

        assertTrue(decision.isAllowed());
    }

    @Test
    void statementMatchingForActionsAndResources() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read", "document:write"))
                .resources(List.of("doc-*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Principal principal = new Principal("user-1", List.of(), policy);
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("*"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision readDecision = engine.evaluate(principal, resource, "read", null);
        assertTrue(readDecision.isAllowed());

        Decision deleteDecision = engine.evaluate(principal, resource, "delete", null);
        assertFalse(deleteDecision.isAllowed());
    }

    @Test
    void conditionEvaluationOnStatements() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .condition("context.time == 'business_hours'")
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        Principal principal = new Principal("user-1", List.of(), policy);
        Resource resource = new Resource("doc-123", "document", Policy.builder().statements(List.of(
                Statement.builder()
                        .effect(Effect.ALLOW)
                        .principals(List.of("*"))
                        .actions(List.of("*:*"))
                        .resources(List.of("*"))
                        .build()
        )).build());

        Decision allowedDecision = engine.evaluate(principal, resource, "read", 
                java.util.Map.of("time", "business_hours"));
        assertTrue(allowedDecision.isAllowed());

        Decision deniedDecision = engine.evaluate(principal, resource, "read", 
                java.util.Map.of("time", "after_hours"));
        assertFalse(deniedDecision.isAllowed());
    }
}
