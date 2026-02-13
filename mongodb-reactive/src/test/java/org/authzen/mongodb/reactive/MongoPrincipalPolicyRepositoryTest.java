package org.authzen.mongodb.reactive;

import org.authzen.service.reactive.PrincipalPolicyRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class MongoPrincipalPolicyRepositoryTest {

    private ReactiveMongoTemplate mongoTemplate;
    private MongoPrincipalPolicyRepository repository;

    @BeforeEach
    void setUp() {
        mongoTemplate = Mockito.mock(ReactiveMongoTemplate.class);
        PrincipalPolicyDocumentMapper mapper = new PrincipalPolicyDocumentMapper() {
            @Override
            public PrincipalPolicyRecord toRecord(PrincipalPolicyDocument doc) {
                return new PrincipalPolicyRecord(doc.getPrincipalId(), doc.getPolicy(), doc.getRoleIds() != null ? doc.getRoleIds() : List.of());
            }

            @Override
            public PrincipalPolicyDocument toDocument(PrincipalPolicyRecord record) {
                PrincipalPolicyDocument doc = new PrincipalPolicyDocument();
                doc.setPrincipalId(record.getPrincipalId());
                doc.setPolicy(record.getPolicy());
                doc.setRoleIds(record.getRoleIds());
                return doc;
            }
        };
        repository = new MongoPrincipalPolicyRepository(mongoTemplate, mapper);
    }

    @Test
    void findByPrincipalId_shouldReturnRecord() {
        PrincipalPolicyDocument doc = new PrincipalPolicyDocument();
        doc.setPrincipalId("p-1");
        when(mongoTemplate.findById("p-1", PrincipalPolicyDocument.class)).thenReturn(Mono.just(doc));

        StepVerifier.create(repository.findByPrincipalId("p-1"))
                .assertNext(r -> assertEquals("p-1", r.getPrincipalId()))
                .verifyComplete();
    }

    @Test
    void save_shouldPersistAndReturnRecord() {
        PrincipalPolicyRecord record = new PrincipalPolicyRecord("p-1", null, List.of());
        PrincipalPolicyDocument doc = new PrincipalPolicyDocument();
        doc.setPrincipalId("p-1");
        doc.setRoleIds(List.of());
        when(mongoTemplate.save(any(PrincipalPolicyDocument.class))).thenReturn(Mono.just(doc));

        StepVerifier.create(repository.save(record))
                .assertNext(r -> assertEquals("p-1", r.getPrincipalId()))
                .verifyComplete();
    }

    @Test
    void deleteByPrincipalId_shouldRemoveDocument() {
        when(mongoTemplate.remove(any(Query.class), eq(PrincipalPolicyDocument.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(repository.deleteByPrincipalId("p-1")).verifyComplete();
    }
}
