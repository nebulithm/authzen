package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementFactoryTest {

    @Test
    void createStatementWithValidCondition() {
        Statement statement = StatementFactory.create(builder -> builder
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .condition("context.time == 'business_hours'")
        );

        assertNotNull(statement);
        assertEquals(Effect.ALLOW, statement.getEffect());
        assertEquals("context.time == 'business_hours'", statement.getCondition());
    }

    @Test
    void createStatementWithoutCondition() {
        Statement statement = StatementFactory.create(builder -> builder
                .effect(Effect.DENY)
                .principals(List.of("user-*"))
                .actions(List.of("*:delete"))
                .resources(List.of("*"))
        );

        assertNotNull(statement);
        assertEquals(Effect.DENY, statement.getEffect());
        assertNull(statement.getCondition());
    }

    @Test
    void createStatementWithInvalidConditionThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                StatementFactory.create(builder -> builder
                        .effect(Effect.ALLOW)
                        .principals(List.of("user-1"))
                        .actions(List.of("document:read"))
                        .resources(List.of("*"))
                        .condition("invalid jexl $$$ syntax")
                )
        );

        assertTrue(exception.getMessage().contains("Invalid JEXL condition"));
    }

    @Test
    void createStatementWithEmptyCondition() {
        Statement statement = StatementFactory.create(builder -> builder
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .condition("")
        );

        assertNotNull(statement);
        assertEquals("", statement.getCondition());
    }

    @Test
    void createStatementWithDefaultEmptyLists() {
        Statement statement = StatementFactory.create(builder -> builder
                .effect(Effect.ALLOW)
        );

        assertNotNull(statement.getPrincipals());
        assertNotNull(statement.getNotPrincipals());
        assertNotNull(statement.getActions());
        assertNotNull(statement.getResources());
        assertTrue(statement.getPrincipals().isEmpty());
        assertTrue(statement.getNotPrincipals().isEmpty());
        assertTrue(statement.getActions().isEmpty());
        assertTrue(statement.getResources().isEmpty());
    }

    @Test
    void createStatementWithNullPrincipalsThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                StatementFactory.create(builder -> builder
                        .effect(Effect.ALLOW)
                        .principals(null)
                )
        );

        assertTrue(exception.getMessage().contains("Principals list cannot be null"));
    }

    @Test
    void createStatementWithNullActionsThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                StatementFactory.create(builder -> builder
                        .effect(Effect.ALLOW)
                        .actions(null)
                )
        );

        assertTrue(exception.getMessage().contains("Actions list cannot be null"));
    }

    @Test
    void createStatementWithNullResourcesThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                StatementFactory.create(builder -> builder
                        .effect(Effect.ALLOW)
                        .resources(null)
                )
        );

        assertTrue(exception.getMessage().contains("Resources list cannot be null"));
    }

    @Test
    void createStatementWithNullNotPrincipalsThrowsException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                StatementFactory.create(builder -> builder
                        .effect(Effect.ALLOW)
                        .notPrincipals(null)
                )
        );

        assertTrue(exception.getMessage().contains("NotPrincipals list cannot be null"));
    }
}
