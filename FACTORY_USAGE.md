# Factory Pattern Usage

## Creating Statements and Policies

Consumers **must** use the factory classes to create statements and policies. Direct access to builders is restricted.

### Creating a Statement

```java
// ✅ Correct - Use StatementFactory
Statement statement = StatementFactory.create(builder -> builder
    .effect(Effect.ALLOW)
    .principals(List.of("user-1"))
    .actions(List.of("document:read"))
    .resources(List.of("*"))
    .condition("context.time == 'business_hours'")
);

// ❌ Incorrect - Statement.builder() is not accessible outside package
Statement statement = Statement.builder()  // Compilation error!
    .effect(Effect.ALLOW)
    .build();
```

### Creating a Policy

```java
// ✅ Correct - Use PolicyFactory
Policy policy = PolicyFactory.create(builder -> builder
    .statements(List.of(
        StatementFactory.create(b -> b
            .effect(Effect.ALLOW)
            .principals(List.of("user-1"))
            .actions(List.of("document:read"))
            .resources(List.of("*"))
        )
    ))
);

// ❌ Incorrect - Policy.builder() is not accessible outside package
Policy policy = Policy.builder()  // Compilation error!
    .statements(List.of(...))
    .build();
```

### Benefits

1. **Validation**: All JEXL conditions are validated at creation time
2. **Consistency**: Single entry point for creating policies and statements
3. **Safety**: Invalid policies/statements cannot be created
4. **Extensibility**: Easy to add more validation rules in the factory

### Complete Example

```java
AuthZen authZen = new AuthZen();

// Create policy with validated statements
Policy principalPolicy = PolicyFactory.create(builder -> builder
    .statements(List.of(
        StatementFactory.create(b -> b
            .effect(Effect.ALLOW)
            .principals(List.of("user-1"))
            .actions(List.of("document:read", "document:write"))
            .resources(List.of("doc-*"))
            .condition("context.time == 'business_hours'")
        )
    ))
);

Policy resourcePolicy = PolicyFactory.create(builder -> builder
    .statements(List.of(
        StatementFactory.create(b -> b
            .effect(Effect.ALLOW)
            .principals(List.of("user-1"))
            .actions(List.of("document:*"))
            .resources(List.of("doc-123"))
        )
    ))
);

Principal principal = new Principal("user-1", List.of(), principalPolicy);
Resource resource = new Resource("doc-123", "document", resourcePolicy);

Decision decision = authZen.authorize(principal, resource, "read", 
    Map.of("time", "business_hours"));
```
