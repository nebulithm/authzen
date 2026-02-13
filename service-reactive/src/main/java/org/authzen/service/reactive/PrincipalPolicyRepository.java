package org.authzen.service.reactive;

import reactor.core.publisher.Mono;

public interface PrincipalPolicyRepository {
    Mono<PrincipalPolicyRecord> findByPrincipalId(String principalId);
    Mono<PrincipalPolicyRecord> save(PrincipalPolicyRecord record);
    Mono<Void> deleteByPrincipalId(String principalId);
}
