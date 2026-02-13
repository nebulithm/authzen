package org.authzen.examples.webflux.web;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.authzen.examples.webflux.domain.ExampleAttributes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthorizeRequest {
    private String userId;
    private ExampleAttributes userAttributes;
    private String targetResourceId;
    private String requestedAction;
}
