package org.authzen;

import java.util.*;
import java.util.stream.Collectors;

public class AuthorizationEngine {
    private final PatternMatcher patternMatcher = new PatternMatcher();
    private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

    public Decision evaluate(Principal principal, Resource resource, String action, Object context) {
        String fullAction = resource.getType() + ":" + action;
        
        List<Statement> identityStatements = collectIdentityStatements(principal);
        List<Statement> resourceStatements = collectResourceStatements(resource);
        
        List<Statement> matchedIdentityStatements = evaluateStatements(identityStatements, fullAction, resource.getId(), 
                principal, resource, action, context, true);
        List<Statement> matchedResourceStatements = evaluateStatements(resourceStatements, fullAction, resource.getId(), 
                principal, resource, action, context, false);
        
        boolean identityDeny = matchedIdentityStatements.stream().anyMatch(s -> s.getEffect() == Effect.DENY);
        boolean resourceDeny = matchedResourceStatements.stream().anyMatch(s -> s.getEffect() == Effect.DENY);
        
        if (identityDeny || resourceDeny) {
            return new Decision(false, "Explicit deny", 
                mergeStatements(matchedIdentityStatements, matchedResourceStatements));
        }
        
        boolean identityAllow = matchedIdentityStatements.stream().anyMatch(s -> s.getEffect() == Effect.ALLOW);
        boolean resourceAllow = matchedResourceStatements.stream().anyMatch(s -> s.getEffect() == Effect.ALLOW);
        
        if (identityAllow && resourceAllow) {
            return new Decision(true, "Both identity and resource policies allow", 
                mergeStatements(matchedIdentityStatements, matchedResourceStatements));
        }
        
        return new Decision(false, "No matching allow policies from both identity and resource", 
            mergeStatements(matchedIdentityStatements, matchedResourceStatements));
    }

    private List<Statement> collectIdentityStatements(Principal principal) {
        List<Statement> statements = new ArrayList<>();
        if (principal.getPolicy() != null) {
            statements.addAll(principal.getPolicy().getStatements());
        }
        for (Role role : principal.getRoles()) {
            if (role.getPolicy() != null) {
                statements.addAll(role.getPolicy().getStatements());
            }
        }
        return statements;
    }

    private List<Statement> collectResourceStatements(Resource resource) {
        if (resource.getPolicy() != null) {
            return new ArrayList<>(resource.getPolicy().getStatements());
        }
        return new ArrayList<>();
    }

    private List<Statement> evaluateStatements(List<Statement> statements, String action, String resourceId, 
                                               Principal principal, Resource resource, String actionName, 
                                               Object context, boolean isIdentityPolicy) {
        List<Statement> matched = new ArrayList<>();
        
        for (Statement statement : statements) {
            if (matchesAction(statement, action) && 
                matchesResource(statement, resourceId) &&
                matchesPrincipalContext(statement, principal, isIdentityPolicy)) {
                
                Map<String, Object> evalContext = buildContext(principal, resource, actionName, context);
                if (conditionEvaluator.evaluate(statement.getCondition(), evalContext)) {
                    matched.add(statement);
                }
            }
        }
        
        return matched;
    }

    private boolean matchesAction(Statement statement, String action) {
        if (statement.getActions().isEmpty()) {
            return false;
        }
        return statement.getActions().stream().anyMatch(pattern -> patternMatcher.matches(pattern, action));
    }

    private boolean matchesResource(Statement statement, String resourceId) {
        if (statement.getResources().isEmpty()) {
            return false;
        }
        return statement.getResources().stream().anyMatch(pattern -> patternMatcher.matches(pattern, resourceId));
    }

    private boolean matchesPrincipalContext(Statement statement, Principal principal, boolean isIdentityPolicy) {
        if (statement.getPrincipals().isEmpty()) {
            return !isIdentityPolicy;
        }

        List<String> principalIds = new ArrayList<>();
        principalIds.add(principal.getId());
        principal.getRoles().forEach(role -> principalIds.add(role.getId()));

        boolean matchesPrincipal = principalIds.stream()
                .anyMatch(id -> statement.getPrincipals().stream()
                        .anyMatch(pattern -> patternMatcher.matches(pattern, id)));

        if (!matchesPrincipal) {
            return false;
        }

        if (!statement.getNotPrincipals().isEmpty()) {
            boolean matchesNotPrincipal = principalIds.stream()
                    .anyMatch(id -> statement.getNotPrincipals().stream()
                            .anyMatch(pattern -> patternMatcher.matches(pattern, id)));
            if (matchesNotPrincipal) {
                return false;
            }
        }

        return true;
    }

    private Map<String, Object> buildContext(Principal principal, Resource resource, String action, Object context) {
        Map<String, Object> evalContext = new HashMap<>();
        evalContext.put("principal", principal);
        evalContext.put("resource", resource);
        evalContext.put("action", action);
        if (context != null) {
            evalContext.put("context", context);
        }
        return evalContext;
    }

    private List<Statement> mergeStatements(List<Statement> list1, List<Statement> list2) {
        List<Statement> merged = new ArrayList<>(list1);
        merged.addAll(list2);
        return merged;
    }
}
