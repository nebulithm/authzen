package org.authzen;

import lombok.Value;
import java.util.List;

@Value
public class Decision {
    boolean allowed;
    String reason;
    List<Statement> matchedStatements;
}
