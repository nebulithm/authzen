package org.authzen;

import tools.jackson.databind.module.SimpleModule;

public class AuthZenModule extends SimpleModule {
    public AuthZenModule() {
        super("AuthZenModule");
        addDeserializer(Statement.class, new StatementDeserializer());
        addDeserializer(Policy.class, new PolicyDeserializer());
    }
}
