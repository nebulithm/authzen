package org.authzen;

import tools.jackson.databind.json.JsonMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PolicyDeserializerTest {
    private JsonMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = JsonMapper.builder().addModule(new AuthZenModule()).build();
    }

    @Test
    void testDeserializeValidPolicy() throws Exception {
        String json = """
            {
                "statements": [
                    {
                        "effect": "ALLOW",
                        "principals": ["user-1"],
                        "actions": ["document:read"],
                        "resources": ["doc-*"]
                    },
                    {
                        "effect": "DENY",
                        "principals": ["user-2"],
                        "actions": ["document:delete"],
                        "resources": ["doc-123"]
                    }
                ]
            }
            """;

        Policy policy = mapper.readValue(json, Policy.class);

        assertEquals(2, policy.getStatements().size());
        assertEquals("user-1", policy.getStatements().get(0).getPrincipals().get(0));
        assertEquals("user-2", policy.getStatements().get(1).getPrincipals().get(0));
    }

    @Test
    void testDeserializeEmptyPolicy() throws Exception {
        String json = """
            {
                "statements": []
            }
            """;

        Policy policy = mapper.readValue(json, Policy.class);

        assertTrue(policy.getStatements().isEmpty());
    }

    @Test
    void testDeserializePolicyWithInvalidStatement() {
        String json = """
            {
                "statements": [
                    {
                        "effect": "ALLOW",
                        "actions": ["document:read"],
                        "resources": ["*"],
                        "condition": "invalid @@@ jexl"
                    }
                ]
            }
            """;

        assertThrows(Exception.class, () -> mapper.readValue(json, Policy.class));
    }

    @Test
    void testDeserializeNullStatements() throws Exception {
        String json = "{}";

        Policy policy = mapper.readValue(json, Policy.class);
        
        assertTrue(policy.getStatements().isEmpty());
    }
}
