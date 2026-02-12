package org.authzen;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import java.util.function.UnaryOperator;

public class PolicyFactory {
    private static final JexlEngine JEXL = new JexlBuilder()
            .safe(false)
            .silent(false)
            .strict(false)
            .create();

    public static Policy create(UnaryOperator<Policy.PolicyBuilder> configurator) {
        Policy.PolicyBuilder builder = Policy.builder();
        builder = configurator.apply(builder);
        
        Policy policy = builder.build();
        validate(policy);
        
        return policy;
    }

    private static void validate(Policy policy) {
        if (policy.getStatements() == null) {
            throw new IllegalArgumentException("Statements list cannot be null");
        }
        
        for (Statement statement : policy.getStatements()) {
            validateStatement(statement);
        }
    }

    private static void validateStatement(Statement statement) {
        if (statement.getPrincipals() == null) {
            throw new IllegalArgumentException("Principals list cannot be null in statement");
        }
        if (statement.getNotPrincipals() == null) {
            throw new IllegalArgumentException("NotPrincipals list cannot be null in statement");
        }
        if (statement.getActions() == null) {
            throw new IllegalArgumentException("Actions list cannot be null in statement");
        }
        if (statement.getResources() == null) {
            throw new IllegalArgumentException("Resources list cannot be null in statement");
        }
        
        if (statement.getCondition() != null && !statement.getCondition().isEmpty()) {
            try {
                JEXL.createExpression(statement.getCondition());
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid JEXL condition in statement: " + statement.getCondition(), e);
            }
        }
    }
}
