package org.authzen.examples.webflux.persistence;

import org.authzen.mongodb.reactive.RolePolicyDocument;
import org.authzen.mongodb.reactive.RolePolicyDocumentMapper;
import org.authzen.service.reactive.RolePolicyRecord;
import org.springframework.stereotype.Component;

@Component
public class ExampleRolePolicyDocumentMapper implements RolePolicyDocumentMapper {

    @Override
    public RolePolicyRecord toRecord(RolePolicyDocument document) {
        return new RolePolicyRecord(document.getRoleId(), document.getName(), document.getPolicy(), document.getAttributes());
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
}
