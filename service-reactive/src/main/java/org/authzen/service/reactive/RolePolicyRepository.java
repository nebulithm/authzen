package org.authzen.service.reactive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RolePolicyRepository {
    Mono<RolePolicyRecord> findById(String roleId);
    Flux<RolePolicyRecord> findByIds(List<String> roleIds);
    Mono<RolePolicyRecord> save(RolePolicyRecord record);
    Mono<Void> deleteById(String roleId);
}
