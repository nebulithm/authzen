package org.authzen.service.reactive;

import org.authzen.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

class AuthorizationServiceTest {

    private AuthorizationService<Principal, Resource, Map<String, Object>, Map<String, Object>> service;
    private ResourceRepository<Resource> resourceRepository;
    private PrincipalPolicyRepository principalPolicyRepository;
    private RolePolicyRepository rolePolicyRepository;
    private ServerHttpRequest request;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        resourceRepository = Mockito.mock(ResourceRepository.class);
        principalPolicyRepository = Mockito.mock(PrincipalPolicyRepository.class);
        rolePolicyRepository = Mockito.mock(RolePolicyRepository.class);
        request = Mockito.mock(ServerHttpRequest.class);

        PrincipalFactory<Principal, Map<String, Object>> factory = (id, attrs, roles, policy) -> new Principal(id, roles, policy);
        ContextFactory<Map<String, Object>> contextFactory = req -> Mono.just(Map.of());

        service = new AuthorizationService<>(
                new AuthZen(), resourceRepository, principalPolicyRepository, rolePolicyRepository, factory, contextFactory
        );
    }

    @Test
    void authorize_shouldAllow_whenBothPoliciesAllow() {
        Statement stmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1")));

        Resource resource = new Resource("doc-1", "document", PolicyFactory.create(b -> b.statements(List.of(stmt))));
        PrincipalPolicyRecord record = new PrincipalPolicyRecord("user-1",
                PolicyFactory.create(b -> b.statements(List.of(stmt))), List.of());

        when(resourceRepository.findById("doc-1")).thenReturn(Mono.just(resource));
        when(principalPolicyRepository.findByPrincipalId("user-1")).thenReturn(Mono.just(record));
        when(rolePolicyRepository.findByIds(List.of())).thenReturn(Flux.empty());

        StepVerifier.create(service.authorize("user-1", Map.of(), "doc-1", "read", request))
                .assertNext(decision -> assertTrue(decision.isAllowed()))
                .verifyComplete();
    }

    @Test
    void authorize_shouldDeny_whenResourcePolicyMissing() {
        Statement stmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1")));

        Resource resource = new Resource("doc-1", "document");
        PrincipalPolicyRecord record = new PrincipalPolicyRecord("user-1",
                PolicyFactory.create(b -> b.statements(List.of(stmt))), List.of());

        when(resourceRepository.findById("doc-1")).thenReturn(Mono.just(resource));
        when(principalPolicyRepository.findByPrincipalId("user-1")).thenReturn(Mono.just(record));
        when(rolePolicyRepository.findByIds(List.of())).thenReturn(Flux.empty());

        StepVerifier.create(service.authorize("user-1", Map.of(), "doc-1", "read", request))
                .assertNext(decision -> assertFalse(decision.isAllowed()))
                .verifyComplete();
    }

    @Test
    void authorize_shouldAllow_whenRolePolicyFetchedFromRepository() {
        Statement roleStmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("role-admin"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1")));
        Policy rolePolicy = PolicyFactory.create(b -> b.statements(List.of(roleStmt)));

        Resource resource = new Resource("doc-1", "document", PolicyFactory.create(b -> b.statements(List.of(roleStmt))));
        PrincipalPolicyRecord record = new PrincipalPolicyRecord("user-1", null, List.of("role-admin"));

        when(resourceRepository.findById("doc-1")).thenReturn(Mono.just(resource));
        when(principalPolicyRepository.findByPrincipalId("user-1")).thenReturn(Mono.just(record));
        when(rolePolicyRepository.findByIds(List.of("role-admin")))
                .thenReturn(Flux.just(new RolePolicyRecord("role-admin", "Admin", rolePolicy, Map.of())));

        StepVerifier.create(service.authorize("user-1", Map.of(), "doc-1", "read", request))
                .assertNext(decision -> assertTrue(decision.isAllowed()))
                .verifyComplete();
    }

    @Test
    void authorize_shouldThrow_whenResourceNotFound() {
        when(resourceRepository.findById("doc-1")).thenReturn(Mono.empty());
        when(principalPolicyRepository.findByPrincipalId("user-1")).thenReturn(Mono.empty());

        StepVerifier.create(service.authorize("user-1", Map.of(), "doc-1", "read", request))
                .expectErrorSatisfies(ex -> {
                    assertInstanceOf(ResourceNotFoundException.class, ex);
                    assertEquals("doc-1", ((ResourceNotFoundException) ex).getResourceId());
                })
                .verify();
    }

    @Test
    void authorize_shouldDeny_whenPrincipalPolicyNotFound() {
        Statement stmt = StatementFactory.create(b -> b
                .effect(Effect.ALLOW)
                .principals(List.of("user-1"))
                .actions(List.of("document:read"))
                .resources(List.of("doc-1")));
        Resource resource = new Resource("doc-1", "document", PolicyFactory.create(b -> b.statements(List.of(stmt))));

        when(resourceRepository.findById("doc-1")).thenReturn(Mono.just(resource));
        when(principalPolicyRepository.findByPrincipalId("user-1")).thenReturn(Mono.empty());
        when(rolePolicyRepository.findByIds(List.of())).thenReturn(Flux.empty());

        StepVerifier.create(service.authorize("user-1", Map.of(), "doc-1", "read", request))
                .assertNext(decision -> {
                    assertFalse(decision.isAllowed());
                    assertEquals("No matching allow policies from both identity and resource", decision.getReason());
                })
                .verifyComplete();
    }
}
