package org.authzen.mongodb.reactive;

import org.authzen.service.reactive.RolePolicyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MongoRolePolicyRepositoryTest {

    private ReactiveMongoTemplate mongoTemplate;
    private MongoRolePolicyRepository repository;

    @BeforeEach
    void setUp() {
        mongoTemplate = Mockito.mock(ReactiveMongoTemplate.class);
        RolePolicyDocumentMapper mapper = new RolePolicyDocumentMapper() {
            @Override
            public RolePolicyRecord toRecord(RolePolicyDocument doc) {
                return new RolePolicyRecord(doc.getRoleId(), doc.getName(), doc.getPolicy(), doc.getAttributes());
            }

            @Override
            public RolePolicyDocument toDocument(RolePolicyRecord record) {
                RolePolicyDocument doc = new RolePolicyDocument();
                doc.setRoleId(record.getRoleId());
                doc.setName(record.getName());
                doc.setPolicy(record.getPolicy());
                doc.setAttributes(record.getAttributes());
                return doc;
            }
        };
        repository = new MongoRolePolicyRepository(mongoTemplate, mapper);
    }

    @Test
    void findById_shouldReturnRecord() {
        RolePolicyDocument doc = new RolePolicyDocument();
        doc.setRoleId("role-admin");
        doc.setName("Admin");
        when(mongoTemplate.findById("role-admin", RolePolicyDocument.class)).thenReturn(Mono.just(doc));

        StepVerifier.create(repository.findById("role-admin"))
                .assertNext(r -> {
                    assertEquals("role-admin", r.getRoleId());
                    assertEquals("Admin", r.getName());
                })
                .verifyComplete();
    }

    @Test
    void findByIds_shouldReturnMultipleRecords() {
        RolePolicyDocument doc1 = new RolePolicyDocument();
        doc1.setRoleId("role-admin");
        doc1.setName("Admin");
        RolePolicyDocument doc2 = new RolePolicyDocument();
        doc2.setRoleId("role-editor");
        doc2.setName("Editor");
        when(mongoTemplate.find(any(Query.class), eq(RolePolicyDocument.class)))
                .thenReturn(Flux.just(doc1, doc2));

        StepVerifier.create(repository.findByIds(List.of("role-admin", "role-editor")))
                .assertNext(r -> assertEquals("role-admin", r.getRoleId()))
                .assertNext(r -> assertEquals("role-editor", r.getRoleId()))
                .verifyComplete();
    }

    @Test
    void findByIds_shouldReturnEmpty_whenListEmpty() {
        StepVerifier.create(repository.findByIds(List.of())).verifyComplete();
    }

    @Test
    void save_shouldPersistAndReturnRecord() {
        RolePolicyRecord record = new RolePolicyRecord("role-admin", "Admin", null, Map.of());
        RolePolicyDocument doc = new RolePolicyDocument();
        doc.setRoleId("role-admin");
        doc.setName("Admin");
        when(mongoTemplate.save(any(RolePolicyDocument.class))).thenReturn(Mono.just(doc));

        StepVerifier.create(repository.save(record))
                .assertNext(r -> assertEquals("role-admin", r.getRoleId()))
                .verifyComplete();
    }

    @Test
    void deleteById_shouldRemoveDocument() {
        when(mongoTemplate.remove(any(Query.class), eq(RolePolicyDocument.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(repository.deleteById("role-admin")).verifyComplete();
    }
}
