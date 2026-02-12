package org.authzen;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StatementDeserializer extends JsonDeserializer<Statement> {
    @Override
    public Statement deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
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
            throw JsonMappingException.from(ctxt, "Failed to deserialize Statement: " + e.getMessage(), e);
        }
    }
    
    private List<String> toList(JsonNode node) {
        List<String> list = new ArrayList<>();
        node.forEach(item -> list.add(item.asText()));
        return list;
    }
}
