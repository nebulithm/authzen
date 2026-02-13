package org.authzen.examples.webflux.config;

import org.authzen.AuthZen;
import org.authzen.Resource;
import org.authzen.examples.webflux.domain.ExampleAttributes;
import org.authzen.examples.webflux.domain.ExampleContextFactory;
import org.authzen.examples.webflux.domain.ExamplePrincipal;
import org.authzen.examples.webflux.domain.ExamplePrincipalFactory;
import org.authzen.examples.webflux.persistence.ExamplePrincipalPolicyDocumentMapper;
import org.authzen.examples.webflux.persistence.ExampleResourceDocumentMapper;
import org.authzen.examples.webflux.persistence.ExampleRolePolicyDocumentMapper;
import org.authzen.mongodb.reactive.MongoPrincipalPolicyRepository;
import org.authzen.mongodb.reactive.MongoResourceRepository;
import org.authzen.mongodb.reactive.MongoRolePolicyRepository;
import org.authzen.service.reactive.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import java.util.Map;

@Configuration
public class AppConfig {

    @Bean
    public AuthZen authZen() {
        return new AuthZen();
    }

    @Bean
    public ResourceRepository<Resource> resourceRepository(ReactiveMongoTemplate mongoTemplate,
                                                            ExampleResourceDocumentMapper mapper) {
        return new MongoResourceRepository<>(mongoTemplate, mapper);
    }

    @Bean
    public PrincipalPolicyRepository principalPolicyRepository(ReactiveMongoTemplate mongoTemplate,
                                                                ExamplePrincipalPolicyDocumentMapper mapper) {
        return new MongoPrincipalPolicyRepository(mongoTemplate, mapper);
    }

    @Bean
    public RolePolicyRepository rolePolicyRepository(ReactiveMongoTemplate mongoTemplate,
                                                      ExampleRolePolicyDocumentMapper mapper) {
        return new MongoRolePolicyRepository(mongoTemplate, mapper);
    }

    @Bean
    public AuthorizationService<ExamplePrincipal, Resource, ExampleAttributes, Map<String, Object>> authorizationService(
            AuthZen authZen,
            ResourceRepository<Resource> resourceRepository,
            PrincipalPolicyRepository principalPolicyRepository,
            RolePolicyRepository rolePolicyRepository,
            ExamplePrincipalFactory principalFactory,
            ExampleContextFactory contextFactory) {
        return new AuthorizationService<>(authZen, resourceRepository, principalPolicyRepository, rolePolicyRepository, principalFactory, contextFactory);
    }
}
