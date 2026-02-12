package org.authzen;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class AuthZenModule extends SimpleModule {
    public AuthZenModule() {
        super("AuthZenModule");
        addDeserializer(Statement.class, new StatementDeserializer());
        addDeserializer(Policy.class, new PolicyDeserializer());
    }
}
