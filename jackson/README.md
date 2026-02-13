# AuthZen Jackson Module

Jackson serialization/deserialization support for AuthZen policies and statements.

## Overview

The `authzen-jackson` module provides Jackson integration for deserializing JSON into AuthZen `Policy` and `Statement` objects. All deserialization uses the factory methods (`PolicyFactory.create()` and `StatementFactory.create()`) to ensure validation is applied.

## Maven Dependency

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.nebulithm</groupId>
    <artifactId>authzen-jackson</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Register the Module

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.authzen.AuthZenModule;

ObjectMapper mapper = new ObjectMapper();
mapper.registerModule(new AuthZenModule());
```

### Deserialize a Statement

```java
String json = """
    {
        "effect": "ALLOW",
        "principals": ["user-1", "user-2"],
        "actions": ["document:read", "document:write"],
        "resources": ["doc-*"],
        "condition": "principal.id == 'user-1'"
    }
    """;

Statement statement = mapper.readValue(json, Statement.class);
```

### Deserialize a Policy

```java
String json = """
    {
        "statements": [
            {
                "effect": "ALLOW",
                "principals": ["user-1"],
                "actions": ["document:read"],
                "resources": ["doc-*"]
            },
            {
                "effect": "DENY",
                "principals": ["user-2"],
                "actions": ["document:delete"],
                "resources": ["doc-sensitive"]
            }
        ]
    }
    """;

Policy policy = mapper.readValue(json, Policy.class);
```

## JSON Format

### Statement JSON

```json
{
  "effect": "ALLOW",
  "principals": ["user-1", "user-*"],
  "notPrincipals": ["user-blocked"],
  "actions": ["document:read", "document:write"],
  "resources": ["doc-*"],
  "condition": "context.time == 'business_hours'"
}
```

**Fields:**
- `effect` (optional): `"ALLOW"` or `"DENY"` - defaults to null if not provided
- `principals` (optional): Array of principal ID patterns - defaults to empty array
- `notPrincipals` (optional): Array of excluded principal patterns - defaults to empty array
- `actions` (optional): Array of action patterns - defaults to empty array
- `resources` (optional): Array of resource ID patterns - defaults to empty array
- `condition` (optional): JEXL expression string - defaults to null

### Policy JSON

```json
{
  "statements": [
    {
      "effect": "ALLOW",
      "principals": ["user-1"],
      "actions": ["document:*"],
      "resources": ["*"]
    }
  ]
}
```

**Fields:**
- `statements` (optional): Array of statement objects - defaults to empty array

## Validation

All deserialization goes through the factory methods, which apply validation:

1. **JEXL Condition Validation**: Invalid JEXL expressions throw `JsonMappingException`
2. **Null List Validation**: Null values for list fields throw `JsonMappingException`
3. **Nested Validation**: Policy deserialization validates all nested statements

### Example: Invalid JEXL

```java
String json = """
    {
        "effect": "ALLOW",
        "actions": ["document:read"],
        "resources": ["*"],
        "condition": "invalid @@@ syntax"
    }
    """;

// Throws JsonMappingException with message about invalid JEXL
Statement statement = mapper.readValue(json, Statement.class);
```

## Implementation Details

### Package Structure

All classes are in the `org.authzen` package (same as core) to access package-private builders:

- `AuthZenModule` - Jackson module that registers deserializers
- `StatementDeserializer` - Custom deserializer for Statement
- `PolicyDeserializer` - Custom deserializer for Policy

### Why Same Package?

The `Policy` and `Statement` classes use Lombok's `@Builder(access = lombok.AccessLevel.PACKAGE)`, making their builders package-private. The deserializers must be in the same package to access these builders through the factory methods.

### Error Handling

- Factory validation errors (`IllegalArgumentException`) are wrapped as `JsonMappingException`
- Jackson parsing errors propagate naturally
- Nested deserialization errors (e.g., invalid statement in policy) are properly wrapped

## Testing

The module includes comprehensive tests:

- **StatementDeserializerTest**: Tests for statement deserialization including validation
- **PolicyDeserializerTest**: Tests for policy deserialization with nested statements
- **AuthZenModuleTest**: Integration tests for end-to-end deserialization

Run tests:
```bash
mvn test -pl jackson
```

## Example: Complete Usage

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import org.authzen.*;

public class Example {
    public static void main(String[] args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new AuthZenModule());
        
        String policyJson = """
            {
                "statements": [
                    {
                        "effect": "ALLOW",
                        "principals": ["user-*"],
                        "actions": ["document:read"],
                        "resources": ["doc-*"],
                        "condition": "principal.id == 'user-1'"
                    }
                ]
            }
            """;
        
        Policy policy = mapper.readValue(policyJson, Policy.class);
        
        // Use with AuthZen
        Principal principal = new Principal("user-1", List.of(), policy);
        Resource resource = new Resource("doc-123", "document", null);
        
        AuthZen authZen = new AuthZen();
        Decision decision = authZen.authorize(principal, resource, "read");
        
        System.out.println("Allowed: " + decision.isAllowed());
    }
}
```
