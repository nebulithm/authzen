package org.authzen;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import java.util.function.UnaryOperator;

public class StatementFactory {
    private static final JexlEngine JEXL = new JexlBuilder()
            .safe(false)
            .silent(false)
            .strict(false)
            .create();

    public static Statement create(UnaryOperator<Statement.StatementBuilder> configurator) {
        Statement.StatementBuilder builder = Statement.builder();
        builder = configurator.apply(builder);
        
        Statement statement = builder.build();
        validate(statement);
        
        return statement;
    }

    private static void validate(Statement statement) {
        if (statement.getPrincipals() == null) {
            throw new IllegalArgumentException("Principals list cannot be null");
        }
        if (statement.getNotPrincipals() == null) {
            throw new IllegalArgumentException("NotPrincipals list cannot be null");
        }
        if (statement.getActions() == null) {
            throw new IllegalArgumentException("Actions list cannot be null");
        }
        if (statement.getResources() == null) {
            throw new IllegalArgumentException("Resources list cannot be null");
        }
        
        if (statement.getCondition() != null && !statement.getCondition().isEmpty()) {
            try {
                JEXL.createExpression(statement.getCondition());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JEXL condition: " + statement.getCondition(), e);
            }
        }
    }
}
