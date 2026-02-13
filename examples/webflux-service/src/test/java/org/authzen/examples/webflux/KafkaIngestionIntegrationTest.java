package org.authzen.examples.webflux;

import org.authzen.examples.webflux.kafka.PrincipalPolicyKafkaMessage;
import org.authzen.examples.webflux.kafka.ResourceKafkaMessage;
import org.authzen.examples.webflux.kafka.RolePolicyKafkaMessage;
import org.authzen.mongodb.reactive.PrincipalPolicyDocument;
import org.authzen.mongodb.reactive.ResourceDocument;
import org.authzen.mongodb.reactive.RolePolicyDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

class KafkaIngestionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private ReactiveMongoTemplate mongoTemplate;

    @BeforeEach
    void cleanDb() {
        mongoTemplate.dropCollection(ResourceDocument.class).block();
        mongoTemplate.dropCollection(PrincipalPolicyDocument.class).block();
        mongoTemplate.dropCollection(RolePolicyDocument.class).block();
    }

    // --- Resource events ---

    @Test
    void resourceConsumer_shouldPersistResource_whenCreateEventReceived() {
        kafkaTemplate.send("resource-events", new ResourceKafkaMessage("CREATE", "res-1", "document", null));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ResourceDocument doc = mongoTemplate.findById("res-1", ResourceDocument.class).block();
            assertNotNull(doc);
            assertEquals("document", doc.getType());
        });
    }

    @Test
    void resourceConsumer_shouldUpdateResource_whenUpdateEventReceived() {
        kafkaTemplate.send("resource-events", new ResourceKafkaMessage("CREATE", "res-2", "document", null));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNotNull(mongoTemplate.findById("res-2", ResourceDocument.class).block()));

        kafkaTemplate.send("resource-events", new ResourceKafkaMessage("UPDATE", "res-2", "file", null));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            ResourceDocument doc = mongoTemplate.findById("res-2", ResourceDocument.class).block();
            assertNotNull(doc);
            assertEquals("file", doc.getType());
        });
    }

    @Test
    void resourceConsumer_shouldRemoveResource_whenDeleteEventReceived() {
        kafkaTemplate.send("resource-events", new ResourceKafkaMessage("CREATE", "res-3", "document", null));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNotNull(mongoTemplate.findById("res-3", ResourceDocument.class).block()));

        kafkaTemplate.send("resource-events", new ResourceKafkaMessage("DELETE", "res-3", "document", null));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNull(mongoTemplate.findById("res-3", ResourceDocument.class).block()));
    }

    // --- Principal policy events ---

    @Test
    void principalPolicyConsumer_shouldPersistPolicy_whenCreateEventReceived() {
        kafkaTemplate.send("principal-policy-events",
                new PrincipalPolicyKafkaMessage("CREATE", "p-1", null, List.of("role-admin")));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            PrincipalPolicyDocument doc = mongoTemplate.findById("p-1", PrincipalPolicyDocument.class).block();
            assertNotNull(doc);
            assertEquals(List.of("role-admin"), doc.getRoleIds());
        });
    }

    @Test
    void principalPolicyConsumer_shouldUpdatePolicy_whenUpdateEventReceived() {
        kafkaTemplate.send("principal-policy-events",
                new PrincipalPolicyKafkaMessage("CREATE", "p-2", null, List.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNotNull(mongoTemplate.findById("p-2", PrincipalPolicyDocument.class).block()));

        kafkaTemplate.send("principal-policy-events",
                new PrincipalPolicyKafkaMessage("UPDATE", "p-2", null, List.of("role-editor")));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            PrincipalPolicyDocument doc = mongoTemplate.findById("p-2", PrincipalPolicyDocument.class).block();
            assertNotNull(doc);
            assertEquals(List.of("role-editor"), doc.getRoleIds());
        });
    }

    @Test
    void principalPolicyConsumer_shouldRemovePolicy_whenDeleteEventReceived() {
        kafkaTemplate.send("principal-policy-events",
                new PrincipalPolicyKafkaMessage("CREATE", "p-3", null, List.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNotNull(mongoTemplate.findById("p-3", PrincipalPolicyDocument.class).block()));

        kafkaTemplate.send("principal-policy-events",
                new PrincipalPolicyKafkaMessage("DELETE", "p-3", null, List.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNull(mongoTemplate.findById("p-3", PrincipalPolicyDocument.class).block()));
    }

    // --- Role policy events ---

    @Test
    void rolePolicyConsumer_shouldPersistRole_whenCreateEventReceived() {
        kafkaTemplate.send("role-policy-events",
                new RolePolicyKafkaMessage("CREATE", "role-admin", "Admin", null, Map.of("level", 10)));

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            RolePolicyDocument doc = mongoTemplate.findById("role-admin", RolePolicyDocument.class).block();
            assertNotNull(doc);
            assertEquals("Admin", doc.getName());
            assertEquals(10, doc.getAttributes().get("level"));
        });
    }

    @Test
    void rolePolicyConsumer_shouldUpdateRole_whenUpdateEventReceived() {
        kafkaTemplate.send("role-policy-events",
                new RolePolicyKafkaMessage("CREATE", "role-ed", "Editor", null, Map.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNotNull(mongoTemplate.findById("role-ed", RolePolicyDocument.class).block()));

        kafkaTemplate.send("role-policy-events",
                new RolePolicyKafkaMessage("UPDATE", "role-ed", "Senior Editor", null, Map.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            RolePolicyDocument doc = mongoTemplate.findById("role-ed", RolePolicyDocument.class).block();
            assertNotNull(doc);
            assertEquals("Senior Editor", doc.getName());
        });
    }

    @Test
    void rolePolicyConsumer_shouldRemoveRole_whenDeleteEventReceived() {
        kafkaTemplate.send("role-policy-events",
                new RolePolicyKafkaMessage("CREATE", "role-del", "Temp", null, Map.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNotNull(mongoTemplate.findById("role-del", RolePolicyDocument.class).block()));

        kafkaTemplate.send("role-policy-events",
                new RolePolicyKafkaMessage("DELETE", "role-del", "Temp", null, Map.of()));
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                assertNull(mongoTemplate.findById("role-del", RolePolicyDocument.class).block()));
    }
}
