package org.authzen;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PolicyDeserializer extends JsonDeserializer<Policy> {
    @Override
    public Policy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        
        try {
            return PolicyFactory.create(builder -> {
                if (node.has("statements")) {
                    List<Statement> statements = new ArrayList<>();
                    JsonNode statementsNode = node.get("statements");
                    for (JsonNode statementNode : statementsNode) {
                        try {
                            Statement statement = p.getCodec().treeToValue(statementNode, Statement.class);
                            statements.add(statement);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    builder.statements(statements);
                }
                return builder;
            });
        } catch (IllegalArgumentException e) {
            throw JsonMappingException.from(ctxt, "Failed to deserialize Policy: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }
}
