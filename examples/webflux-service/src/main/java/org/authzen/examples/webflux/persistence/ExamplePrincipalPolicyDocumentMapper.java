package org.authzen.examples.webflux.persistence;

import org.authzen.mongodb.reactive.PrincipalPolicyDocument;
import org.authzen.mongodb.reactive.PrincipalPolicyDocumentMapper;
import org.authzen.service.reactive.PrincipalPolicyRecord;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExamplePrincipalPolicyDocumentMapper implements PrincipalPolicyDocumentMapper {

    @Override
    public PrincipalPolicyRecord toRecord(PrincipalPolicyDocument document) {
        return new PrincipalPolicyRecord(
                document.getPrincipalId(),
                document.getPolicy(),
                document.getRoleIds() != null ? document.getRoleIds() : List.of());
    }

    @Override
    public PrincipalPolicyDocument toDocument(PrincipalPolicyRecord record) {
        PrincipalPolicyDocument doc = new PrincipalPolicyDocument();
        doc.setPrincipalId(record.getPrincipalId());
        doc.setPolicy(record.getPolicy());
        doc.setRoleIds(record.getRoleIds());
        return doc;
    }
}
