package org.authzen.examples.webflux.persistence;

import org.authzen.Resource;
import org.authzen.mongodb.reactive.ResourceDocument;
import org.authzen.mongodb.reactive.ResourceDocumentMapper;
import org.springframework.stereotype.Component;

@Component
public class ExampleResourceDocumentMapper implements ResourceDocumentMapper<Resource> {

    @Override
    public Resource toResource(ResourceDocument document) {
        return new Resource(document.getId(), document.getType(), document.getPolicy());
    }

    @Override
    public ResourceDocument toDocument(Resource resource) {
        ResourceDocument doc = new ResourceDocument();
        doc.setId(resource.getId());
        doc.setType(resource.getType());
        doc.setPolicy(resource.getPolicy());
        return doc;
    }
}
