package org.authzen.service.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class AuthorizationService<P extends Principal, R extends Resource, A, C> {

    private static final Policy EMPTY_POLICY = PolicyFactory.create(b -> b.statements(List.of()));

    private final AuthZen authZen;
    private final ResourceRepository<R> resourceRepository;
    private final PrincipalPolicyRepository principalPolicyRepository;
    private final RolePolicyRepository rolePolicyRepository;
    private final PrincipalFactory<P, A> principalFactory;
    private final ContextFactory<C> contextFactory;

    public Mono<Decision> authorize(String principalId, A attributes, String resourceId, String action, ServerHttpRequest request) {
        Mono<R> resourceMono = resourceRepository.findById(resourceId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(resourceId)));
        Mono<PrincipalPolicyRecord> policyMono = principalPolicyRepository.findByPrincipalId(principalId)
                .defaultIfEmpty(new PrincipalPolicyRecord(principalId, EMPTY_POLICY, List.of()));

        return Mono.zip(resourceMono, policyMono, contextFactory.create(request))
                .flatMap(tuple -> {
                    R resource = tuple.getT1();
                    PrincipalPolicyRecord record = tuple.getT2();
                    C context = tuple.getT3();

                    return rolePolicyRepository.findByIds(record.getRoleIds())
                            .map(rp -> new Role(rp.getRoleId(), rp.getName(), rp.getPolicy(), rp.getAttributes()))
                            .collectList()
                            .map(roles -> {
                                P principal = principalFactory.create(principalId, attributes, roles, record.getPolicy());
                                return authZen.authorize(principal, resource, action, context);
                            });
                });
    }
}
