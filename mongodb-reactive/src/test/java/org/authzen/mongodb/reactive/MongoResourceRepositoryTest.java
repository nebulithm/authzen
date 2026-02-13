package org.authzen.mongodb.reactive;

import org.authzen.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MongoResourceRepositoryTest {

    private ReactiveMongoTemplate mongoTemplate;
    private MongoResourceRepository<Resource> repository;

    @BeforeEach
    void setUp() {
        mongoTemplate = Mockito.mock(ReactiveMongoTemplate.class);
        ResourceDocumentMapper<Resource> mapper = new ResourceDocumentMapper<>() {
            @Override
            public Resource toResource(ResourceDocument doc) {
                return new Resource(doc.getId(), doc.getType(), doc.getPolicy());
            }

            @Override
            public ResourceDocument toDocument(Resource resource) {
                ResourceDocument doc = new ResourceDocument();
                doc.setId(resource.getId());
                doc.setType(resource.getType());
                doc.setPolicy(resource.getPolicy());
                return doc;
            }
        };
        repository = new MongoResourceRepository<>(mongoTemplate, mapper);
    }

    @Test
    void findByIdReturnsResource() {
        ResourceDocument doc = new ResourceDocument();
        doc.setId("r-1");
        doc.setType("document");
        when(mongoTemplate.findById("r-1", ResourceDocument.class)).thenReturn(Mono.just(doc));

        StepVerifier.create(repository.findById("r-1"))
                .assertNext(r -> {
                    assertEquals("r-1", r.getId());
                    assertEquals("document", r.getType());
                })
                .verifyComplete();
    }

    @Test
    void savePersistsAndReturnsResource() {
        Resource resource = new Resource("r-1", "document");
        ResourceDocument doc = new ResourceDocument();
        doc.setId("r-1");
        doc.setType("document");
        when(mongoTemplate.save(any(ResourceDocument.class))).thenReturn(Mono.just(doc));

        StepVerifier.create(repository.save(resource))
                .assertNext(r -> assertEquals("r-1", r.getId()))
                .verifyComplete();
    }

    @Test
    void deleteByIdRemovesDocument() {
        when(mongoTemplate.remove(any(Query.class), eq(ResourceDocument.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(repository.deleteById("r-1")).verifyComplete();
    }
}
