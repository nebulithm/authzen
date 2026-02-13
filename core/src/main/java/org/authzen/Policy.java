package org.authzen;

import lombok.Builder;
import lombok.Value;
import java.util.List;

@Value
@Builder
public class Policy {
    @Builder.Default
    List<Statement> statements = List.of();
}
