package org.authzen.mongodb.reactive;

import org.authzen.service.reactive.RolePolicyRecord;

public interface RolePolicyDocumentMapper {
    RolePolicyRecord toRecord(RolePolicyDocument document);
    RolePolicyDocument toDocument(RolePolicyRecord record);
}
