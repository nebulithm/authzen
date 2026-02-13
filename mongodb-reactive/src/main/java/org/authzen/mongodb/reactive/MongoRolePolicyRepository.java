package org.authzen.mongodb.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.service.reactive.RolePolicyRecord;
import org.authzen.service.reactive.RolePolicyRepository;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class MongoRolePolicyRepository implements RolePolicyRepository {

    private final ReactiveMongoTemplate mongoTemplate;
    private final RolePolicyDocumentMapper mapper;

    @Override
    public Mono<RolePolicyRecord> findById(String roleId) {
        return mongoTemplate.findById(roleId, RolePolicyDocument.class)
                .map(mapper::toRecord);
    }

    @Override
    public Flux<RolePolicyRecord> findByIds(List<String> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) return Flux.empty();
        return mongoTemplate.find(Query.query(Criteria.where("_id").in(roleIds)), RolePolicyDocument.class)
                .map(mapper::toRecord);
    }

    @Override
    public Mono<RolePolicyRecord> save(RolePolicyRecord record) {
        return mongoTemplate.save(mapper.toDocument(record))
                .map(mapper::toRecord);
    }

    @Override
    public Mono<Void> deleteById(String roleId) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(roleId)), RolePolicyDocument.class)
                .then();
    }
}
