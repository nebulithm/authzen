package org.authzen.mongodb.reactive;

import org.authzen.Resource;

public interface ResourceDocumentMapper<R extends Resource> {
    R toResource(ResourceDocument document);
    ResourceDocument toDocument(R resource);
}
