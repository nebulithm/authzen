package org.authzen;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StatementDeserializerTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new AuthZenModule());
    }

    @Test
    void testDeserializeValidStatement() throws Exception {
        String json = """
            {
                "effect": "ALLOW",
                "principals": ["user-1", "user-2"],
                "notPrincipals": ["user-3"],
                "actions": ["document:read", "document:write"],
                "resources": ["doc-*"],
                "condition": "principal.id == 'user-1'"
            }
            """;

        Statement statement = mapper.readValue(json, Statement.class);

        assertEquals(Effect.ALLOW, statement.getEffect());
        assertEquals(2, statement.getPrincipals().size());
        assertTrue(statement.getPrincipals().contains("user-1"));
        assertEquals(1, statement.getNotPrincipals().size());
        assertEquals(2, statement.getActions().size());
        assertEquals(1, statement.getResources().size());
        assertEquals("principal.id == 'user-1'", statement.getCondition());
    }

    @Test
    void testDeserializeMinimalStatement() throws Exception {
        String json = """
            {
                "effect": "DENY",
                "actions": ["document:delete"],
                "resources": ["doc-123"]
            }
            """;

        Statement statement = mapper.readValue(json, Statement.class);

        assertEquals(Effect.DENY, statement.getEffect());
        assertTrue(statement.getPrincipals().isEmpty());
        assertTrue(statement.getNotPrincipals().isEmpty());
        assertEquals(1, statement.getActions().size());
        assertEquals(1, statement.getResources().size());
        assertNull(statement.getCondition());
    }

    @Test
    void testDeserializeWithEmptyLists() throws Exception {
        String json = """
            {
                "effect": "ALLOW",
                "principals": [],
                "actions": ["document:read"],
                "resources": ["*"]
            }
            """;

        Statement statement = mapper.readValue(json, Statement.class);

        assertTrue(statement.getPrincipals().isEmpty());
        assertEquals(1, statement.getActions().size());
    }

    @Test
    void testDeserializeInvalidJexlCondition() {
        String json = """
            {
                "effect": "ALLOW",
                "actions": ["document:read"],
                "resources": ["*"],
                "condition": "invalid jexl @@@ syntax"
            }
            """;

        assertThrows(JsonMappingException.class, () -> mapper.readValue(json, Statement.class));
    }

    @Test
    void testDeserializeNullEffect() throws Exception {
        String json = """
            {
                "actions": ["document:read"],
                "resources": ["*"]
            }
            """;

        Statement statement = mapper.readValue(json, Statement.class);
        
        // Effect is null when not provided - factory doesn't validate this
        assertNull(statement.getEffect());
        assertEquals(1, statement.getActions().size());
    }
}
