# AuthZen WebFlux Service Example

A runnable Spring Boot WebFlux application demonstrating how to wire AuthZen library modules together for a policy evaluation service.

## What This Demonstrates

- **Custom DTOs**: `AuthorizeRequest`/`AuthorizeResponse` with consumer-defined field names (not constrained by the library)
- **Extended Principal**: `ExamplePrincipal` adds a `department` field
- **Type-safe attributes**: `ExampleAttributes` with `department` and `clearanceLevel`
- **PrincipalFactory**: Maps attributes + roles + policy into the custom principal
- **ContextFactory**: Extracts client IP from the HTTP request
- **MongoDB repositories**: Wired via `MongoResourceRepository` and `MongoPrincipalPolicyRepository` with custom document mappers
- **Kafka consumers**: `ResourceEventConsumer` and `PrincipalPolicyEventConsumer` with custom message mappers
- **No autoconfiguration**: All beans are explicitly wired in `AppConfig` and `KafkaConfig`

## Dependencies

This project pulls AuthZen libraries from JitPack:

- `authzen-service-reactive` — reactive service abstractions
- `authzen-kafka-reactive` — Kafka consumer adapter
- `authzen-mongodb-reactive` — MongoDB repository adapter

## Running Locally

Start infrastructure:

```bash
docker-compose up -d
```

Run the application:

```bash
mvn spring-boot:run
```

Test the endpoint:

```bash
curl -X POST http://localhost:8080/api/v1/authorize \
  -H "Content-Type: application/json" \
  -d '{
    "userId": "user-1",
    "userAttributes": {"department": "engineering", "clearanceLevel": 5},
    "targetResourceId": "doc-1",
    "requestedAction": "read"
  }'
```

## Running Integration Tests

Tests use Testcontainers (Docker required) — no manual infrastructure setup needed:

```bash
mvn test
```

Two test groups:
- **KafkaIngestionIntegrationTest** — verifies Kafka messages are consumed and persisted/deleted in MongoDB
- **AuthorizationIntegrationTest** — verifies authorization decisions for various policy configurations

## Project Structure

```
src/main/java/org/authzen/examples/webflux/
├── ExampleApplication.java              # Spring Boot entry point
├── config/
│   ├── AppConfig.java                   # Bean wiring (AuthZen, repositories, service)
│   └── KafkaConfig.java                 # Kafka producer/consumer factories
├── domain/
│   ├── ExamplePrincipal.java            # Extended Principal with department
│   ├── ExampleAttributes.java           # Type-safe attributes DTO
│   ├── ExamplePrincipalFactory.java     # PrincipalFactory implementation
│   └── ExampleContextFactory.java       # ContextFactory implementation
├── kafka/
│   ├── ResourceKafkaMessage.java        # Kafka message shape for resources
│   ├── PrincipalPolicyKafkaMessage.java # Kafka message shape for policies
│   ├── ExampleResourceEventConsumer.java
│   ├── ExamplePrincipalPolicyEventConsumer.java
│   └── KafkaListeners.java             # @KafkaListener wiring
├── persistence/
│   ├── ExampleResourceDocumentMapper.java
│   └── ExamplePrincipalPolicyDocumentMapper.java
└── web/
    ├── AuthorizeRequest.java            # Consumer-defined request DTO
    ├── AuthorizeResponse.java           # Consumer-defined response DTO
    └── AuthorizationController.java     # REST controller
```
