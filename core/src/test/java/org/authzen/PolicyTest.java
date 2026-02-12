package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyTest {

    @Test
    void policyWithMultipleStatements() {
        Statement statement1 = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-*"))
                .build();

        Statement statement2 = Statement.builder()
                .effect(Effect.DENY)
                .principals(List.of("user-2"))
                .actions(List.of("document:delete"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement1, statement2))
                .build();

        assertEquals(2, policy.getStatements().size());
        assertEquals(statement1, policy.getStatements().get(0));
        assertEquals(statement2, policy.getStatements().get(1));
    }

    @Test
    void policyWithEmptyStatementsList() {
        Policy policy = Policy.builder()
                .statements(List.of())
                .build();

        assertNotNull(policy.getStatements());
        assertTrue(policy.getStatements().isEmpty());
    }

    @Test
    void policyWithDefaultEmptyStatements() {
        Policy policy = Policy.builder()
                .build();

        assertNotNull(policy.getStatements());
        assertTrue(policy.getStatements().isEmpty());
    }

    @Test
    void builderPatternWorksCorrectly() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .actions(List.of("*:read"))
                .resources(List.of("*"))
                .build();

        Policy policy = Policy.builder()
                .statements(List.of(statement))
                .build();

        assertNotNull(policy);
        assertEquals(1, policy.getStatements().size());
        assertEquals(Effect.ALLOW, policy.getStatements().get(0).getEffect());
    }
}
