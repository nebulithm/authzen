package org.authzen.examples.webflux;

import org.authzen.*;
import org.authzen.examples.webflux.domain.ExampleAttributes;
import org.authzen.examples.webflux.web.AuthorizeRequest;
import org.authzen.mongodb.reactive.PrincipalPolicyDocument;
import org.authzen.mongodb.reactive.ResourceDocument;
import org.authzen.mongodb.reactive.RolePolicyDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;

class AuthorizationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDb() {
        mongoTemplate.dropCollection(ResourceDocument.class).block();
        mongoTemplate.dropCollection(PrincipalPolicyDocument.class).block();
        mongoTemplate.dropCollection(RolePolicyDocument.class).block();
    }

    private void seedResource(String id, String type, Policy policy) {
        ResourceDocument doc = new ResourceDocument();
        doc.setId(id);
        doc.setType(type);
        doc.setPolicy(policy);
        mongoTemplate.save(doc).block();
    }

    private void seedPrincipalPolicy(String principalId, Policy policy, List<String> roleIds) {
        PrincipalPolicyDocument doc = new PrincipalPolicyDocument();
        doc.setPrincipalId(principalId);
        doc.setPolicy(policy);
        doc.setRoleIds(roleIds != null ? roleIds : List.of());
        mongoTemplate.save(doc).block();
    }

    private void seedRolePolicy(String roleId, String name, Policy policy) {
        RolePolicyDocument doc = new RolePolicyDocument();
        doc.setRoleId(roleId);
        doc.setName(name);
        doc.setPolicy(policy);
        doc.setAttributes(Map.of());
        mongoTemplate.save(doc).block();
    }

    private Statement allowStatement(String principal, String action, String resource) {
        return StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of(principal))
                .actions(List.of(action))
                .resources(List.of(resource)));
    }

    private Statement denyStatement(String principal, String action, String resource) {
        return StatementFactory.create(b -> b
                .effect(Effect.DENY)
                .principals(List.of(principal))
                .actions(List.of(action))
                .resources(List.of(resource)));
    }

    private Policy policyOf(Statement... statements) {
        return PolicyFactory.create(b -> b.statements(List.of(statements)));
    }

    private AuthorizeRequest request(String userId, String resourceId, String action) {
        return new AuthorizeRequest(userId, new ExampleAttributes("engineering", 5), resourceId, action);
    }

    private WebTestClient.ResponseSpec postAuthorize(AuthorizeRequest req) {
        return webTestClient.post().uri("/api/v1/authorize").bodyValue(req).exchange();
    }

    @Test
    void authorize_shouldAllow_whenBothPoliciesAllow() {
        Statement stmt = allowStatement("user-1", "document:read", "doc-1");
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(true);
    }

    @Test
    void authorize_shouldDeny_whenIdentityPolicyExplicitlyDenies() {
        Statement allow = allowStatement("user-1", "document:read", "doc-1");
        Statement deny = denyStatement("user-1", "document:read", "doc-1");
        seedResource("doc-1", "document", policyOf(allow));
        seedPrincipalPolicy("user-1", policyOf(deny), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false)
                .jsonPath("$.reason").isEqualTo("Explicit deny");
    }

    @Test
    void authorize_shouldDeny_whenResourcePolicyExplicitlyDenies() {
        Statement allow = allowStatement("user-1", "document:read", "doc-1");
        Statement deny = denyStatement("user-1", "document:read", "doc-1");
        seedResource("doc-1", "document", policyOf(deny));
        seedPrincipalPolicy("user-1", policyOf(allow), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false)
                .jsonPath("$.reason").isEqualTo("Explicit deny");
    }

    @Test
    void authorize_shouldDeny_whenResourceHasNoPolicy() {
        Statement stmt = allowStatement("user-1", "document:read", "doc-1");
        seedResource("doc-1", "document", null);
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false);
    }

    @Test
    void authorize_shouldDeny_whenPrincipalPolicyNotFound() {
        seedResource("doc-1", "document", null);

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false);
    }

    @Test
    void authorize_shouldFail_whenResourceNotFound() {
        seedPrincipalPolicy("user-1", null, List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().is5xxServerError();
    }

    @Test
    void authorize_shouldDeny_whenActionNotMatched() {
        Statement stmt = allowStatement("user-1", "document:read", "doc-1");
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "write"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false);
    }

    @Test
    void authorize_shouldAllow_whenActionMatchesWildcard() {
        Statement stmt = allowStatement("user-1", "document:*", "doc-1");
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(true);
    }

    @Test
    void authorize_shouldAllow_whenPrincipalMatchesWildcard() {
        Statement stmt = allowStatement("user-*", "document:read", "doc-1");
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(true);
    }

    @Test
    void authorize_shouldDeny_whenPrincipalExcludedByNotPrincipals() {
        Statement stmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("user-*"))
                .notPrincipals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1")));
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false);
    }

    @Test
    void authorize_shouldAllow_whenRolePolicyFetchedFromRepository() {
        Statement roleStmt = allowStatement("role-admin", "document:read", "doc-1");
        seedResource("doc-1", "document", policyOf(roleStmt));
        seedRolePolicy("role-admin", "Admin", policyOf(roleStmt));
        seedPrincipalPolicy("user-1", null, List.of("role-admin"));

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(true);
    }

    @Test
    void authorize_shouldAllow_whenJexlConditionSatisfied() {
        Statement stmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1"))
                .condition("context.clientIp != null"));
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(true);
    }

    @Test
    void authorize_shouldDeny_whenJexlConditionNotSatisfied() {
        Statement stmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1"))
                .condition("context.clientIp == '1.2.3.4'"));
        seedResource("doc-1", "document", policyOf(stmt));
        seedPrincipalPolicy("user-1", policyOf(stmt), List.of());

        postAuthorize(request("user-1", "doc-1", "read"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.allowed").isEqualTo(false);
    }
}
