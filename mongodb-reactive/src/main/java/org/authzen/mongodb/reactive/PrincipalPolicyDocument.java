package org.authzen.mongodb.reactive;

import lombok.Getter;
import lombok.Setter;
import org.authzen.Policy;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Document("principal_policies")
public class PrincipalPolicyDocument {
    @Id
    private String principalId;
    private Policy policy;
    private List<String> roleIds;
}
