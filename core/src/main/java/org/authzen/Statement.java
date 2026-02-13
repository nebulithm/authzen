package org.authzen;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class Statement {
    Effect effect;
    @Builder.Default
    List<String> principals = List.of();
    @Builder.Default
    List<String> notPrincipals = List.of();
    @Builder.Default
    List<String> actions = List.of();
    @Builder.Default
    List<String> resources = List.of();
    String condition;
}
