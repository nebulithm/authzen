package org.authzen;

/**
 * Core authorization policy engine for AWS-style policy evaluation.
 * This module is authentication-agnostic and focuses solely on policy-based authorization.
 */
public class AuthZen {
    private final AuthorizationEngine engine = new AuthorizationEngine();

    public Decision authorize(Principal principal, Resource resource, String action) {
        return engine.evaluate(principal, resource, action, null);
    }

    public Decision authorize(Principal principal, Resource resource, String action, Object context) {
        return engine.evaluate(principal, resource, action, context);
    }
}
