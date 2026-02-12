package org.authzen;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AuthZenIntegrationTest {

    private final AuthZen authZen = new AuthZen();

    @Test
    void basicAuthorizationWithIdentityAndResourcePolicies() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-123"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-123"))
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
    void explicitDenyPrecedence() {
        Statement allowStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-2"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Statement denyStatement = Statement.builder()
                .effect(Effect.DENY)
                .principals(List.of("user-2"))
                .actions(List.of("document:delete"))
                .resources(List.of("*"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(allowStatement, denyStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("*"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-2", List.of(), principalPolicy);
        Resource resource = new Resource("doc-456", "document", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "delete");

        assertFalse(decision.isAllowed());
        assertEquals("Explicit deny", decision.getReason());
    }

    @Test
    void wildcardPatternMatchingInActionsAndResources() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-3"))
                .actions(List.of("*:read", "*:list"))
                .resources(List.of("*"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("*"))
                .actions(List.of("file:*"))
                .resources(List.of("file-*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-3", List.of(), principalPolicy);
        Resource resource = new Resource("file-789", "file", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "read");

        assertTrue(decision.isAllowed());
    }

    @Test
    void jexlConditionsWithContext() {
        Map<String, Object> context = Map.of(
                "time", "business_hours",
                "ipAddress", "10.0.0.1"
        );

        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-4"))
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
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .condition("context.ipAddress != null")
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-4", List.of(), principalPolicy);
        Resource resource = new Resource("doc-999", "document", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "write", context);

        assertTrue(decision.isAllowed());
    }

    @Test
    void roleBasedPolicies() {
        Statement roleStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("role-admin"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Policy rolePolicy = Policy.builder()
                .statements(List.of(roleStatement))
                .build();

        Role adminRole = new Role("role-admin", "Admin", rolePolicy, Map.of("level", "admin"));

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("*"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-5", List.of(adminRole), null);
        Resource resource = new Resource("doc-111", "document", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "update");

        assertTrue(decision.isAllowed());
    }

    @Test
    void defaultDenyBehavior() {
        Principal principal = new Principal("user-6");
        Resource resource = new Resource("doc-222", "document");

        Decision decision = authZen.authorize(principal, resource, "read");

        assertFalse(decision.isAllowed());
        assertEquals("No matching allow policies from both identity and resource", decision.getReason());
    }

    @Test
    void customPrincipalResourceExtensibility() {
        CustomPrincipal principal = new CustomPrincipal("user-7", "engineering");
        CustomResource resource = new CustomResource("doc-333", "document", "engineering");

        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-7"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("*"))
                .actions(List.of("document:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        principal = new CustomPrincipal("user-7", "engineering", List.of(), principalPolicy);
        resource = new CustomResource("doc-333", "document", "engineering", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "read");

        assertTrue(decision.isAllowed());
        assertEquals("engineering", principal.getDepartment());
        assertEquals("engineering", resource.getDepartment());
    }

    @Test
    void principalMatchingExact() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-8"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-8"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-8", List.of(), principalPolicy);
        Resource resource = new Resource("doc-444", "document", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "read");

        assertTrue(decision.isAllowed());
    }

    @Test
    void principalMatchingWildcard() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy principalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Principal principal = new Principal("user-999", List.of(), principalPolicy);
        Resource resource = new Resource("doc-555", "document", resourcePolicy);

        Decision decision = authZen.authorize(principal, resource, "read");

        assertTrue(decision.isAllowed());
    }

    @Test
    void notPrincipalsExclusion() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .notPrincipals(List.of("user-blocked"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
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

        Principal allowedPrincipal = new Principal("user-allowed", List.of(), principalPolicy);
        Resource resource = new Resource("doc-666", "document", resourcePolicy);

        Decision allowedDecision = authZen.authorize(allowedPrincipal, resource, "read");
        assertTrue(allowedDecision.isAllowed());

        Principal blockedPrincipal = new Principal("user-blocked", List.of(), principalPolicy);
        Decision blockedDecision = authZen.authorize(blockedPrincipal, resource, "read");
        assertFalse(blockedDecision.isAllowed());
    }

    @Test
    void identityPolicyPrincipalValidation() {
        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-10"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
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

        Principal matchingPrincipal = new Principal("user-10", List.of(), principalPolicy);
        Resource resource = new Resource("doc-777", "document", resourcePolicy);

        Decision decision = authZen.authorize(matchingPrincipal, resource, "read");
        assertTrue(decision.isAllowed());

        Principal nonMatchingPrincipal = new Principal("user-11", List.of(), principalPolicy);
        Decision deniedDecision = authZen.authorize(nonMatchingPrincipal, resource, "read");
        assertFalse(deniedDecision.isAllowed());
    }

    @Test
    void resourcePolicyPrincipalValidation() {
        Statement resourceStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-12"))
                .actions(List.of("document:read"))
                .resources(List.of("*"))
                .build();

        Policy resourcePolicy = Policy.builder()
                .statements(List.of(resourceStatement))
                .build();

        Resource resource = new Resource("doc-888", "document", resourcePolicy);

        Statement principalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-12"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy allowedPrincipalPolicy = Policy.builder()
                .statements(List.of(principalStatement))
                .build();

        Principal allowedPrincipal = new Principal("user-12", List.of(), allowedPrincipalPolicy);
        Decision allowedDecision = authZen.authorize(allowedPrincipal, resource, "read");
        assertTrue(allowedDecision.isAllowed());

        Statement deniedPrincipalStatement = Statement.builder()
                .effect(Effect.ALLOW)
                .principals(List.of("user-13"))
                .actions(List.of("*:*"))
                .resources(List.of("*"))
                .build();

        Policy deniedPrincipalPolicy = Policy.builder()
                .statements(List.of(deniedPrincipalStatement))
                .build();

        Principal deniedPrincipal = new Principal("user-13", List.of(), deniedPrincipalPolicy);
        Decision deniedDecision = authZen.authorize(deniedPrincipal, resource, "read");
        assertFalse(deniedDecision.isAllowed());
    }

    static class CustomPrincipal extends Principal {
        private final String department;

        public CustomPrincipal(String id, String department) {
            super(id);
            this.department = department;
        }

        public CustomPrincipal(String id, String department, List<Role> roles, Policy policy) {
            super(id, roles, policy);
            this.department = department;
        }

        public String getDepartment() {
            return department;
        }
    }

    static class CustomResource extends Resource {
        private final String department;

        public CustomResource(String id, String type, String department) {
            super(id, type);
            this.department = department;
        }

        public CustomResource(String id, String type, String department, Policy policy) {
            super(id, type, policy);
            this.department = department;
        }

        public String getDepartment() {
            return department;
        }
    }
}
