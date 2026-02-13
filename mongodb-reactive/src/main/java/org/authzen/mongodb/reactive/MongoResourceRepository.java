package org.authzen.mongodb.reactive;

import lombok.RequiredArgsConstructor;
import org.authzen.Resource;
import org.authzen.service.reactive.ResourceRepository;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class MongoResourceRepository<R extends Resource> implements ResourceRepository<R> {

    private final ReactiveMongoTemplate mongoTemplate;
    private final ResourceDocumentMapper<R> mapper;

    @Override
    public Mono<R> findById(String id) {
        return mongoTemplate.findById(id, ResourceDocument.class)
                .map(mapper::toResource);
    }

    @Override
    public Mono<R> save(R resource) {
        return mongoTemplate.save(mapper.toDocument(resource))
                .map(mapper::toResource);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoTemplate.remove(Query.query(Criteria.where("_id").is(id)), ResourceDocument.class)
                .then();
    }
}
