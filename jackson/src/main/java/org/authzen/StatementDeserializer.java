package org.authzen;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.ArrayList;
import java.util.List;

public class StatementDeserializer extends ValueDeserializer<Statement> {
    @Override
    public Statement deserialize(JsonParser p, DeserializationContext ctxt) {
        JsonNode node = p.readValueAsTree();

        try {
            return StatementFactory.create(builder -> {
                if (node.has("effect")) {
                    builder.effect(Effect.valueOf(node.get("effect").asText()));
                }
                if (node.has("principals")) {
                    builder.principals(toList(node.get("principals")));
                }
                if (node.has("notPrincipals")) {
                    builder.notPrincipals(toList(node.get("notPrincipals")));
                }
                if (node.has("actions")) {
                    builder.actions(toList(node.get("actions")));
                }
                if (node.has("resources")) {
                    builder.resources(toList(node.get("resources")));
                }
                if (node.has("condition")) {
                    builder.condition(node.get("condition").asText());
                }
                return builder;
            });
        } catch (IllegalArgumentException e) {
            throw new tools.jackson.core.JacksonException("Failed to deserialize Statement: " + e.getMessage()) {};
        }
    }

    private List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<>();
        node.forEach(item -> list.add(item.asText()));
        return list;
    }
}
