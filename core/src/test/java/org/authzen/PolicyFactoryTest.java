package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolicyFactoryTest {

    @Test
    void createPolicyWithValidStatements() {
        Policy policy = PolicyFactory.create(builder -> builder
                .statements(List.of(
                        Statement.builder()
                                .effect(Effect.ALLOW)
                                .principals(List.of("user-1"))
                                .actions(List.of("document:read"))
                                .resources(List.of("*"))
                                .condition("context.time == 'business_hours'")
                                .build(),
                        Statement.builder()
                                .effect(Effect.DENY)
                                .principals(List.of("user-*"))
                                .actions(List.of("*:delete"))
                                .resources(List.of("*"))
                                .build()
                ))
        );

        assertNotNull(policy);
        assertEquals(2, policy.getStatements().size());
    }

    @Test
    void createPolicyWithInvalidConditionThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PolicyFactory.create(builder -> builder
                        .statements(List.of(
                                Statement.builder()
                                        .effect(Effect.ALLOW)
                                        .principals(List.of("user-1"))
                                        .actions(List.of("document:read"))
                                        .resources(List.of("*"))
                                        .condition("invalid jexl $$$ syntax")
                                        .build()
                        ))
                )
        );

        assertTrue(exception.getMessage().contains("Invalid JEXL condition"));
    }

    @Test
    void createPolicyWithEmptyStatements() {
        Policy policy = PolicyFactory.create(builder -> builder
                .statements(List.of())
        );

        assertNotNull(policy);
        assertTrue(policy.getStatements().isEmpty());
    }

    @Test
    void createPolicyWithDefaultEmptyStatements() {
        Policy policy = PolicyFactory.create(builder -> builder);

        assertNotNull(policy);
        assertNotNull(policy.getStatements());
        assertTrue(policy.getStatements().isEmpty());
    }

    @Test
    void createPolicyValidatesAllStatements() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PolicyFactory.create(builder -> builder
                        .statements(List.of(
                                Statement.builder()
                                        .effect(Effect.ALLOW)
                                        .principals(List.of("user-1"))
                                        .actions(List.of("document:read"))
                                        .resources(List.of("*"))
                                        .condition("context.valid == true")
                                        .build(),
                                Statement.builder()
                                        .effect(Effect.DENY)
                                        .principals(List.of("user-2"))
                                        .actions(List.of("*:delete"))
                                        .resources(List.of("*"))
                                        .condition("invalid syntax $$$")
                                        .build()
                        ))
                )
        );

        assertTrue(exception.getMessage().contains("Invalid JEXL condition"));
    }

    @Test
    void createPolicyWithNullStatementsThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PolicyFactory.create(builder -> builder
                        .statements(null)
                )
        );

        assertTrue(exception.getMessage().contains("Statements list cannot be null"));
    }

    @Test
    void createPolicyValidatesStatementLists() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                PolicyFactory.create(builder -> builder
                        .statements(List.of(
                                Statement.builder()
                                        .effect(Effect.ALLOW)
                                        .principals(null)
                                        .build()
                        ))
                )
        );

        assertTrue(exception.getMessage().contains("Principals list cannot be null"));
    }
}
