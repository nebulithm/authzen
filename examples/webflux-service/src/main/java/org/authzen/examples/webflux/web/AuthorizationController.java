package org.authzen.examples.webflux.web;

import lombok.RequiredArgsConstructor;
import org.authzen.examples.webflux.domain.ExampleAttributes;
import org.authzen.examples.webflux.domain.ExamplePrincipal;
import org.authzen.Resource;
import org.authzen.service.reactive.AuthorizationService;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationService<ExamplePrincipal, Resource, ExampleAttributes, Map<String, Object>> authorizationService;

    @PostMapping("/api/v1/authorize")
    public Mono<AuthorizeResponse> authorize(@RequestBody AuthorizeRequest request, ServerHttpRequest httpRequest) {
        return authorizationService.authorize(
                request.getUserId(),
                request.getUserAttributes(),
                request.getTargetResourceId(),
                request.getRequestedAction(),
                httpRequest
        ).map(decision -> new AuthorizeResponse(decision.isAllowed(), decision.getReason()));
    }
}
