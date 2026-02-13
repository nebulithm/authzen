package org.authzen.service.reactive;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String resourceId;

    public ResourceNotFoundException(String resourceId) {
        super("Resource not found: " + resourceId);
        this.resourceId = resourceId;
    }
}
