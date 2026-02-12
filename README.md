# AuthZen - AWS-Style Authorization Library

AuthZen is a Java library for attribute-based authorization with AWS IAM-style dual policy evaluation.

## Features

- **Statement-Based Policies**: AWS IAM-style policies with multiple statements
- **Dual Policy Model**: Identity-based and resource-based policies
- **Principal Matching**: Statements can specify which principals they apply to with wildcard support
- **Effect Control**: ALLOW or DENY effects with explicit deny precedence
- **Wildcard Patterns**: Support for `*`, `?`, `[abc]`, `[a-z]` in actions, resources, and principals
- **JEXL Conditions**: Dynamic condition evaluation with access to principal, resource, action, and context
- **Extensible Entities**: Principal and Resource classes can be extended with custom typed properties
- **Role-Based Access**: Principals can have roles with attached policies
- **Default Deny**: Secure by default - both identity and resource policies must explicitly allow

## Quick Start

### Maven Dependency

Add JitPack repository:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add dependency:
```xml
<dependency>
    <groupId>com.github.danieledefrancesco</groupId>
    <artifactId>authzen-core</artifactId>
    <version>v1.0.0</version>
</dependency>
```

### Basic Usage

```java
AuthZen authZen = new AuthZen();

// Create statements
Statement principalStatement = Statement.builder()
        .effect(Effect.ALLOW)
        .principals(List.of("user-1"))
        .actions(List.of("document:read", "document:write"))
        .resources(List.of("doc-*"))
        .build();

Statement resourceStatement = Statement.builder()
        .effect(Effect.ALLOW)
        .principals(List.of("user-1"))
        .actions(List.of("document:*"))
        .resources(List.of("doc-123"))
        .build();

// Create policies with statements
Policy principalPolicy = Policy.builder()
        .statements(List.of(principalStatement))
        .build();

Policy resourcePolicy = Policy.builder()
        .statements(List.of(resourceStatement))
        .build();

// Create principal and resource
Principal principal = new Principal("user-1", List.of(), principalPolicy);
Resource resource = new Resource("doc-123", "document", resourcePolicy);

// Evaluate authorization
Decision decision = authZen.authorize(principal, resource, "read");
System.out.println("Allowed: " + decision.isAllowed());
System.out.println("Reason: " + decision.getReason());
```

## Core Concepts

### Statement Structure

A statement consists of:
- **Effect**: `ALLOW` or `DENY`
- **Principals**: List of principal ID patterns (e.g., `"user-1"`, `"user-*"`, `"role-admin"`)
- **NotPrincipals** (optional): List of principal ID patterns to exclude
- **Actions**: List of action patterns (e.g., `"document:read"`, `"*:write"`, `"file:*"`)
- **Resources**: List of resource ID patterns (e.g., `"doc-123"`, `"file-*"`, `"*"`)
- **Condition** (optional): JEXL expression for dynamic evaluation

### Policy Structure

A policy contains a list of statements. Each principal, resource, and role has a single inline policy.

### Dual Policy Evaluation

Authorization requires **both** identity-based and resource-based policies to explicitly allow:

1. **Identity-based policies**: Attached to principals or roles
   - Statement's `principals` field must match the policy owner's ID or role IDs
2. **Resource-based policies**: Attached to resources
   - Statement's `principals` field must match the requesting principal's ID or role IDs

**Evaluation rules:**
- Explicit `DENY` always wins (from either policy type)
- Both policy types must have at least one `ALLOW` for access to be granted
- No matching policies = implicit deny

### Wildcard Patterns

Actions, resources, and principals support glob patterns:
- `*` - matches zero or more characters
- `?` - matches exactly one character
- `[abc]` - matches one character from the set
- `[a-z]` - matches one character from the range

Examples:
- `document:*` - all document actions
- `*:read` - read action on any resource type
- `user-*` - all principals starting with "user-"
- `file-[0-9]*` - files starting with a digit

### Principal Matching

**Identity Policies:**
- Statement applies only if the `principals` field includes the policy owner's ID or role IDs
- Allows principals to delegate permissions to themselves

**Resource Policies:**
- Statement applies only if the `principals` field includes the requesting principal's ID or role IDs
- Controls which principals can access the resource

**NotPrincipals:**
- Excludes specific principals from a statement
- Supports wildcard matching

### JEXL Conditions

Conditions are evaluated with access to:
- `principal` - the Principal object
- `resource` - the Resource object
- `action` - the action string
- `context` - custom context (Map)

Example:
```java
Statement statement = Statement.builder()
        .effect(Effect.ALLOW)
        .principals(List.of("user-*"))
        .actions(List.of("document:*"))
        .resources(List.of("*"))
        .condition("context.time == 'business_hours'")
        .build();

Policy policy = Policy.builder()
        .statements(List.of(statement))
        .build();

Map<String, Object> context = Map.of("time", "business_hours");
Decision decision = authZen.authorize(principal, resource, "read", context);
```

### Extensibility

Principal and Resource classes can be extended with custom properties:

```java
public class CustomPrincipal extends Principal {
    private final String department;
    
    public CustomPrincipal(String id, String department, List<Role> roles, Policy policy) {
        super(id, roles, policy);
        this.department = department;
    }
    
    public String getDepartment() {
        return department;
    }
}

// Use custom principal with the authorization engine
CustomPrincipal principal = new CustomPrincipal("user-1", "engineering", List.of(), policy);
Decision decision = authZen.authorize(principal, resource, "read");
```

### Roles

Principals can have roles, and roles can have policies:

```java
Statement roleStatement = Statement.builder()
        .effect(Effect.ALLOW)
        .principals(List.of("role-admin"))
        .actions(List.of("document:*"))
        .resources(List.of("*"))
        .build();

Policy rolePolicy = Policy.builder()
        .statements(List.of(roleStatement))
        .build();

Role adminRole = new Role("admin", "Administrator", rolePolicy, Map.of());
Principal principal = new Principal("user-1", List.of(adminRole), null);
```

## Examples

See `AuthZenIntegrationTest.java` for comprehensive examples including:
1. Basic authorization with dual policies
2. Explicit deny precedence
3. Wildcard pattern matching
4. JEXL conditions with context
5. Role-based policies
6. Default deny behavior
7. Custom principal/resource extensibility
8. Principal matching (exact and wildcard)
9. NotPrincipals exclusion
10. Identity and resource policy principal validation

## Running Tests

```bash
mvn test
```

## Architecture

```
org.authzen
├── AuthZen - Main entry point
├── AuthorizationEngine - Core evaluation logic
├── Principal - Extensible principal entity (formerly User)
├── Resource - Extensible resource entity
├── Role - Role entity with policy and attributes
├── Policy - Container for statements
├── Statement - Atomic authorization rule
├── Effect - ALLOW or DENY enum
├── Decision - Authorization decision result
├── ConditionEvaluator - JEXL expression evaluator
└── PatternMatcher - Glob pattern matcher
```

## License

Apache License 2.0
