package org.authzen.mongodb.reactive;

import org.authzen.service.reactive.PrincipalPolicyRecord;

public interface PrincipalPolicyDocumentMapper {
    PrincipalPolicyRecord toRecord(PrincipalPolicyDocument document);
    PrincipalPolicyDocument toDocument(PrincipalPolicyRecord record);
}
