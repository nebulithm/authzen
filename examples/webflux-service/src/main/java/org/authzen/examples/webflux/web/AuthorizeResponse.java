package org.authzen.examples.webflux.web;

import lombok.Value;

@Value
public class AuthorizeResponse {
    boolean allowed;
    String reason;
}
