package org.authzen;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthZenModuleTest {
    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
        mapper.registerModule(new AuthZenModule());
    }

    @Test
    void testModuleRegistration() {
        assertNotNull(mapper);
        assertTrue(mapper.getRegisteredModuleIds().contains("AuthZenModule"));
    }

    @Test
    void testEndToEndPolicyDeserialization() throws Exception {
        String json = """
            {
                "statements": [
                    {
                        "effect": "ALLOW",
                        "principals": ["user-1", "user-2"],
                        "actions": ["document:read", "document:write"],
                        "resources": ["doc-*"]
                    },
                    {
                        "effect": "DENY",
                        "principals": ["user-3"],
                        "actions": ["document:delete"],
                        "resources": ["doc-sensitive"]
                    }
                ]
            }
            """;

        Policy policy = mapper.readValue(json, Policy.class);

        assertNotNull(policy);
        assertEquals(2, policy.getStatements().size());
        
        Statement allowStatement = policy.getStatements().get(0);
        assertEquals(Effect.ALLOW, allowStatement.getEffect());
        assertEquals(2, allowStatement.getPrincipals().size());
        assertEquals(2, allowStatement.getActions().size());
        
        Statement denyStatement = policy.getStatements().get(1);
        assertEquals(Effect.DENY, denyStatement.getEffect());
        assertEquals(1, denyStatement.getPrincipals().size());
    }

    @Test
    void testEndToEndStatementDeserialization() throws Exception {
        String json = """
            {
                "effect": "ALLOW",
                "principals": ["role-admin"],
                "actions": ["*"],
                "resources": ["*"]
            }
            """;

        Statement statement = mapper.readValue(json, Statement.class);

        assertNotNull(statement);
        assertEquals(Effect.ALLOW, statement.getEffect());
        assertEquals(1, statement.getPrincipals().size());
        assertEquals("role-admin", statement.getPrincipals().get(0));
        assertEquals("*", statement.getActions().get(0));
        assertEquals("*", statement.getResources().get(0));
    }

    @Test
    void testFactoryValidationApplied() {
        String json = """
            {
                "effect": "ALLOW",
                "actions": ["document:read"],
                "resources": ["*"],
                "condition": "this is not valid JEXL syntax @@@ ###"
            }
            """;

        JsonMappingException exception = assertThrows(JsonMappingException.class, 
            () -> mapper.readValue(json, Statement.class));
        
        assertTrue(exception.getMessage().contains("Failed to deserialize Statement"));
    }

    @Test
    void testComplexPolicyWithConditions() throws Exception {
        String json = """
            {
                "statements": [
                    {
                        "effect": "ALLOW",
                        "principals": ["user-*"],
                        "actions": ["document:read"],
                        "resources": ["doc-*"],
                        "condition": "principal.id == 'user-1'"
                    },
                    {
                        "effect": "ALLOW",
                        "principals": ["role-manager"],
                        "actions": ["document:*"],
                        "resources": ["*"],
                        "condition": "context.department == 'engineering'"
                    }
                ]
            }
            """;

        Policy policy = mapper.readValue(json, Policy.class);

        assertNotNull(policy);
        assertEquals(2, policy.getStatements().size());
        
        Statement firstStatement = policy.getStatements().get(0);
        assertEquals("principal.id == 'user-1'", firstStatement.getCondition());
        
        Statement secondStatement = policy.getStatements().get(1);
        assertEquals("context.department == 'engineering'", secondStatement.getCondition());
    }
}
