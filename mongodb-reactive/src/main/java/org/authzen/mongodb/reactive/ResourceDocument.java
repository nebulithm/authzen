package org.authzen.mongodb.reactive;

import lombok.Getter;
import lombok.Setter;
import org.authzen.Policy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document("resources")
public class ResourceDocument {
    @Id
    private String id;
    private String type;
    private Policy policy;
}
