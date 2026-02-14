package org.authzen;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

public class PolicyDeserializer extends ValueDeserializer<Policy> {
    @Override
    public Policy deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonNode node = p.readValueAsTree();

        try {
            return PolicyFactory.create(builder -> {
                if (node.has("statements")) {
                    List<Statement> statements = new ArrayList<>();
                    JsonNode statementsNode = node.get("statements");
                    for (JsonNode statementNode : statementsNode) {
                        Statement statement = ctxt.readTreeAsValue(statementNode, Statement.class);
                        statements.add(statement);
                    }
                    builder.statements(statements);
                }
                return builder;
            });
        } catch (IllegalArgumentException e) {
            throw new tools.jackson.core.JacksonException("Failed to deserialize Policy: " + e.getMessage()) {};
        }
    }
}
