package org.authzen.service.reactive;

import lombok.Value;
import org.authzen.Policy;
import java.util.List;

@Value
public class PrincipalPolicyRecord {
    String principalId;
    Policy policy;
    List<String> roleIds;
}
