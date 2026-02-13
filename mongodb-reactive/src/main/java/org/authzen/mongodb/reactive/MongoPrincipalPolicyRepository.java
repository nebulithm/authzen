package org.authzen.mongodb.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.service.reactive.PrincipalPolicyRecord;
import org.authzen.service.reactive.PrincipalPolicyRepository;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class MongoPrincipalPolicyRepository implements PrincipalPolicyRepository {

    private final ReactiveMongoTemplate mongoTemplate;
    private final PrincipalPolicyDocumentMapper mapper;

    @Override
    public Mono<PrincipalPolicyRecord> findByPrincipalId(String principalId) {
        return mongoTemplate.findById(principalId, PrincipalPolicyDocument.class)
                .map(mapper::toRecord);
    }

    @Override
    public Mono<PrincipalPolicyRecord> save(PrincipalPolicyRecord record) {
        return mongoTemplate.save(mapper.toDocument(record))
                .map(mapper::toRecord);
    }

    @Override
    public Mono<Void> deleteByPrincipalId(String principalId) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(principalId)), PrincipalPolicyDocument.class)
                .then();
    }
}
