package io.kestra.plugin.klaviyo;

import org.junit.jupiter.api.BeforeEach;

import io.kestra.core.runners.RunContextFactory;

import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Inject;

public abstract class AbstractKlaviyoTest {
    @Inject
    protected RunContextFactory runContextFactory;

    @Inject
    protected EmbeddedServer server;

    @BeforeEach
    void setUp() {
        if (!server.isRunning()) {
            server.start();
        }
    }

}
