package org.authzen.mongodb.reactive;

import lombok.Getter;
import lombok.Setter;
import org.authzen.Policy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Getter
@Setter
@Document("role_policies")
public class RolePolicyDocument {
    @Id
    private String roleId;
    private String name;
    private Policy policy;
    private Map<String, Object> attributes;
}
