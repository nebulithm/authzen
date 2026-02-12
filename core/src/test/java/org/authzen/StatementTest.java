package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StatementTest {

    @Test
    void builderCreatesStatementWithAllFields() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1", "user-2"))
                .notPrincipals(List.of("user-3"))
                .actions(List.of("document:read", "document:write"))
                .resources(List.of("doc-*"))
                .condition("context.time == 'business_hours'")
                .build();

        assertEquals(Effect.ALLOW, statement.getEffect());
        assertEquals(List.of("user-1", "user-2"), statement.getPrincipals());
        assertEquals(List.of("user-3"), statement.getNotPrincipals());
        assertEquals(List.of("document:read", "document:write"), statement.getActions());
        assertEquals(List.of("doc-*"), statement.getResources());
        assertEquals("context.time == 'business_hours'", statement.getCondition());
    }

    @Test
    void builderHandlesNullCollections() {
        Statement statement = Statement.builder()
                .effect(Effect.DENY)
                .principals(null)
                .notPrincipals(null)
                .actions(null)
                .resources(null)
                .condition(null)
                .build();

        assertEquals(Effect.DENY, statement.getEffect());
        assertNull(statement.getPrincipals());
        assertNull(statement.getNotPrincipals());
        assertNull(statement.getActions());
        assertNull(statement.getResources());
        assertNull(statement.getCondition());
    }

    @Test
    void builderHandlesEmptyCollections() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of())
                .notPrincipals(List.of())
                .actions(List.of())
                .resources(List.of())
                .condition("")
                .build();

        assertEquals(Effect.ALLOW, statement.getEffect());
        assertTrue(statement.getPrincipals().isEmpty());
        assertTrue(statement.getNotPrincipals().isEmpty());
        assertTrue(statement.getActions().isEmpty());
        assertTrue(statement.getResources().isEmpty());
        assertEquals("", statement.getCondition());
    }

    @Test
    void builderUsesDefaultEmptyLists() {
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .build();

        assertEquals(Effect.ALLOW, statement.getEffect());
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
    void statementIsImmutable() {
        List<String> principals = List.of("user-1");
        Statement statement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(principals)
                .actions(List.of("document:read"))
                .resources(List.of("doc-*"))
                .build();

        assertThrows(UnsupportedOperationException.class, () -> 
            statement.getPrincipals().add("user-2")
        );
    }
}
